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
                            boolean charInvolved = load("eax", quad.getOperand1());
                            push("eax", charInvolved);
                            break;
                        case R: ;
                        case RETCALLER:
                            loadAddr("si", quad.getOperand1());
                            writer.println("push si"); // needs special char treatment?
                            break;
                        default:
                            System.err.println("Internal error: wrong QuadOperand type " +
                                               quad.getOperand2().getType() + " in FinalCode");
                            //System.exit(1);
                    }
                    break;
                case CALL:
                    String calledFunction = quad.getOutput().getIdentifier();
                    String originalName = uniqueToOriginal(calledFunction);
                    Function function = (Function)symbolTable.lookup(originalName);
                    long totalSize = function.getArguments().size() * wordSize;
                    boolean isStandardLibrary = Function.isStandardLibrary(calledFunction);
                    if (function.getType() == Type.NOTHING && !isStandardLibrary) {
                        writer.println("sub sp, " + wordSize);
                        totalSize += wordSize;
                    }
                    if (isStandardLibrary) {
                        calledFunction =  "_" + originalName;
                    }
                    writer.println("call " + calledFunction + "\n" +
                                   "add sp, " + totalSize);
                    break;
                case UNIT:
                    curFunction = quad.getOperand1().getIdentifier();
                    originalName = uniqueToOriginal(curFunction);
                    localVars = symbolTable.getLocalVars();
                    numLocalVars = localVars.size();
                    numTempVars = tempVars.size() - curTempVar;
                    curTempVar += numTempVars;
                    //System.out.println("unique is " + curFunction + " original is " + originalName);
                    //System.out.println("Local vars are " + localVars);
                    totalSize = getTotalLocalVarsSize(localVars) + numTempVars * wordSize;
                    writer.println(curFunction + ":\n" +
                                   "push ebp\n" +
                                   "mov ebp, esp\n" +
                            /* */  "sub esp, " + totalSize);
                    break;
                case ENDU:
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
                default:
                    System.err.println("Internal error: wrong Quad OP " + quad.getOp() +
                                       " in FinalCode");
                    //System.exit(1);
            }
        }
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
                long offset = (numLocalVars + tempVar+1) * wordSize;
                //writer.println("mov " + register + ", DWORD PTR [ebp-" + offset + "]");
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

            default:
                System.err.println("Internal error: wrong quadOperand Type " +
                                   quadOperand.getType() + " in FinalCode load");
                //System.exit(1);
        }
        return charInvolved;
    }

    private void loadAddr(String register, QuadOperand quadOperand) {
        switch (quadOperand.getType()) {
            case TEMPVAR:
                int tempVar = quadOperand.getTempVar();
                ArrayList<Type> tempVars = ir.getTempVars();
                long offset = getTotalLocalVarsSize(localVars) + getTempVarInfo(tempVars, tempVar).getOffset()
                                                               + wordSize;
                writer.println("lea " + register + "DWORD PTR [ebp-" + offset + "]");
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
                long offset = (numLocalVars + tempVar + 1) * wordSize;
            //    writer.println("mov DWORD PTR [ebp-" + offset + "], " + register);
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
            default:
                System.err.println("Internal error: wrong quadOperand Type " +
                                   quadOperand.getType() + " in FinalCode store");
                //System.exit(1);
        }
    }

    public void closeWriter() {
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
            offset += getTypeSize(variable.getType());
        }
        return null;
    }

    public SymbolInfo getTempVarInfo(ArrayList<Type> tempVars, int tempVar) {
        long offset = 0;
        if (tempVar < curTempVar) {
            System.err.println("Internal error: tempVar is less than curTempVar in getTempVarInfo");
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

    private static int getArgumentIndex(int argumentPos, int totalArguments) {
        return (totalArguments - argumentPos) + 4;
    }

    private static long getTotalLocalVarsSize(ArrayDeque<Variable> localVars) {
        long index = 0;
        for (Variable variable: localVars) {
            index += getTypeSize(variable.getType());
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
