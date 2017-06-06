package compiler.symbol_table;

import compiler.node.*;
import java.lang.String;
import java.util.ArrayList;

public class Variable extends Symbol {
    protected ArrayList<Integer> dimensions;

    public Variable(Token token, Type type, ArrayList<Integer> dimensions) {
        super(token, type);
        this.dimensions = dimensions;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    @Override
    public String toString() {
        return super.toString() + " " + dimensions;
    }
}
