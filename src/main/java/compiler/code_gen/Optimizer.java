package compiler.code_gen;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashSet;
import java.lang.String;

public class Optimizer {
    private final IntermediateRepresentation ir;
    private final ArrayList<BasicBlock> basicBlocks;
    private int curQuad;
    public Optimizer(IntermediateRepresentation ir) {
        this.ir = ir;
        this.basicBlocks = new ArrayList<BasicBlock>();
        this.curQuad = 0;
    }

    public void run() {
        basicBlocks.clear();
        ArrayList<Quad> quads = ir.getQuads();
        int start = curQuad;
        for (ListIterator<Quad> it = quads.listIterator(curQuad+1) ; it.hasNext() ;) {
            int quadNum = it.nextIndex();
            Quad quad = it.next();
            if (ir.quadIsLabel(quadNum) && start != quadNum) {
                basicBlocks.add(new BasicBlock(start, quadNum - 1));
                start = quadNum;
            }
            if (ir.quadIsJump(quadNum)) {
                basicBlocks.add(new BasicBlock(start, quadNum));
                start = quadNum + 1;
            }
        }
        curQuad = quads.size();
    }

    public void print() {
        for (BasicBlock basicBlock : basicBlocks) {
            System.out.println(basicBlock);
        }
        System.out.println("");
    }

    private static class BasicBlock {
        private final int start;
        private final int end;

        BasicBlock(int start, int end) {
            this.start = start;
            this.end = end;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return String.valueOf(start) + " " + String.valueOf(end);
        }
    }
}
