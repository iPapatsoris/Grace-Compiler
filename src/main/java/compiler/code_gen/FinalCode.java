package compiler.code_gen;

import compiler.symbol_table.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ListIterator;
import java.lang.String;
import java.io.PrintWriter;
import java.io.IOException;


public class FinalCode {
    private IntermediateRepresentation ir;
    private SymbolTable symbolTable;
    private PrintWriter writer;
    private int curQuad;
    private int curTempVar;
    private final int wordSize;

    private String curFunction;
    private ArrayDeque<Argument> arguments;
    private ArrayDeque<Variable> localVars;
    private int numLocalVars;
    private int numTempVars;
    private QuadOperand curReturnTempVar;
    private ArrayList<String> stringLiterals;
    private ArrayDeque<Quad> passParameters;

    public FinalCode(IntermediateRepresentation ir, SymbolTable symbolTable,
                     String output) throws IOException {
        this.ir = ir;
        this.symbolTable = symbolTable;
        this.writer = new PrintWriter(output, "UTF-8");
        this.writer.println(".intel_syntax noprefix\n" +
                            ".text");
        this.curQuad = 0;
        this.curTempVar = 0;
        this.wordSize = 4;
        this.curFunction = null;
        this.arguments = null;
        this.localVars = null;
        this.numLocalVars = 0;
        this.numTempVars = 0;
        this.curReturnTempVar = null;
        this.stringLiterals = new ArrayList<String>();
        this.passParameters = new ArrayDeque<Quad>();
    }

    public void addMainFunction(String name) {
        writer.println("\t.global main\n" +
                       "main:\n" +
                       //"push ebp\n" +
                       //"mov ebp, esp\n\n" +
                       //"sub esp, 8\n" +
                       "call _" + name + "_0\n" +
                       //"add esp, 8\n\n" +
                       //"mov esp, ebp\n" +
                       //"pop ebp\n" +
                       "ret");
    }

    public void generate() {
        ArrayList<Quad> quads = ir.getQuads();
        ArrayList<Type> tempVars = ir.getTempVars();

        for (ListIterator<Quad> it = quads.listIterator(curQuad) ; it.hasNext() ; curQuad++) {
            Quad quad = it.next();

            writer.println("\nL" + curQuad + ":");
            switch (quad.getOp()) {
                case PAR:
                    switch (quad.getOperand2().getType()) {
                        case V:
                        case R:
                            passParameters.push(quad);
                            break;
                        case RETCALLER:
                            curReturnTempVar = quad.getOperand1();
                            break;
                        default:
                            System.err.println("Internal error: wrong QuadOperand type " +
                                               quad.getOperand2().getType() + " in FinalCode");
                            //System.exit(1);
                    }
                    break;
                case CALL:
                    handleParameters();
                    String calledFunction = quad.getOutput().getIdentifier();
                    String originalName = uniqueToOriginal(calledFunction);
                    Function function = (Function)symbolTable.lookup(originalName);
                    long totalSize = function.getArguments().size() * wordSize;
                    boolean isStandardLibrary = Function.isStandardLibrary(calledFunction);
                    if (isStandardLibrary) {
                        calledFunction =  "_" + originalName;
                    }
                    writer.println("call " + calledFunction + (totalSize > 0 ? "\n" +
                                   "add esp, " + totalSize : ""));
                    if (function.getType() != Type.NOTHING) {
                        loadAddr("esi", curReturnTempVar);
                        String register = "eax";
                        if (function.getType() == Type.CHAR) {
                            writer.println("push eax\n" +
                                           "mov al, BYTE PTR [esp]\n" +
                                           "pop eax");
                            register = "al";
                        }
                        writer.println("mov " + getTypeSizeName(function.getType()) +
                                       " [esi], " + register);
                    }
                    break;
                case UNIT:
                    curFunction = quad.getOperand1().getIdentifier();
                    originalName = uniqueToOriginal(curFunction);
                    arguments = ((Function)symbolTable.lookup(originalName)).getArguments();
                    localVars = symbolTable.getLocalVars();
                    numLocalVars = localVars.size();
                    numTempVars = tempVars.size() - curTempVar;
                    //System.out.println("unique is " + curFunction + " original is " + originalName);
                    //System.out.println("Local vars are " + localVars);
                    totalSize = getTotalLocalVarsSize() + numTempVars * wordSize;
                    writer.println(curFunction + ":\n" +
                                   "push ebp\n" +
                                   "mov ebp, esp\n" +
                            /* */  "sub esp, " + totalSize);
                    break;
                case ENDU:
                    curTempVar += numTempVars;
                    writer.println(curFunction + "_end:\n" +
                                   "mov esp, ebp\n" +
                                   "pop ebp\n" +
                                   "ret");
                    break;
                case RET:
                    writer.println("jmp " + curFunction + "_end");
                    break;
                case ASSIGN:
                    load("eax", quad.getOperand1());
                    store("eax", quad.getOutput());
                    break;
                case ARRAY:
                    load("eax", quad.getOperand2());
                    String identifier = quad.getOperand1().getIdentifier();
                    SymbolInfo symbolInfo = getLocalVarInfo(localVars, identifier);
                    Type type = null;
                    if (symbolInfo != null) {
                        type = symbolInfo.getType();
                    } else {
                        type = getArgumentInfo(arguments, identifier).getType();
                    }

                    writer.println("mov ecx, " + getTypeSize(type) + "\n" +
                                   "imul ecx");
                    loadAddr("ecx", quad.getOperand1());
                    writer.println("add eax, ecx");

                    /* Change type from ADDRESS to TEMPVAR for store */
                    store("eax", new QuadOperand(QuadOperandType.TEMPVAR, quad.getOutput().getTempVar()));
                    break;
                case ADD:
                case SUB:
                case MULT:
                    load("eax", quad.getOperand1());
                    load("edx", quad.getOperand2());
                    writer.println(convertOpToCommand(quad.getOp()) + " eax, edx");
                    store("eax", quad.getOutput());
                    break;
                case DIV:
                case MOD:
                    String register = null;
                    if (quad.getOp() == Quad.Op.DIV) {
                        register = "eax";
                    } else if (quad.getOp() == Quad.Op.MOD) {
                        register = "edx";
                    }
                    load("eax", quad.getOperand1());
                    writer.println("cdq");
                    load("ebx", quad.getOperand2());
                    writer.println("idiv ebx");
                    store(register, quad.getOutput());
                    break;
                case JUMP:
                    writer.println("jmp L" + quad.getOutput());
                    break;
                case EQUAL:
                case NOT_EQUAL:
                case GREATER:
                case LESS:
                case GREATER_EQUAL:
                case LESS_EQUAL:
                    load("eax", quad.getOperand1());
                    load("edx", quad.getOperand2());
                    writer.println("cmp eax, edx");
                    writer.println(convertOpToCommand(quad.getOp()) + " L" + quad.getOutput());
                    break;
                default:
                    System.err.println("Internal error: wrong Quad OP " + quad.getOp() +
                                       " in FinalCode");
                    //System.exit(1);
            }
        }
    }

