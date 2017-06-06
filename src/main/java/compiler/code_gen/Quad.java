package compiler.code_gen;

import java.lang.String;

public class Quad {
    public enum Op {
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
        String string = opToString(op) + ", " + (operand1 == null ? "-" : operand1) + ", "
                        + (operand2 == null ? "-" : operand2) + ", ";
        if (op == Quad.Op.ARRAY) {
            string += "$" + output.getTempVar();
        } else {
            string += (output == null ? "-" : output);
        }
        return string;
    }

    public Op getOp() {
        return op;
    }

    public QuadOperand getOperand1() {
        return operand1;
    }

    public QuadOperand getOperand2() {
        return operand2;
    }

    public QuadOperand getOutput() {
        return output;
    }
}
