package compiler;

import compiler.node.*;

import java.util.Collections;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.lang.String;

class IntermediateRepresentation {
    private ArrayList<Quad> quads;

    public IntermediateRepresentation() {
        quads = new ArrayList<Quad>();
    }

    public void print() {
        for (Iterator it = quads.iterator() ; it.hasNext() ; ) {
            System.out.println(it);
        }
    }
}

class Quad {
    enum Op {
        UNIT, ENDU, ASSIGN, ARRAY, IFB,
        JUMP, LABEL, JUMPL, CALL, PAR, RET,
        PLUS, MINUS, TIMES, DIV, MOD,
        EQUAL, NOT_EQUAL, GREATER, LESS,
        GREATER_EQUAL, LESS_EQUAL
    }

    private Op op;
    private QuadOperand operand1;
    private QuadOperand operand2;
    private long output;

    public Quad(Op op, QuadOperand operand1, QuadOperand operand2, long output) {
        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.output = output;
    }

    private String opToString(Op op) {
        switch (op) {
            case ASSIGN: return ":=";
            case PLUS: return "+";
            case MINUS: return "-";
            case TIMES: return "*";
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
            string += String.valueOf(output);
        }
        return string;
    }
}

abstract class QuadOperand {
}

class QuadOperandString {
    private String identifier;

    public QuadOperandString(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}

class QuadOperandValue {
    private long value;
    private boolean pointer;
    private boolean address;

    public QuadOperandValue(long value) {
        this.value = value;
        this.pointer = false;
        this.address = false;
    }

    public QuadOperandValue(long value, boolean pointer, boolean address) {
        this.value = value;
        this.pointer = pointer;
        this.address = address;
    }

    @Override
    public String toString() {
        if (pointer) {
            return "[$" + value + "]";
        } else if (address) {
            return "{$" + value + "}";
        }
        return "";
    }
}