    private void handleParameters() {
        for (Quad quad: passParameters) {
            switch (quad.getOperand2().getType()) {
                case V:
                    boolean charInvolved = load("eax", quad.getOperand1());
                    push("eax", charInvolved);
                    break;
                case R:
                    loadAddr("esi", quad.getOperand1());
                    writer.println("push esi"); // needs special char treatment?
                    break;
                default:
                    System.err.println("Internal error: wrong QuadOperand type " +
                                       quad.getOperand2().getType() + " in handleParameters");
                    //System.exit(1);
            }
        }
        passParameters.clear();
    }

    private void push(String register, boolean charInvolved) {
        if (charInvolved) {
            writer.println("movzx " + register + ", al");
        }
        writer.println("push " + register);
    }

    /* Register is changed to 'al' if char datatype is involved */
    private boolean load(String register, QuadOperand quadOperand) {
        boolean charInvolved = false;
        switch (quadOperand.getType()) {
            case INT:
                writer.println("mov " + register + ", " + Integer.parseInt(quadOperand.getIdentifier()));
                break;
            case CHAR:
                charInvolved = true;
                String character = handleSpecialCharacter(quadOperand.getIdentifier());
                register = "al";
                writer.println("mov " + register + ", " + character);
                break;
            case TEMPVAR:
                int tempVar = quadOperand.getTempVar();
                long offset = getTempVarOffset(tempVar);
                Type tempVarType = getTempVarInfo(ir.getTempVars(), tempVar).getType();
                if (tempVarType == Type.CHAR) {
                    charInvolved = true;
                    register = "al";
                }
                writer.println("mov " + register + ", " + getTypeSizeName(tempVarType) +
                              " [ebp-" + offset + "]");
                break;
            case IDENTIFIER:
                String identifier = quadOperand.getIdentifier();
                SymbolInfo localVarInfo = getLocalVarInfo(localVars, identifier);
                if (localVarInfo != null) {
                    System.out.println("it's a local " + identifier);

                    if (localVarInfo.getType() == Type.CHAR) {
                        charInvolved = true;
                        register = "al";
                    }
                    writer.println("mov " + register + ", " +
                                    getTypeSizeName(localVarInfo.getType()) +
                                    " [ebp-" + (localVarInfo.getOffset() + wordSize) + "]");
                    break;
                }
                String originalName = uniqueToOriginal(curFunction);
                Function function = (Function)symbolTable.lookup(originalName);
                SymbolInfo argumentInfo = getArgumentInfo(function.getArguments(), identifier);
                System.out.println("Searching for " + identifier);
                if (argumentInfo != null) {
                    System.out.println(identifier + " is an argument");
                    if (argumentInfo.getType() == Type.CHAR) {
                        charInvolved = true;
                        register = "al";
                    }
                    if (! argumentInfo.isReference()) {
                        writer.println("mov " + register + ", " +
                                        getTypeSizeName(argumentInfo.getType()) +
                                        " [ebp+" + (argumentInfo.getOffset() + 2 * wordSize) + "]");
                    } else {
                        writer.println("mov edi, DWORD PTR [ebp+" +
                                       (argumentInfo.getOffset() + 2 * wordSize) + "]");
                        writer.println("mov " + register + ", " + getTypeSizeName(argumentInfo.getType()) +
                                       " [edi]");
                    }
                    break;
                }
                break;
            case ADDRESS:
                tempVar = quadOperand.getTempVar();
                tempVarType = getTempVarInfo(ir.getTempVars(), tempVar).getType();
                load("edi", new QuadOperand(QuadOperandType.TEMPVAR, tempVar));
                writer.println("mov " + register + ", " + getTypeSizeName(tempVarType) +
                               " [edi]");
                break;
            default:
                System.err.println("Internal error: wrong quadOperand Type " +
                                   quadOperand.getType() + " in FinalCode load");
                //System.exit(1);
        }
        return charInvolved;
    }

