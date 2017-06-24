package compiler.code_gen;

import compiler.symbol_table.Type;
import java.util.HashMap;

public class ArrayInfo {
    private final Type arrayType;

    public ArrayInfo(Type arrayType) {
        this.arrayType = arrayType;
    }

    public Type getArrayType() {
        return arrayType;
    }

}
