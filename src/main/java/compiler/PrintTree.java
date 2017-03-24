package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

public class PrintTree extends DepthFirstAdapter {
    @Override
    public void defaultIn(Node node) {
        System.out.println(node.getClass() + ": " + node);
    }
}
