package compiler.symbol_table;

import compiler.node.*;

import java.lang.String;

public abstract class Symbol {
    protected final Token token;
    protected final Type type;

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

    public static String getLocation(Token token) {
        return (token.getLine() != -1 ?
        "[" + token.getLine() + "," + token.getPos() + "]" :
        "Standard Library");
    }

    public static String typeToString(Type type) {
        switch (type) {
            case INT     : return "int";
            case CHAR    : return "char";
            case NOTHING : return "nothing";
            default      : System.err.println("Internal error: wrong enum Type in typeToString()");
                           System.exit(1);
        }
        return "";
    }
}
