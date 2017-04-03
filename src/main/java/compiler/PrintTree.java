package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

public class PrintTree extends DepthFirstAdapter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Override
    public void defaultIn(Node node) {
        System.out.println(ANSI_RED + node.getClass() + ": " + ANSI_RESET + node);
    }
}
