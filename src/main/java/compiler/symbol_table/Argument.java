package compiler.symbol_table;

import compiler.node.*;
import java.lang.String;
import java.util.ArrayList;

public class Argument extends Variable {
    private final boolean reference;
    private final boolean noFirstDimension;

    public Argument(Token token, Type type, ArrayList<Integer> dimensions, boolean reference, boolean noFirstDimension) {
        super(token, type, dimensions);
        this.reference = reference;
        this.noFirstDimension = noFirstDimension;
    }

    public boolean isReference() {
        return reference;
    }

    public boolean hasNoFirstDimension() {
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
