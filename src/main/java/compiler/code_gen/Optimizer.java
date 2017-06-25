package compiler.code_gen;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashSet;
import java.lang.String;

public class Optimizer {
    private final IntermediateRepresentation ir;
    private final ArrayList<Integer> basicBlocks;
    private int curQuad;
    public Optimizer(IntermediateRepresentation ir) {
        this.ir = ir;
        this.basicBlocks = new ArrayList<Integer>();
        this.curQuad = 0;
    }

    public void run() {
        ArrayList<Quad> quads = ir.getQuads();
        int leader = curQuad;
        for (ListIterator<Quad> it = quads.listIterator(curQuad+1) ; it.hasNext() ;) {
            int quadNum = it.nextIndex();
            Quad quad = it.next();
            if (ir.quadIsLabel(quadNum) && leader != quadNum) {
                basicBlocks.add(leader);
                leader = quadNum;
            }
            if (ir.quadIsJump(quadNum)) {
                basicBlocks.add(leader);
                leader = quadNum + 1;
            }
        }
        curQuad = quads.size();
    }

    public void print() {
        HashSet<Integer> leaders = new HashSet<Integer>(basicBlocks);
        ArrayList<Quad> quads = ir.getQuads();
        for (ListIterator<Quad> it = quads.listIterator() ; it.hasNext() ; ) {
            int quadNum = it.nextIndex();
            Quad quad = it.next();
            if (leaders.contains(quadNum)) {
                System.out.println("");
            }
            System.out.println(quadNum + ": " + quad);
        }
    }
}
