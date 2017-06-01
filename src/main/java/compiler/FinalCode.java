package compiler;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.lang.String;
import java.io.PrintWriter;
import java.io.IOException;


class FinalCode {
    private IntermediateRepresentation ir;
    private PrintWriter writer;
    private int curQuad;

    public FinalCode(IntermediateRepresentation ir, String output) throws IOException {
        this.ir = ir;
        this.writer = new PrintWriter(output, "UTF-8");
        this.writer.println(".intel_syntax noprefix\n" +
                            ".text");
        this.curQuad = 0;
    }

    public void generate() {
        ArrayList<Quad> quads = ir.getQuads();
        ArrayList<Type> tempVars = ir.getTempVars();

        for (ListIterator<Quad> it = quads.listIterator(curQuad) ; it.hasNext() ; curQuad++) {
            Quad quad = it.next();

            writer.println("\n@" + curQuad + ":");
            switch (quad.getOp()) {
                case UNIT:
                    writer.println(quad.getOperand1().getIdentifier() + " proc near\n" +
                                   "push bp\n" +
                                   "mov bp, sp");
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
}
