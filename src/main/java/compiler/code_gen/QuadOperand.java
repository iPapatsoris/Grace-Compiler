package compiler.code_gen;

import compiler.tree_visitor.IRInfo;
import compiler.tree_visitor.IRInfoType;
import java.lang.String;

public class QuadOperand {
    private QuadOperandType type;
    private int tempVar;
    private String identifier;

    public QuadOperand(IRInfo irInfo) {
        switch (irInfo.getType()) {
            case TEMPVAR:
                this.type = QuadOperandType.TEMPVAR;
                this.tempVar = irInfo.getTempVar();
                this.identifier = null;
                break;
            case ADDRESS:
                this.type = QuadOperandType.ADDRESS;
                this.tempVar = irInfo.getTempVar();
                this.identifier = null;
                break;
            case IDENTIFIER:
                this.type = QuadOperandType.IDENTIFIER;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            case INT:
                this.type = QuadOperandType.INT;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            case CHAR:
                this.type = QuadOperandType.CHAR;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            case STRING:
                this.type = QuadOperandType.STRING;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            default:
                System.err.println("Internal error: unexpected enum type in QuadOperand");
                System.exit(1);
        }
    }

    public QuadOperand(QuadOperandType type, int tempVar) {
        this.type = type;
        this.tempVar = tempVar;
        this.identifier = null;
    }

    public QuadOperand(QuadOperandType type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = identifier;
    }

    public QuadOperand(QuadOperandType type) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = null;
    }

    public QuadOperandType getType() {
        return type;
    }

    public int getTempVar() {
        return tempVar;
    }

    public String getIdentifier() {
        return identifier;
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
            case INT:
                return identifier;
            case STRING:
                return identifier;
            case CHAR:
                return identifier;

            default:
                return type.toString();
        }
    }
}
