package compiler.tree_visitor;

import java.lang.String;

public class IRInfo {
    IRInfoType type;
    private int tempVar;
    private String identifier;

    public IRInfo(IRInfoType type, int tempVar) {
        this.type = type;
        this.tempVar = tempVar;
        this.identifier = "";
    }

    public IRInfo(IRInfoType type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = identifier;
    }

    public IRInfoType getType() {
        return type;
    }

    public int getTempVar() {
        return tempVar;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setType(IRInfoType type) {
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
