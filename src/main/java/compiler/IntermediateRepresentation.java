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
            quads.get(quad).setOutput(new QuadOutput(QuadOutput.Type.LABEL, destinationQuad));
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
    private QuadOutput output;

    public Quad(Op op, QuadOperand operand1, QuadOperand operand2, QuadOutput output) {
        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.output = output;
    }

    void setOutput(QuadOutput output) {
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
        TEMPVAR, IDENTIFIER
    }

    private Type type;
    private int tempVar;
    private boolean isAddress;
    private String identifier;

    public QuadOperand(IRInfo irInfo) {
        switch (irInfo.getType()) {
            case TEMPVAR:
                this.type = irInfo.getType();
                this.tempVar = irInfo.getTempVar();
                this.isAddress = irInfo.isAddress();
                this.identifier = null;
                break;
            case IDENTIFIER:
                this.type = irInfo.getType();
                this.tempVar = -1;
                this.isAddress = irInfo.isAddress();
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
        this.isAddress = false;
        this.identifier = null;
    }

    public QuadOperand(Type type, int tempVar, boolean isAddress) {
        this.type = type;
        this.tempVar = tempVar;
        this.isAddress = isAddress;
        this.identifier = null;
    }

    public QuadOperand(Type type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.isAddress = false;
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        switch (type) {
            case TEMPVAR:
                if (isAddress) {
                    return "[$" + tempVar + "]";
                } else {
                    return "$" + tempVar;
                }
            case IDENTIFIER:
                return identifier;
            default:
                return "";
        }
    }
}

class QuadOutput {
    enum Type {
        NORMAL, ADDRESS, RET, BACKPATCH, LABEL
    }

    private Type type;
    private int tempVar;

    public QuadOutput(IRInfo irInfo) {
        assert(irInfo.getType() == QuadOperand.Type.TEMPVAR);
        this.type = (irInfo.isAddress() ? Type.ADDRESS : Type.NORMAL);
        this.tempVar = irInfo.getTempVar();
    }

    public QuadOutput(Type type, int tempVar) {
        assert(type != Type.RET && type != Type.BACKPATCH);
        this.type = type;
        this.tempVar = tempVar;
    }

    public QuadOutput(Type type) {
        assert(type == Type.RET || type == Type.BACKPATCH);
        this.type = type;
        this.tempVar = -1;
    }

    @Override
    public String toString() {
        switch (type) {
            case NORMAL:
                return "$" + tempVar;
            case ADDRESS:
                return "[$" + tempVar + "]";
            case LABEL:
                return String.valueOf(tempVar);
            case RET:
                return "$$";
            case BACKPATCH:
                return "*";
            default:
                return "";
        }
    }
}
