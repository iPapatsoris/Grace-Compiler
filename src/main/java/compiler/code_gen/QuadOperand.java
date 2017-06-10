package compiler.code_gen;

import compiler.tree_visitor.IRInfo;
import compiler.tree_visitor.IRInfo.Type;
import java.lang.String;

public class QuadOperand {

    public enum Type {
        TEMPVAR, IDENTIFIER, ADDRESS, RETCALLED, RETCALLER, BACKPATCH, LABEL, V, R,
        STRING, CHAR, INT
    }

    private final QuadOperand.Type type;
    private final int tempVar;
    private final String identifier;

    public QuadOperand(IRInfo irInfo) {
        switch (irInfo.getType()) {
            case TEMPVAR:
                this.type = QuadOperand.Type.TEMPVAR;
                this.tempVar = irInfo.getTempVar();
                this.identifier = null;
                break;
            case ADDRESS:
                this.type = QuadOperand.Type.ADDRESS;
                this.tempVar = irInfo.getTempVar();
                this.identifier = null;
                break;
            case IDENTIFIER:
                this.type = QuadOperand.Type.IDENTIFIER;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            case INT:
                this.type = QuadOperand.Type.INT;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            case CHAR:
                this.type = QuadOperand.Type.CHAR;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            case STRING:
                this.type = QuadOperand.Type.STRING;
                this.tempVar = -1;
                this.identifier = irInfo.getIdentifier();
                break;
            default:
                this.type = null;
                this.tempVar = -1;
                this.identifier = null;
                System.err.println("Internal error: unexpected enum type in QuadOperand");
                System.exit(1);
        }
    }

    public QuadOperand(QuadOperand.Type type, int tempVar) {
        this.type = type;
        this.tempVar = tempVar;
        this.identifier = null;
    }

    public QuadOperand(QuadOperand.Type type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = identifier;
    }

    public QuadOperand(QuadOperand.Type type) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = null;
    }

    public QuadOperand.Type getType() {
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
