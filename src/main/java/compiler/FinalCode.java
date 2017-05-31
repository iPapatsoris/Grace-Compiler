package compiler;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.lang.String;
import java.io.PrintWriter;
import java.io.IOException;


class FinalCode {
    IntermediateRepresentation ir;
    private String output;

    public FinalCode(IntermediateRepresentation ir, String output) {
        this.ir = ir;
        this.output = output;
    }

    public void generate() throws IOException {
        PrintWriter writer = new PrintWriter(output, "UTF-8");

        ArrayList<Quad> quads = ir.getQuads();
        ArrayList<Type> tempVars = ir.getTempVars();

        for (ListIterator<Quad> it = quads.listIterator() ; it.hasNext() ; ) {
            int quadNumber = it.nextIndex();
            Quad quad = it.next();
        }
        writer.close();
    }
}