    private void loadAddr(String register, QuadOperand quadOperand) {
        switch (quadOperand.getType()) {
            case STRING:
                writer.println("mov " + register + ", OFFSET FLAT:string_literal_" +
                               stringLiterals.size());
                stringLiterals.add(quadOperand.getIdentifier());
                break;
            case TEMPVAR: // char treatment?
                int tempVar = quadOperand.getTempVar();
                long offset = getTempVarOffset(tempVar);
                writer.println("lea " + register + ", DWORD PTR [ebp-" + offset + "]");
                break;
            case IDENTIFIER:
                String identifier = quadOperand.getIdentifier();
                SymbolInfo localVarInfo = getLocalVarInfo(localVars, identifier);
                if (localVarInfo != null) {
                    writer.println("lea " + register + ", DWORD PTR [ebp-" +
                                   (localVarInfo.getOffset() + wordSize) + "]");
                    break;
                }
                String originalName = uniqueToOriginal(curFunction);
                Function function = (Function)symbolTable.lookup(originalName);
                SymbolInfo argumentInfo = getArgumentInfo(function.getArguments(), identifier);
                if (argumentInfo != null) {
                    if (! argumentInfo.isReference()) {
                        writer.println("lea " + register + ", " +
                                        getTypeSizeName(argumentInfo.getType()) +
                                        " [ebp+" + (argumentInfo.getOffset() + 2 * wordSize) + "]");
                    } else {
                        writer.println("mov " + register + ", DWORD PTR [ebp+" +
                                       (argumentInfo.getOffset() + 2 * wordSize) + "]");
                    }
                    break;
                }
                break;
            case ADDRESS:
                load(register, new QuadOperand(QuadOperandType.TEMPVAR, quadOperand.getTempVar()));
                break;
            default:
                System.err.println("Internal error: wrong quadOperand Type " +
                                   quadOperand.getType() + " in FinalCode loadAddr");
                //System.exit(1);

        }
    }

