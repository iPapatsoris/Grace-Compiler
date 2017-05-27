package compiler;

import compiler.node.*;

import java.util.Collections;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ListIterator;
import java.lang.String;

class IntermediateRepresentation {
    private ArrayList<Quad> quads;
    private ArrayList<Type> tempVars;

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

    public void print() {
        for (ListIterator it = tempVars.listIterator() ; it.hasNext() ; ) {
            System.out.println("$" + it.nextIndex() + " " + it.next());
        }
        System.out.println("");
        for (ListIterator it = quads.listIterator() ; it.hasNext() ; ) {
            System.out.println(it.nextIndex() + ": " + it.next());
        }
    }
}

class Quad {
    enum Op {
        UNIT, ENDU, ASSIGN, ARRAY, IFB,
        JUMP, LABEL, JUMPL, CALL, PAR, RET,
        ADD, SUB, MULT, DIV, MOD,
        EQUAL, NOT_EQUAL, GREATER, LESS,
        GREATER_EQUAL, LESS_EQUAL
    }

    private Op op;
    private QuadOperand operand1;
    private QuadOperand operand2;
    private QuadOperand output;

    public Quad(Op op, QuadOperand operand1, QuadOperand operand2, QuadOperand output) {
        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.output = output;
    }

    void setOutput(QuadOperand output) {
        this.output = output;
    }

    private String opToString(Op op) {
        switch (op) {
            case ASSIGN: return ":=";
            case ADD: return "+";
            case SUB: return "-";
            case MULT: return "*";
            case DIV: return "/";
            case MOD: return "%";
            case EQUAL: return "=";
            case NOT_EQUAL: return "#";
            case GREATER: return ">";
            case LESS: return "<";
            case GREATER_EQUAL: return ">=";
            case LESS_EQUAL: return "<=";
            default: return op.toString().toLowerCase();
        }
    }

    @Override
    public String toString() {
        return opToString(op) + ", " + (operand1 == null ? "-" : operand1) + ", "
                        + (operand2 == null ? "-" : operand2) + ", " + (output == null ? "-" : output);
    }
}

class QuadOperand {
    enum Type {
        TEMPVAR, IDENTIFIER, ADDRESS, RETCALLED, RETCALLER, BACKPATCH, LABEL, V, R
    }

    private Type type;
    private int tempVar;
    private String identifier;

    public QuadOperand(IRInfo irInfo) {
        switch (irInfo.getType()) {
            case TEMPVAR:
                this.type = Type.TEMPVAR;
                this.tempVar = irInfo.getTempVar();
                this.identifier = null;
                break;
            case ADDRESS:
                this.type = Type.ADDRESS;
                this.tempVar = irInfo.getTempVar();
                this.identifier = null;
                break;
            case IDENTIFIER:
                this.type = Type.IDENTIFIER;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            default:
                System.err.println("Internal error: unexpected enum type in QuadOperand");
                System.exit(1);
        }
    }

    public QuadOperand(Type type, int tempVar) {
        this.type = type;
        this.tempVar = tempVar;
        this.identifier = null;
    }

    public QuadOperand(Type type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = identifier;
    }

    public QuadOperand(Type type) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = null;
    }

    @Override
    public String toString() {
        switch (type) {
            case TEMPVAR:
                return "$" + tempVar;
            case ADDRESS:
                return "[$" + tempVar + "]";
            case LABEL:
                return String.valueOf(tempVar);
            case RETCALLED:
                return "$$";
            case RETCALLER:
                return "RET";
            case BACKPATCH:
                return "*";
            case IDENTIFIER:
                return identifier;
            default:
                return type.toString();
        }
    }
}
