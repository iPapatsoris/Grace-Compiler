/* This collection of classes is used to carry
 * information when returning from nested AST nodes */

package compiler;

import compiler.node.*;

import java.util.ArrayList;
import java.util.ArrayDeque;

abstract class ReturnInfo {

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
    private Type returnType;

    public FunctionInfo(Token token, ArrayDeque<ArgumentInfo> arguments, Type returnType) {
        this.token = token;
        this.arguments = arguments;
        this.returnType = returnType;
    }

    public Token getToken() {
        return token;
    }

    public ArrayDeque<ArgumentInfo> getArguments() {
        return arguments;
    }

    public Type getReturnType() {
        return returnType;
    }

}

class ExprInfo extends ReturnInfo {
    private Type type;
    private boolean negative;
    private boolean lvalue;
    private ArrayList<Integer> dimensions;
    private Token token; // For error printing

    ExprInfo(Type type, Token token) {
        this.token = token;
        this.type = type;
        this.negative = false;
        this.lvalue = false;
        this.dimensions = null;
    }

    ExprInfo(Type type, ArrayList<Integer> dimensions, Token token) {
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
