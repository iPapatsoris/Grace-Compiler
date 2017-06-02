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

    public FinalCode(IntermediateRepresentation ir, SymbolTable symbolTable,
                     String output) throws IOException {
        this.ir = ir;
        this.symbolTable = symbolTable;
        this.writer = new PrintWriter(output, "UTF-8");
        this.writer.println(".intel_syntax noprefix\n" +
                            ".text");
        this.curQuad = 0;
        this.wordSize = 4;
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

        for (ListIterator<Quad> it = quads.listIterator(curQuad) ; it.hasNext() ; curQuad++) {
            Quad quad = it.next();

            writer.println("\n" + curQuad + ":");
            switch (quad.getOp()) {
                case UNIT:
                    String originalName = uniqueToOriginal(quad.getOperand1().getIdentifier());
                    ArrayDeque<Variable> localVars = symbolTable.getLocalVars();
                    long curScope = symbolTable.getCurScope();
                    System.out.println("unique is " + quad.getOperand1().getIdentifier() + " original is " + originalName);
                    System.out.println("Local vars are " + localVars);
                    writer.println(quad.getOperand1().getIdentifier() + ":\n" +
                                   "push ebp\n" +
                                   "mov ebp, esp\n" +
                                   "sub esp, " + localVars.size() * wordSize);
                    break;
                case ENDU:
                    writer.println(quad.getOperand1().getIdentifier() + "_end:\n" +
                                   "mov esp, ebp\n" +
                                   "pop ebp\n" +
                                   "ret");
                    break;
                default:
                    //System.err.println("Internal error: wrong quad OP in FinalCode");
                    //System.exit(1);
            }
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
