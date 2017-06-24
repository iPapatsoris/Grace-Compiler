package compiler.symbol_table;

import compiler.node.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.ListIterator;

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
        return getTotalCells(0);
    }

    public long getTotalCells(int start) {
        if (dimensions.size() == 0) {
            return 0;
        }
        long cells = dimensions.get(start);
        if (dimensions.size() == start + 1) {
            return cells;
        }
        for (ListIterator<Integer> it = dimensions.listIterator(start + 1) ; it.hasNext() ;) {
            cells *= it.next();
        }
        return cells;
    }

    @Override
    public String toString() {
        return super.toString() + " " + dimensions;
    }
}
