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
        for (ListIterator it = toBackpatch.listIterator() ; it.hasNext() ; ) {
            quads.get(it.nextIndex()).setOutput(destinationQuad);
            it.next();
        }
    }

    public void print() {
        for (ListIterator it = tempVars.listIterator() ; it.hasNext() ; ) {
            System.out.println("$" + it.nextIndex() + " " + it.next());
        }
        System.out.println("");
        for (Iterator it = quads.iterator() ; it.hasNext() ; ) {
            System.out.println(it.next());
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
    private int output;

    public Quad(Op op, QuadOperand operand1, QuadOperand operand2, int output) {
        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.output = output;
    }

    void setOutput(int output) {
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
        String string = opToString(op) + ", " + (operand1 == null ? "-" : operand1) + ", "
                        + (operand2 == null ? "-" : operand2) + ", ";
        if (output == -2) {
            string += "-";
        }
        else if (output == -1) {
            string += "*";
        }
        else {
            string += "$" + String.valueOf(output);
        }
        return string;
    }
}

class QuadOperand {
    enum Type {
        TEMPVAR, IDENTIFIER
    }

    private Type type;
    private int tempVar;
    private boolean pointer;
    private boolean address;
    private String identifier;

    public QuadOperand(IRInfo irInfo) {
        switch (irInfo.getType()) {
            case TEMPVAR:
                this.type = irInfo.getType();
                this.tempVar = irInfo.getTempVar();
                this.pointer = false;
                this.address = false;
                this.identifier = null;
                break;
            case IDENTIFIER:
            this.type = irInfo.getType();
                this.tempVar = -1;
                this.pointer = false;
                this.address = false;
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
        this.pointer = false;
        this.address = false;
        this.identifier = null;
    }

    public QuadOperand(Type type, int tempVar, boolean pointer, boolean address) {
        this.type = type;
        this.tempVar = tempVar;
        this.pointer = pointer;
        this.address = address;
        this.identifier = null;
    }

    public QuadOperand(Type type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.pointer = false;
        this.address = false;
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        switch (type) {
            case TEMPVAR:
                if (pointer) {
                    return "[$" + tempVar + "]";
                } else if (address) {
                    return "{$" + tempVar + "}";
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
