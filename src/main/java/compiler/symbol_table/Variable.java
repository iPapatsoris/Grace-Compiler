package compiler.symbol_table;

import compiler.node.*;
import java.lang.String;
import java.util.ArrayList;

public class Variable extends Symbol {
    protected final ArrayList<Integer> dimensions;

    public Variable(Token token, Type type, ArrayList<Integer> dimensions) {
        super(token, type);
        this.dimensions = dimensions;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }

    public long getTotalCells() {
        long cells = 0;
        for (Integer dimension: dimensions) {
            cells += dimension;
        }
        return cells;
    }

    @Override
    public String toString() {
        return super.toString() + " " + dimensions;
    }
}
