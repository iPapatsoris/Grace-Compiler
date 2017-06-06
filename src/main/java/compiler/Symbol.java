package compiler;

import compiler.node.*;

import java.lang.String;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;

enum Type {
    INT, CHAR, NOTHING
}

abstract class Symbol {
    protected Token token;
    protected Type type;

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

class Variable extends Symbol {
    protected ArrayList<Integer> dimensions;

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
    private boolean reference;
    private boolean noFirstDimension;

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

    /* TODO: Implement Object.hashCode */
    @Override
    public boolean equals(Object object) {
        Argument argument = (Argument)object;
        return this.type == argument.type
               && this.dimensions.equals(argument.dimensions)
               && this.reference == argument.reference
               && this.noFirstDimension == argument.noFirstDimension;
    }

    @Override
    public String toString() {
        return super.toString() + " " + reference + " " + noFirstDimension;
    }
}

class Function extends Symbol {
    private ArrayDeque<Argument> arguments;
    private boolean defined;

    public Function(Token token, ArrayDeque<Argument> arguments, Type type, boolean defined) {
        super(token, type);
        this.arguments = arguments;
        this.defined = defined;
    }

    public ArrayDeque<Argument> getArguments() {
        return arguments;
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean sameHeader(Function function) {
        return this.token.getText().equals(function.token.getText())
               && this.type == function.type
               && equalDeque(this.arguments, function.arguments);
    }

    @Override
    public String toString() {
        return super.toString() + " " + defined + " " + arguments ;
    }

    /* Dequeue does not implement Object.equals . Either implement ArrayDeque or use this method */
    private static boolean equalDeque(ArrayDeque<Argument> queue1, ArrayDeque<Argument> queue2) {
        if (queue1.size() != queue2.size()) {
            return false;
        }

        Iterator<Argument> it2 = queue2.iterator();
        for (Iterator<Argument> it1 = queue1.iterator(); it1.hasNext(); ) {
            if (! it1.next().equals(it2.next())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStandardLibrary(String function) {
        return function.equals("_puti_-1") || function.equals("_putc_-1") || function.equals("_puts_-1") ||
               function.equals("_geti_-1") || function.equals("_getc_-1") || function.equals("_gets_-1") ||
               function.equals("_abs_-1") || function.equals("_ord_-1") || function.equals("_chr_-1") ||
               function.equals("_strlen_-1") || function.equals("_strcpy_-1") || function.equals("_strcmp_-1") ||
               function.equals("_strcat_-1");
    }
}
