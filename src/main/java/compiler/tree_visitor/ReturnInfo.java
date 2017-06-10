/* This collection of classes is used to carry
 * information when returning from nested AST nodes */

package compiler.tree_visitor;

import compiler.symbol_table.Type;
import compiler.node.*;

import java.util.ArrayList;
import java.util.ArrayDeque;

abstract class ReturnInfo {
}

class VariableInfo extends ReturnInfo {
    private final Type type;
    private final ArrayList<Integer> dimensions;

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
    private final Type type;
    private boolean reference;
    private final ArrayList<Integer> dimensions;
    private final boolean noFirstDimension;

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
    private final Token token;
    private ArrayDeque<ArgumentInfo> arguments;
    private final Type type;
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
    private final IRInfo irInfo;
    private final Type type;
    private boolean negative;
    private final boolean lvalue;
    private final ArrayList<Integer> dimensions;
    private final Token token; // For error printing

    ExprInfo(Type type, Token token) {
        this.token = token;
        this.type = type;
        this.negative = false;
        this.lvalue = false;
        this.dimensions = new ArrayList<Integer>();
        this.irInfo = null;
    }

    ExprInfo(Type type, Token token, IRInfo irInfo) {
        this.token = token;
        this.type = type;
        this.negative = false;
        this.lvalue = false;
        this.dimensions = new ArrayList<Integer>();
        this.irInfo = irInfo;
    }

    ExprInfo(Type type, ArrayList<Integer> dimensions, Token token, IRInfo irInfo) {
        this.token = token;
        this.type = type;
        this.negative = false;
        this.lvalue = true;
        this.dimensions = dimensions;
        this.irInfo = irInfo;
    }

    public IRInfo getIRInfo() {
        return irInfo;
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

class BackpatchInfo extends ReturnInfo {
    private ArrayList<Integer> falseList;
    private ArrayList<Integer> trueList;
    private final ArrayList<Integer> nextList;

    BackpatchInfo(ArrayList<Integer> falseList, ArrayList<Integer> trueList) {
        this.falseList = falseList;
        this.trueList = trueList;
        this.nextList = null;
    }

    BackpatchInfo(ArrayList<Integer> nextList) {
        this.falseList = null;
        this.trueList = null;
        this.nextList = nextList;
    }

    public ArrayList<Integer> getFalseList() {
        return falseList;
    }

    public ArrayList<Integer> getNextList() {
        return nextList;
    }

    public ArrayList<Integer> getTrueList() {
        return trueList;
    }

    public void setFalseList(ArrayList<Integer> falseList) {
        this.falseList = falseList;
    }

    public void setTrueList(ArrayList<Integer> trueList) {
        this.trueList = trueList;
    }

}
