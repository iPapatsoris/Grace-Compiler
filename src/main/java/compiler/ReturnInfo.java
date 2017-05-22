/* This collection of classes is used to carry
 * information when returning from nested AST nodes */

package compiler;

import compiler.node.*;

import java.util.ArrayList;
import java.util.ArrayDeque;

abstract class ReturnInfo {
    protected IRInfo irInfo; // For intermediate representation
                             // reduce to subclasses later if found possible
    public ReturnInfo() {
        irInfo = null;
    }

    public IRInfo getIRInfo() {
        return irInfo;
    }
}

class VariableInfo extends ReturnInfo {
    private Type type;
    private ArrayList<Integer> dimensions;

    VariableInfo(Type type, ArrayList<Integer> dimensions) {
        super();
        this.type = type;
        this.dimensions = dimensions;
    }

    public Type getType() {
        return type;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }
}

class ArgumentInfo extends ReturnInfo {
    private ArrayDeque<Token> identifiers;
    private Type type;
    private boolean reference;
    private ArrayList<Integer> dimensions;
    private boolean noFirstDimension;

    public ArgumentInfo(Type type, ArrayList<Integer> dimensions, boolean noFirstDimension) {
        super();
        this.type = type;
        this.dimensions = dimensions;
        this.noFirstDimension = noFirstDimension;
    }

    public void setIdentifiers(ArrayDeque<Token> identifiers) {
        this.identifiers = identifiers;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

    public ArrayDeque<Token> getIdentifiers() {
        return identifiers;
    }

    public Type getType() {
        return type;
    }

    public boolean hasReference() {
        return reference;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    public boolean hasNoFirstDimension() {
        return noFirstDimension;
    }
}

class FunctionInfo extends ReturnInfo {
    private Token token;
    private ArrayDeque<ArgumentInfo> arguments;
    private Type type;
    private boolean foundReturn;

    public FunctionInfo(Token token, ArrayDeque<ArgumentInfo> arguments, Type type) {
        super();
        this.token = token;
        this.arguments = arguments;
        this.type = type;
        this.foundReturn = false;
    }

    public Token getToken() {
        return token;
    }

    public ArrayDeque<ArgumentInfo> getArguments() {
        return arguments;
    }

    public void setArguments(ArrayDeque<ArgumentInfo> arguments) {
        this.arguments = arguments;
    }

    public Type getType() {
        return type;
    }

    public boolean getFoundReturn() {
        return foundReturn;
    }

    public void setFoundReturn(boolean foundReturn) {
        this.foundReturn = foundReturn;
    }

}

class ExprInfo extends ReturnInfo {
    private Type type;
    private boolean negative;
    private boolean lvalue;
    private ArrayList<Integer> dimensions;
    private Token token; // For error printing

    ExprInfo(Type type, Token token) {
        super();
        this.token = token;
        this.type = type;
        this.negative = false;
        this.lvalue = false;
        this.dimensions = new ArrayList<Integer>();
    }

    ExprInfo(Type type, Token token, IRInfo irInfo) {
        this(type, token);
        this.irInfo = irInfo;
    }

    ExprInfo(Type type, ArrayList<Integer> dimensions, Token token) {
        super();
        this.token = token;
        this.type = type;
        this.negative = false;
        this.lvalue = true;
        this.dimensions = dimensions;
    }

    public Token getToken() {
        return token;
    }

    public Type getType() {
        return type;
    }

    public boolean isNegative() {
        return negative;
    }

    public boolean isLvalue() {
        return lvalue;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    public void toggleNegative() {
        negative = !negative;
    }
}

class IRInfo {
    QuadOperand.Type type;
    private int tempVar;
    private String identifier;

    public IRInfo(QuadOperand.Type type, int tempVar) {
        this.type = type;
        this.tempVar = tempVar;
        this.identifier = "";
    }

    public IRInfo(QuadOperand.Type type, String identifier) {
        this.type = type;
        this.tempVar = -1;
        this.identifier = identifier;
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
        return type + " " + tempVar + " " + identifier;
    }
}
