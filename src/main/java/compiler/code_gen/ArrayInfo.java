package compiler.code_gen;

import compiler.symbol_table.Type;
import java.util.HashMap;

public class ArrayInfo {
    private final Type arrayType;
    private final int dimensionsLeft;

    public ArrayInfo(Type arrayType, int dimensionsLeft) {
        this.arrayType = arrayType;
        this.dimensionsLeft = dimensionsLeft;
    }

    public Type getArrayType() {
        return arrayType;
    }

    public int getDimensionsLeft() {
        return dimensionsLeft;
    }
}