    /* Register is changed to 'al' if char datatype is involved */
    private void store(String register, QuadOperand quadOperand) {
        switch (quadOperand.getType()) {
            case TEMPVAR:
                int tempVar = quadOperand.getTempVar();
                long offset = getTempVarOffset(tempVar);
                Type tempVarType = getTempVarInfo(ir.getTempVars(), tempVar).getType();
                if (tempVarType == Type.CHAR) {
                    register = "al";
                }
                writer.println("mov " + getTypeSizeName(tempVarType) +
                              " [ebp-" + offset + "], " + register);
                break;
            case IDENTIFIER:
                String identifier = quadOperand.getIdentifier();
                SymbolInfo localVarInfo = getLocalVarInfo(localVars, identifier);
                if (localVarInfo != null) {
                    if (localVarInfo.getType() == Type.CHAR) {
                        register = "al";
                    }
                    writer.println("mov " + getTypeSizeName(localVarInfo.getType()) +
                                   " [ebp-" + (localVarInfo.getOffset() + wordSize) +
                                   "], " + register);
                    break;
                }
                String originalName = uniqueToOriginal(curFunction);
                Function function = (Function)symbolTable.lookup(originalName);
                SymbolInfo argumentInfo = getArgumentInfo(function.getArguments(), identifier);
                if (argumentInfo != null) {
                    System.out.println(identifier + " is an argument");
                    if (argumentInfo.getType() == Type.CHAR) {
                        register = "al";
                    }
                    if (! argumentInfo.isReference()) {
                        writer.println("mov " + getTypeSizeName(argumentInfo.getType()) +
                                       " [ebp+" + (argumentInfo.getOffset() + 2 * wordSize) +
                                       "], " + register);
                    } else {
                        writer.println("mov edi, DWORD PTR [ebp+" +
                                       (argumentInfo.getOffset() + 2 * wordSize) + "]");
                        writer.println("mov " + getTypeSizeName(argumentInfo.getType()) +
                                       " [edi], " + register);
                    }
                    break;
                }
                break;
            case ADDRESS:
                tempVar = quadOperand.getTempVar();
                ArrayInfo arrayInfo = ir.getArrayInfo().get(tempVar);
                if (arrayInfo == null) {
                    System.err.println("Internal error: tempVar " + tempVar +
                                       " not found in arrayInfo map");
                    System.exit(1);
                }
                tempVarType = (arrayInfo.getDimensionsLeft() > 0 ? Type.INT :
                               arrayInfo.getArrayType());
                if (tempVarType == Type.CHAR) {
                    register = "al";
                }
                load("edi", new QuadOperand(QuadOperandType.TEMPVAR, tempVar));
                writer.println("mov " + getTypeSizeName(tempVarType) +
                               " [edi], " + register);
                break;
            case RETCALLED:
                writer.println("mov eax, " + register);
                break;
            default:
                System.err.println("Internal error: wrong quadOperand Type " +
                                   quadOperand.getType() + " in FinalCode store");
                //System.exit(1);
        }
    }

    public void closeWriter() {
        if (stringLiterals.size() > 0) {
            writer.println("\n.data");
        }
        for (ListIterator<String> it = stringLiterals.listIterator() ; it.hasNext() ; ) {
            String stringLiteral = it.next();
            writer.println("string_literal_" + it.previousIndex() + ": .asciz " +
                           stringLiteral);
        }
        writer.close();
    }

    private int getIndexOfArgument(ArrayDeque<Argument> arguments, String identifier) {
        int index = 0;
        for (Iterator<Argument> it = arguments.iterator() ; it.hasNext() ; index++) {
            if (it.next().getToken().getText().equals(identifier)) {
                return index;
            }
        }
        return -1;
    }

    /* Temp var offset in current stack frame */
    private long getTempVarOffset(int tempVar) {
        ArrayList<Type> tempVars = ir.getTempVars();
        System.out.println("it's " + getTotalLocalVarsSize() + " " + getTempVarInfo(tempVars, tempVar).getOffset() + " " +
                                                + wordSize);
        return getTotalLocalVarsSize() + getTempVarInfo(tempVars, tempVar).getOffset()
                                                + wordSize;
    }

    /* Used to get info about a particular symbol within a list of local vars,
     * arguments or temp vars:
     *  - its byte position in that list, taking into account the size of the other symbols
     *  - its type
     *  - its pass method, if an argument
     */
    private class SymbolInfo {
        private long offset;
        private Type type;
        private boolean isReference;

        public SymbolInfo(long offset, Type type) {
            this.offset = offset;
            this.type = type;
            this.isReference = false;
        }

        public SymbolInfo(long offset, Type type, boolean isReference) {
            this.offset = offset;
            this.type = type;
            this.isReference = isReference;
        }

        public long getOffset() {
            return offset;
        }

        public Type getType() {
            return type;
        }

        public boolean isReference() {
            return isReference;
        }
    }

