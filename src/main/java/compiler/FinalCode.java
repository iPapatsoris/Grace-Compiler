package compiler;

import java.util.Collections;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ListIterator;
import java.lang.String;
import java.io.PrintWriter;
import java.io.IOException;


class FinalCode {
    private IntermediateRepresentation ir;
    private SymbolTable symbolTable;
    private PrintWriter writer;
    private int curQuad;
    private int curTempVar;
    private final int wordSize;

    private String curFunction;
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
                       "push ebp\n" +
                       "mov ebp, esp\n\n" +
                       "sub esp, 8\n" +
                       "call _" + name + "_0\n" +
                       "add esp, 8\n\n" +
                       "mov esp, ebp\n" +
                       "pop ebp\n" +
                       "ret");
    }

    public void generate() {
        ArrayList<Quad> quads = ir.getQuads();
        ArrayList<Type> tempVars = ir.getTempVars();

        for (ListIterator<Quad> it = quads.listIterator(curQuad) ; it.hasNext() ; curQuad++) {
            Quad quad = it.next();

            writer.println("\n" + curQuad + ":");
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
                    localVars = symbolTable.getLocalVars();
                    numLocalVars = localVars.size();
                    numTempVars = tempVars.size() - curTempVar;
                    //System.out.println("unique is " + curFunction + " original is " + originalName);
                    //System.out.println("Local vars are " + localVars);
                    totalSize = getTotalLocalVarsSize(localVars) + numTempVars * wordSize;
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
                    boolean charInvolved = load("eax", quad.getOperand2());
                    writer.println("mov ecx, " + (charInvolved ? 1 : wordSize) + "\n" +
                                   "imul ecx");
                    loadAddr("ecx", quad.getOperand1());
                    writer.println("add eax, ecx");

                    /* Change type from ADDRESS to TEMPVAR for store */
                    store("eax", new QuadOperand(QuadOperand.Type.TEMPVAR, quad.getOutput().getTempVar()));
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
                register = "al";
                writer.println("mov " + register + ", " + quadOperand.getIdentifier());
                break;
            case TEMPVAR:
                int tempVar = quadOperand.getTempVar();
                long offset = getTempVarOffset(tempVar);
                Type tempVarType = getTempVarInfo(ir.getTempVars(), tempVar).getType();
                if (tempVarType == Type.CHAR) {
                    register = "al";
                }
                writer.println("mov " + register + ", " + getTypeSizeName(tempVarType) +
                              " [ebp-" + offset + "]");
                break;
            case IDENTIFIER:
                String identifier = quadOperand.getIdentifier();
                SymbolInfo localVarInfo = getLocalVarInfo(localVars, identifier);
                if (localVarInfo != null) {
                    if (localVarInfo.getType() == Type.CHAR) {
                        charInvolved = true;
                        register = "al";
                    }
                    writer.println("mov " + register + ", " +
                                    getTypeSizeName(localVarInfo.getType()) +
                                    " [ebp-" + (localVarInfo.getOffset() + wordSize) + "]");
                    break;
                }
                /*String originalName = uniqueToOriginal(curFunction);
                Function function = (Function)symbolTable.lookup(originalName);
                index = indexOfArgument(function.getArguments(), identifier);
                if (index >= 0) {
                    offset = (getArgumentIndex(index, function.getArguments().size()) + 1) * wordSize;
                    //writer.println("mov " + register + ", DWORD PTR [ebp+" + offset + "]");
                }*/
                break;
            case ADDRESS:
                tempVar = quadOperand.getTempVar();
                tempVarType = getTempVarInfo(ir.getTempVars(), tempVar).getType();
                load("edi", new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
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
                                   " [ebp-" + (localVarInfo.getOffset() + wordSize) + "], " + register);
                    break;
                }
                /*String originalName = uniqueToOriginal(curFunction);
                Function function = (Function)symbolTable.lookup(originalName);
                index = indexOfArgument(function.getArguments(), identifier);
                if (index >= 0) {
                    offset = (getArgumentIndex(index, function.getArguments().size()) + 1) * wordSize;
                    writer.println("mov DWORD PTR [bp+" + offset + "], " + register);
                }*/
                break;
            /*case ADDRESS:
                tempVar = quadOperand.getTempVar();
                tempVarType = getTempVarInfo(ir.getTempVars(), tempVar).getType();
                load("edi", new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
                writer.println("mov " + getTypeSizeName(tempVarType) +
                               " [edi], " + register);
                break;*/
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

    public int getIndexOfArgument(ArrayDeque<Argument> arguments, String identifier) {
        int index = 0;
        for (Iterator<Argument> it = arguments.iterator() ; it.hasNext() ; index++) {
            if (it.next().getToken().getText().equals(identifier)) {
                return index;
            }
        }
        return -1;
    }

    /* Temp var offset in current stack frame */
    public long getTempVarOffset(int tempVar) {
        ArrayList<Type> tempVars = ir.getTempVars();
        System.out.println("it's " + getTotalLocalVarsSize(localVars) + " " + getTempVarInfo(tempVars, tempVar).getOffset() + " " +
                                                + wordSize);
        return getTotalLocalVarsSize(localVars) + getTempVarInfo(tempVars, tempVar).getOffset()
                                                + wordSize;
    }

    /* Used to get info about a particular symbol within a list of local vars,
     * arguments or temp vars:
     *  - its byte position in that list, taking into account the size of the other symbols
     *  - its type
     */
    private class SymbolInfo {
        private long offset;
        private Type type;

        public SymbolInfo(long offset, Type type) {
            this.offset = offset;
            this.type = type;
        }

        public long getOffset() {
            return offset;
        }

        public Type getType() {
            return type;
        }
    }

    public SymbolInfo getLocalVarInfo(ArrayDeque<Variable> variables, String identifier) {
        long offset = 0;
        for (Iterator<Variable> it = variables.iterator() ; it.hasNext() ; ) {
            Variable variable = it.next();
            if (variable.getToken().getText().equals(identifier)) {
                return new SymbolInfo(offset, variable.getType());
            }
            offset += getLocalVarSize(variable);
        }
        return null;
    }

    public SymbolInfo getTempVarInfo(ArrayList<Type> tempVars, int tempVar) {
        long offset = 0;
        if (tempVar < curTempVar) {
            System.err.println("Internal error: tempVar " + tempVar +
                               " is less than curTempVar " + curTempVar + " in getTempVarInfo");
            System.exit(1);
        }
        for (ListIterator<Type> i = tempVars.listIterator(curTempVar) ; i.hasNext() ; ) {
            Type tempVarType = i.next();
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

    private static long getTotalLocalVarsSize(ArrayDeque<Variable> localVars) {
        long index = 0;
        for (Variable variable: localVars) {
            index += getLocalVarSize(variable);
        }
        return index;
    }

    private static long getLocalVarSize(Variable variable) {
        long index = 0;
        if (variable.getDimensions().size() == 0) {
            index += getTypeSize(variable.getType());
        } else {
            for (Integer dimension: variable.getDimensions()) {
                index += (dimension * getTypeSize(variable.getType()));
            }
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

    public static String getTypeSizeName(Type type) {
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

    private static int getArgumentIndex(int argumentPos, int totalArguments) {
        return (totalArguments - argumentPos) + 4;
    }

    public static String makeUniqueFunctionName(String function, String scope) {
        return "_" + function + "_" + String.valueOf(scope);
    }

    public static String uniqueToOriginal(String unique) {
        int index = unique.lastIndexOf("_");
        if (index == -1) {
            index = unique.length();
        }
        return unique.substring(1, index);
    }
}
