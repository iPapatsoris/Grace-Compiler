package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

import java.util.Collections;
import java.lang.String;


public class PrintTree extends DepthFirstAdapter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private int indentation = 0;
    private void addIndentationLevel() {
        indentation++;
    }

    private void removeIndentationLevel() {
        indentation--;
    }

    private void printIndentation() {
        System.out.print(String.join("", Collections.nCopies(indentation, " ")));
    }

    private String getClassName(Node node) {
        String nodeClass = node.getClass().toString();
        int suffixIndex = nodeClass.lastIndexOf('.');
        return nodeClass.substring(suffixIndex+2);
    }

    /* To be used by nodes without pure tokens as attributes */
    private void printNode(Node node) {
        printIndentation();
        String nodeClass = node.getClass().toString();
        int suffixIndex = nodeClass.lastIndexOf('.');
        System.out.println(nodeClass.substring(suffixIndex+2));
        addIndentationLevel();
    }

    private void printLeaf(Node node) {
        printIndentation();
        System.out.println(node);
    }

    @Override
    public void defaultIn(Node node) {
        System.out.println(ANSI_RED + node.getClass() + ": " + ANSI_RESET + node);
    }

/*    @Override
    public void defaultOut(Node node) {
        removeIndentationLevel();
    }
*/

/* In */

    @Override
    public void inAIdentifierLValue(AIdentifierLValue node) {
        printIndentation();
        System.out.println(getClassName(node) + " " + node.getIdentifier());
        addIndentationLevel();
    }

    @Override
    public void inAStringLValue(AStringLValue node) {
        printIndentation();
        System.out.println(getClassName(node) + " " + node.getString());
        addIndentationLevel();
    }

    @Override
    public void inALValueExpr(ALValueExpr node) {
        printNode(node);
    }

    @Override
    public void inAAddExpr(AAddExpr node) {
        printNode(node);
    }

    @Override
    public void inASubExpr(ASubExpr node) {
        printNode(node);
    }

    @Override
    public void inAMultExpr(AMultExpr node) {
        printNode(node);
    }

    @Override
    public void inADivExpr(ADivExpr node) {
        printNode(node);
    }

    @Override
    public void inAModExpr(AModExpr node) {
        printNode(node);
    }

    @Override
    public void inAPositiveExpr(APositiveExpr node) {
        printNode(node);
    }

    @Override
    public void inANegativeExpr(ANegativeExpr node) {
        printNode(node);
    }

    @Override
    public void inAIntConstantExpr(AIntConstantExpr node) {
        printLeaf(node);
    }

    @Override
    public void inACharConstantExpr(ACharConstantExpr node) {
        printLeaf(node);
    }

/* Out */

    @Override
    public void outAIdentifierLValue(AIdentifierLValue node) {
        removeIndentationLevel();
    }

    @Override
    public void outAStringLValue(AStringLValue node) {
        removeIndentationLevel();
    }

    @Override
    public void outAAddExpr(AAddExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outASubExpr(ASubExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outAMultExpr(AMultExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outADivExpr(ADivExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outAModExpr(AModExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outAPositiveExpr(APositiveExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outANegativeExpr(ANegativeExpr node) {
        removeIndentationLevel();
    }


    @Override
    public void outAIntConstantExpr(AIntConstantExpr node) {

    }

    @Override
    public void outACharConstantExpr(ACharConstantExpr node) {

    }

}
