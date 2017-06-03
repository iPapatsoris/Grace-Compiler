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
    private final int wordSize;

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
        this.wordSize = 4;
        this.localVars = null;
        this.numLocalVars = 0;
        this.numTempVars = 0;
    }

    public void addMainFunction(String name) {
        writer.println("\t.global main\n" +
                       "main:\n" +
                       "push ebp\n" +
                       "mov ebp, esp\n" +
                       "call _" + name + "_0\n" +
                       "mov esp, ebp\n" +
                       "pop ebp\n" +
                       "ret");
    }

    public void generate() {
        ArrayList<Quad> quads = ir.getQuads();
        ArrayList<Type> tempVars = ir.getTempVars();

        String curFunction = null;

        for (ListIterator<Quad> it = quads.listIterator(curQuad) ; it.hasNext() ; curQuad++) {
            Quad quad = it.next();

            writer.println("\n" + curQuad + ":");
            switch (quad.getOp()) {
                case UNIT:
                    curFunction = quad.getOperand1().getIdentifier();
                    String originalName = uniqueToOriginal(curFunction);
                    localVars = symbolTable.getLocalVars();
                    numLocalVars = localVars.size();
                    numTempVars = tempVars.size() - curQuad;
                    System.out.println("unique is " + curFunction + " original is " + originalName);
                    //System.out.println("Local vars are " + localVars);
                    writer.println(curFunction + ":\n" +
                                   "push ebp\n" +
                                   "mov ebp, esp\n" +
                                   "sub esp, " + (numLocalVars + numTempVars) * wordSize);
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
                    System.err.println("Internal error: wrong quad OP " + quad.getOp() +
                                       " in FinalCode");
                    //System.exit(1);
            }
        }
    }

    private void load(String register, QuadOperand quadOperand) {
        switch (quadOperand.getType()) {
            case INT:
                writer.println("mov " + register + ", " + Integer.parseInt(quadOperand.getIdentifier()));
                break;
            default:
                System.err.println("Internal error: wrong quadOperand Type " +
                                   quadOperand.getType() + " in FinalCode load");
                //System.exit(1);
        }
    }

    private void store(String register, QuadOperand quadOperand) {
        switch (quadOperand.getType()) {
            case TEMPVAR:
                int tempVar = quadOperand.getTempVar();
                long offset = (numLocalVars + tempVar) * wordSize;
                writer.println("mov word ptr [bp-" + offset + "], " + register);
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

    public static String uniqueToOriginal(String unique) {
        int index = unique.lastIndexOf("_");
        if (index == -1) {
            index = unique.length();
        }
        return unique.substring(1, index);
    }
}
