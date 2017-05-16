package compiler;

import compiler.node.*;

import java.lang.String;
import java.util.ArrayList;

enum Type {
    INT, CHAR, NOTHING
}

abstract class Symbol {
    private Token token;
    private Type type;

    public Symbol(Token token, Type type) {
        this.token = token;
        this.type = type;
    }

    public Token getToken() {
        return token;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return token + " " + type;
    }

}

class Variable extends Symbol {
    ArrayList<Integer> dimensions;

    public Variable(Token token, Type type, ArrayList<Integer> dimensions) {
        super(token, type);
        this.dimensions = dimensions;
    }

    ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    @Override
    public String toString() {
        return super.toString() + " " + dimensions;
    }

}

class Argument extends Variable {
    boolean reference;
    boolean noFirstDimension;

    public Argument(Token token, Type type, ArrayList<Integer> dimensions, boolean reference, boolean noFirstDimension) {
        super(token, type, dimensions);
        this.reference = reference;
        this.noFirstDimension = noFirstDimension;
    }

    boolean isReference() {
        return reference;
    }

    boolean hasNoFirstDimension() {
        return noFirstDimension;
    }

    @Override
    public String toString() {
        return super.toString() + " " + reference + " " + noFirstDimension;
    }
}
