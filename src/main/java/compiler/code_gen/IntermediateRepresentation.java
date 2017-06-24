package compiler.code_gen;

import compiler.symbol_table.Type;
import compiler.node.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.lang.String;

public class IntermediateRepresentation {
    private final ArrayList<Quad> quads;
    private final ArrayList<Type> tempVars;

    public IntermediateRepresentation() {
        quads = new ArrayList<Quad>();
        tempVars = new ArrayList<Type>();
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
        }
    }

    public void print(int quadIndex, int tempVarIndex) {
        /* for (ListIterator it = tempVars.listIterator(tempVarIndex) ; it.hasNext() ; ) {
            System.out.println("$" + it.nextIndex() + " " + it.next());
            ArrayInfo arrayVar = arrayInfo.get(it.previousIndex());
            if (arrayVar != null) {
                System.out.println(" with " + arrayVar.getDimensionsLeft() +
                                   " dimensions left " + " and array type of " +
                                   arrayVar.getArrayType());
            }
        }
        System.out.println(""); */
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
}
