package compiler.code_gen;

import compiler.symbol_table.Type;
import compiler.node.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.lang.String;

public class IntermediateRepresentation {
    private final ArrayList<Quad> quads;
    private final ArrayList<Type> tempVars;
    private final HashMap<Integer, ArrayInfo> arrayInfo;
    private final HashSet<Integer> labels; // Used only by Optimizer
    private final boolean optimize;

    public IntermediateRepresentation(boolean optimize) {
        this.quads = new ArrayList<Quad>();
        this.tempVars = new ArrayList<Type>();
        this.arrayInfo = new HashMap<Integer, ArrayInfo>();
        this.labels = new HashSet<Integer>();
        this.optimize = optimize;
    }

    public int getNextQuadIndex() {
        return quads.size();
    }

    public void insertQuad(Quad quad) {
        quads.add(quad);
    }

    public int newTempVar(Type type) {
        tempVars.add(type);
        return tempVars.size()-1;
    }

    public void backpatch(ArrayList<Integer> toBackpatch, int destinationQuad) {
        for (Integer quad : toBackpatch) {
            quads.get(quad).setOutput(new QuadOperand(QuadOperand.Type.LABEL, destinationQuad));
            if (optimize) {
                labels.add(destinationQuad);
            }
        }
    }

    boolean quadIsLabel(int quad) {
        Quad.Op op = quads.get(quad).getOp();
        return labels.contains(quad) || op == Quad.Op.ENDU;
    }

    boolean quadIsJump(int quad) {
        Quad.Op op = quads.get(quad).getOp();
        return op == Quad.Op.JUMP ||  op == Quad.Op.CALL || op == Quad.Op.RET ||
               op == Quad.Op.EQUAL || op == Quad.Op.NOT_EQUAL ||
               op == Quad.Op.GREATER || op == Quad.Op.LESS ||
               op == Quad.Op.GREATER_EQUAL || op == Quad.Op.LESS_EQUAL ||
               op == Quad.Op.ENDU;
        }

    public void print(int quadIndex, int tempVarIndex) {
        for (ListIterator<Quad> it = quads.listIterator(quadIndex) ; it.hasNext() ; ) {
            Quad quad = it.next();
            if (quad.getOp() == Quad.Op.UNIT) {
                System.out.println("");
            }
            System.out.println(it.previousIndex() + ": " + quad);
        }
    }

    public void print() {
        print(0, 0);
    }

    public ArrayList<Quad> getQuads() {
        return quads;
    }

    public ArrayList<Type> getTempVars() {
        return tempVars;
    }

    public HashMap<Integer, ArrayInfo> getArrayInfo() {
        return arrayInfo;
    }

    public HashSet<Integer> getLabels() {
        return labels;
    }
}
