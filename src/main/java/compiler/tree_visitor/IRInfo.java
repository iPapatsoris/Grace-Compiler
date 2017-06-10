package compiler.tree_visitor;

import java.lang.String;

public class IRInfo {

    public enum Type {
        TEMPVAR, ADDRESS, IDENTIFIER, STRING, CHAR, INT
    }

    private IRInfo.Type type;
    private int tempVar;
    private final String identifier;

    public IRInfo(IRInfo.Type type, int tempVar) {
        this.type = type;
        this.tempVar = tempVar;
        this.identifier = "";
    }

    public IRInfo(IRInfo.Type type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = identifier;
    }

    public IRInfo.Type getType() {
        return type;
    }

    public int getTempVar() {
        return tempVar;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setType(IRInfo.Type type) {
        this.type = type;
    }

    public void setTempVar(int tempVar) {
        this.tempVar = tempVar;
    }

    @Override
    public String toString() {
        return type + " " + tempVar + " " + identifier;
    }
}
