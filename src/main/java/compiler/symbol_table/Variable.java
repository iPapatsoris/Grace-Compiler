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
        if (dimensions.size() == 0) {
            return 0;
        }
        long cells = dimensions.get(0);
        if (dimensions.size() == 1) {
            return cells;
        }
        for (ListIterator<Integer> it = dimensions.listIterator(1) ; it.hasNext() ;) {
            cells *= it.next();
        }
        return cells;
    }

    @Override
    public String toString() {
        return super.toString() + " " + dimensions;
    }
}
