/* This collection of classes is used to carry
 * information when returning from nested AST nodes */

package compiler;

import java.util.ArrayList;

abstract class ReturnInfo {

}

class VariableInfo extends ReturnInfo {
    private Type type;
    private ArrayList<Integer> dimensions;

    VariableInfo(Type type, ArrayList<Integer> dimensions) {
        super();
        this.type = type;
        this.dimensions = dimensions;
    }

    public Type getType() {
        return type;
    }

    public ArrayList<Integer> getDimensions() {
        return dimensions;
    }
}