    private SymbolInfo getLocalVarInfo(ArrayDeque<Variable> variables, String identifier) {
        long offset = 0;
        for (Iterator<Variable> it = variables.iterator() ; it.hasNext() ; ) {
            Variable variable = it.next();
            if (variable.getType() == Type.INT && (offset > 0 && offset % wordSize != 0)) {
                offset = nextWordAlignedByte(offset, wordSize);
            }
            if (variable.getToken().getText().equals(identifier)) {
                long dimensions = variable.getDimensions().size();
                if (dimensions > 0) {
                    System.out.println(variable + " total cells: " + variable.getTotalCells());
                    offset += ((variable.getTotalCells() - 1) * getTypeSize(variable.getType()));
                }
                return new SymbolInfo(offset, variable.getType());
            }
            offset += getLocalVarSize(variable);
        }
        return null;
    }

    private SymbolInfo getTempVarInfo(ArrayList<Type> tempVars, int tempVar) {
        long offset = 0;
        if (tempVar < curTempVar) {
            System.err.println("Internal error: tempVar " + tempVar +
                               " is less than curTempVar " + curTempVar + " in getTempVarInfo");
            System.exit(1);
        }
        for (ListIterator<Type> i = tempVars.listIterator(curTempVar) ; i.hasNext() ; ) {
            Type tempVarType = i.next();
            if (tempVarType == Type.INT && (offset > 0 && offset % wordSize != 0)) {
                offset = nextWordAlignedByte(offset, wordSize);
            }
            if (i.previousIndex() == tempVar) {
                return new SymbolInfo(offset, tempVarType);
            }
            offset += getTypeSize(tempVarType);
        }
        System.err.println("Internal error: tempVar " + tempVar + " does not exist in " +
                           "tempVars in getTempVarInfo");
        System.exit(1);
        return null;
    }

    private SymbolInfo getArgumentInfo(ArrayDeque<Argument> arguments, String identifier) {
        long offset = 0;
        for (Iterator<Argument> it = arguments.iterator() ; it.hasNext() ; ) {
            Argument argument = it.next();
            if (argument.getToken().getText().equals(identifier)) {
                return new SymbolInfo(offset, argument.getType(), argument.isReference());
            }
            offset += wordSize;
        }
        return null;
    }

    private long getTotalLocalVarsSize() {
        long index = 0;
        for (Variable variable: localVars) {
            if (variable.getType() == Type.INT && (index > 0 && index % wordSize != 0)) {
                index = nextWordAlignedByte(index, wordSize);
            }
            index += getLocalVarSize(variable);
        }
        return index;
    }

    private static long getLocalVarSize(Variable variable) {
        long index = 0;
        if (variable.getDimensions().size() == 0) {
            index += getTypeSize(variable.getType());
        } else {
            index += (variable.getTotalCells() * getTypeSize(variable.getType()));
        }
        return index;
    }

    private static int getTypeSize(Type type) {
        switch (type) {
            case INT:
                return 4;
            case CHAR:
                return 1;
            default:
                System.err.println("Internal error: invalid variable type " + type + " in getTypeSize");
        }
        return -1;
    }

    private static String getTypeSizeName(Type type) {
        switch (type) {
            case INT:
                return "DWORD PTR";
            case CHAR:
                return "BYTE PTR";
            default:
                System.err.println("Internal error: invalid variable type " + type + " in getTypeSizeName");
        }
        return null;
    }


    public int getCurTempVar() {
        return curTempVar;
    }

    public int getCurQuad() {
        return curQuad;
    }

    private static long nextWordAlignedByte(long pos, int wordSize) {
        if (pos == 0) {
            return pos;
        }
        while (pos % wordSize != 0) {
            pos++;
        }
        return pos;
    }

    private static String convertOpToCommand(Quad.Op op) {
        switch (op) {
            case ADD:
                return "add";
            case SUB:
                return "sub";
            case MULT:
                return "imul";
            case EQUAL:
                return "jz";
            case NOT_EQUAL:
                return "jnz";
            case GREATER:
                return "jg";
            case LESS:
                return "jl";
            case GREATER_EQUAL:
                return "jge";
            case LESS_EQUAL:
                return "jle";
            default:
                System.err.println("Internal error: OP is not listed in convertOpToCommand");
                System.exit(1);
        }
        return "";
    }

    public static String makeUniqueFunctionName(String function, String scope) {
        return "_" + function + "_" + String.valueOf(scope);
    }

    private static String uniqueToOriginal(String unique) {
        int index = unique.lastIndexOf("_");
        if (index == -1) {
            index = unique.length();
        }
        return unique.substring(1, index);
    }

    private static String handleSpecialCharacter(String character) {
        switch (character) {
            case "'\\0'":
                return "0";
            default:
                return character;
        }
    }
}
