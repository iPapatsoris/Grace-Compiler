package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ArrayDeque;
import java.util.List;
import java.lang.String;


class TreeVisitor extends DepthFirstAdapter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private SymbolTable symbolTable;
    private ArrayDeque<ReturnInfo> returnInfo;
    private int indentation;

    public TreeVisitor() {
        symbolTable = new SymbolTable();
        returnInfo = new ArrayDeque<ReturnInfo>();
        indentation = 0;
    }

    private void addIndentationLevel() {
        indentation++;
    }

    private void removeIndentationLevel() {
        indentation--;
    }

    private void printIndentation() {
        System.out.print(String.join("", Collections.nCopies(indentation, " ")));
    }

    private static String getClassName(Node node) {
        String nodeClass = node.getClass().toString();
        int suffixIndex = nodeClass.lastIndexOf('.');
        return nodeClass.substring(suffixIndex+2);
    }

    private static Type convertNodeToType(Node node) {
        switch (getClassName(node)) {
            case "IntDataType"     : return Type.INT;
            case "CharDataType"    : return Type.CHAR;
            case "NothingDataType" : return Type.NOTHING;
            default                : System.err.println("Internal error in convertNodeToType");
                                     System.exit(1);
        }
        return Type.NOTHING;
    }

    private static ArrayList<Integer> convertTokensToNumbers(LinkedList<TIntConstant> tokens) {
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        for (TIntConstant token : tokens) {
            numbers.add(Integer.parseInt(token.getText()));
        }
        return numbers;
    }

    /* Generic printing that does not print pure token attributes */
    private void printNode(Node node) {
        printIndentation();
        String nodeClass = node.getClass().toString();
        int suffixIndex = nodeClass.lastIndexOf('.');
        System.out.println(nodeClass.substring(suffixIndex+2));
        addIndentationLevel();
    }

    private void printTokenVal(Node node) {
        printIndentation();
        System.out.println(node);
    }

    @Override
    public void defaultIn(Node node) {
        System.out.println(ANSI_RED + node.getClass() + " " + ANSI_RESET + node);
    }

    /* ******** In ******** */

    @Override
    public void inAFuncDefLocalDef(AFuncDefLocalDef node) {
        printNode(node);
    }

    @Override
    public void inAFuncDeclLocalDef(AFuncDeclLocalDef node) {
        printNode(node);
    }

    @Override
    public void inAVarDefLocalDef(AVarDefLocalDef node) {
        printNode(node);
    }

    @Override
    public void inAFuncDef(AFuncDef node) {
        printNode(node);
    }

    @Override
    public void inAHeader(AHeader node) {
        printIndentation();
        System.out.println(getClassName(node) + ":  " + node.getIdentifier());
        addIndentationLevel();
    }

    @Override
    public void inAFparDef(AFparDef node) {
        printIndentation();
        System.out.println(getClassName(node) + ":  " +
            (node.getRef() != null ? "ref " : "")  + node.getIdentifier());
        addIndentationLevel();
    }

    @Override
    public void inAFparType(AFparType node) {
        printIndentation();
        System.out.println(getClassName(node) + ":  " +
            (node.getLsquareBracket() != null ? "empty dimension, " : "")  + node.getIntConstant());
        addIndentationLevel();
    }

    @Override
    public void inAIntDataType(AIntDataType node) {
        printNode(node);
    }

    @Override
    public void inACharDataType(ACharDataType node) {
        printNode(node);
    }

    @Override
    public void inANothingDataType(ANothingDataType node) {
        printNode(node);
    }

    /* Variable */

    @Override
    public void inAVarDef(AVarDef node) {
        printIndentation();
        System.out.println(getClassName(node) + ":  " + node.getIdentifier());
        addIndentationLevel();
    }

    @Override
    public void inAVarType(AVarType node) {
        printIndentation();
        System.out.println(getClassName(node) + ":  " + node.getIntConstant());
        addIndentationLevel();
    }

    /* Statement */

    @Override
    public void inAIfStatement(AIfStatement node) {
        printIndentation();
        System.out.println(getClassName(node) + ": then " + node.getThen().size() + ", else " + node.getElse().size());
        addIndentationLevel();
    }

    @Override
    public void inAWhileStatement(AWhileStatement node) {
        printNode(node);
    }

    @Override
    public void inAAssignmentStatement(AAssignmentStatement node) {
        printNode(node);
    }

    @Override
    public void inAFuncCallStatement(AFuncCallStatement node) {
        printNode(node);
    }

    @Override
    public void inAReturnStatement(AReturnStatement node) {
        printNode(node);
    }

    @Override
    public void inANullStatement(ANullStatement node) {
        printNode(node);
    }

    /* Condition */

    @Override
    public void inADisjCond(ADisjCond node) {
        printNode(node);
    }

    @Override
    public void inAConjCond(AConjCond node) {
        printNode(node);
    }

    @Override
    public void inANegCond(ANegCond node) {
        printNode(node);
    }

    @Override
    public void inAEqualCond(AEqualCond node) {
        printNode(node);
    }

    @Override
    public void inANotEqualCond(ANotEqualCond node) {
        printNode(node);
    }

    @Override
    public void inAGreaterCond(AGreaterCond node) {
        printNode(node);
    }

    @Override
    public void inALessCond(ALessCond node) {
        printNode(node);
    }

    @Override
    public void inAGreaterEqualCond(AGreaterEqualCond node) {
        printNode(node);
    }

    @Override
    public void inALessEqualCond(ALessEqualCond node) {
        printNode(node);
    }

    /* Function call */

    @Override
    public void inAFuncCall(AFuncCall node) {
        printIndentation();
        System.out.println(getClassName(node) + ": " + node.getIdentifier());
        addIndentationLevel();
    }

   /* L_value */

    @Override
    public void inAIdentifierLValue(AIdentifierLValue node) {
        printIndentation();
        System.out.println(getClassName(node) + ": " + node.getIdentifier());
        addIndentationLevel();
    }

    @Override
    public void inAStringLValue(AStringLValue node) {
        printIndentation();
        System.out.println(getClassName(node) + ": " + node.getString());
        addIndentationLevel();
    }

    /* Expr */

    @Override
    public void inALValueExpr(ALValueExpr node) {
        printNode(node);
    }

    @Override
    public void inAFuncCallExpr(AFuncCallExpr node) {
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
        printTokenVal(node);
    }

    @Override
    public void inACharConstantExpr(ACharConstantExpr node) {
        printTokenVal(node);
    }

    @Override
    public void inStart(Start node) {}

    /* ******** Out & Case  ******** */

    @Override
    public void outAFuncDefLocalDef(AFuncDefLocalDef node) {
        removeIndentationLevel();
    }

    @Override
    public void outAFuncDeclLocalDef(AFuncDeclLocalDef node) {
        removeIndentationLevel();
    }

    @Override
    public void outAVarDefLocalDef(AVarDefLocalDef node) {
        removeIndentationLevel();
    }

    @Override
    public void outAFuncDef(AFuncDef node) {
        removeIndentationLevel();
        symbolTable.exit();
    }

    @Override
    public void caseAFuncDef(AFuncDef node)
    {
        inAFuncDef(node);
        if(node.getHeader() != null)
        {
            node.getHeader().apply(this);
            FunctionInfo functionInfo = (FunctionInfo) returnInfo.pop();

            /* Function symbol on current scope and arguments on new, unless it's main function */
            boolean mainFunction = symbolTable.onFirstScope();
            if (mainFunction) {
                symbolTable.enter();
            }

            /* Add function symbol on current scope */
            Symbol function = new Function(functionInfo.getToken(), functionInfo.getReturnType(), true);
            try {
                symbolTable.insert(function);
            } catch (SemanticException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }

            /* Add arguments on new scope */
            if (! mainFunction) {
                symbolTable.enter();
            }
            for (ArgumentInfo argumentInfo : functionInfo.getArguments()) {
                for (Token argument : argumentInfo.getIdentifiers()) {
                    try {
                        symbolTable.insert(new Argument(argument, argumentInfo.getType(), argumentInfo.getDimensions(), argumentInfo.hasReference(), argumentInfo.hasNoFirstDimension()));
                    } catch (SemanticException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
            }

            System.out.println("returnInfo size now: " + returnInfo.size());
        }
        {
            List<PLocalDef> copy = new ArrayList<PLocalDef>(node.getLocalDef());
            for(PLocalDef e : copy)
            {
                e.apply(this);
            }
        }
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getStatement());
            for(PStatement e : copy)
            {
                e.apply(this);
            }
        }
        outAFuncDef(node);
    }

    @Override
    public void outAHeader(AHeader node) {
        removeIndentationLevel();

        ArrayDeque<ArgumentInfo> arguments = new ArrayDeque<ArgumentInfo>();
        for (int argumentCount = node.getFparDef().size() ; argumentCount > 0 ; argumentCount--) {
            ArgumentInfo argumentInfo = (ArgumentInfo) returnInfo.pop();
            arguments.addFirst(argumentInfo);
        }
        ReturnInfo functionInfo = new FunctionInfo(node.getIdentifier(), arguments, convertNodeToType(node.getDataType()));
        returnInfo.push(functionInfo);
    }

    @Override
    public void outAFparDef(AFparDef node) {
        removeIndentationLevel();

        ArgumentInfo argumentInfo = (ArgumentInfo) returnInfo.peek();
        boolean reference =  (node.getRef() != null ? true : false);
        argumentInfo.setReference(reference);
        argumentInfo.setIdentifiers(new ArrayDeque(node.getIdentifier()));
    }

    @Override
    public void outAFparType(AFparType node) {
        removeIndentationLevel();

        boolean noFirstDimension = (node.getLsquareBracket() != null ? true : false);
        ArgumentInfo argumentInfo = new ArgumentInfo(convertNodeToType(node.getDataType()), convertTokensToNumbers(node.getIntConstant()), noFirstDimension);
        returnInfo.push(argumentInfo);
    }

    @Override
    public void outAIntDataType(AIntDataType node) {
        removeIndentationLevel();
    }

    @Override
    public void outACharDataType(ACharDataType node) {
        removeIndentationLevel();
    }

    @Override
    public void outANothingDataType(ANothingDataType node) {
        removeIndentationLevel();
    }

    /* Variable */

    @Override
    public void outAVarDef(AVarDef node) {
        removeIndentationLevel();

        VariableInfo variableInfo = (VariableInfo) returnInfo.pop();
        for (Token token : node.getIdentifier()) {
            Symbol symbol = new Variable(token, variableInfo.getType(), variableInfo.getDimensions());
            try {
                symbolTable.insert(symbol);
            } catch (SemanticException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    @Override
    public void outAVarType(AVarType node) {
        removeIndentationLevel();

        Type type = convertNodeToType(node.getDataType());
        if (type == Type.NOTHING) {
            System.err.println("Internal error: unsupported AST returnInfo in outAVarType");
            System.exit(1);
        }

        ReturnInfo variableInfo = new VariableInfo(type, convertTokensToNumbers(node.getIntConstant()));
        returnInfo.push(variableInfo);
    }

    /* Statement */

    @Override
    public void outAIfStatement(AIfStatement node) {
        removeIndentationLevel();
    }

    @Override
    public void outAWhileStatement(AWhileStatement node) {
        removeIndentationLevel();
    }

    @Override
    public void outAAssignmentStatement(AAssignmentStatement node) {
        removeIndentationLevel();
    }

    @Override
    public void outAFuncCallStatement(AFuncCallStatement node) {
        removeIndentationLevel();
    }

    @Override
    public void outAReturnStatement(AReturnStatement node) {
        removeIndentationLevel();
    }

    @Override
    public void outANullStatement(ANullStatement node) {
        removeIndentationLevel();
    }


    /* Condition */

    @Override
    public void outADisjCond(ADisjCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outAConjCond(AConjCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outANegCond(ANegCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outAEqualCond(AEqualCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outANotEqualCond(ANotEqualCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outAGreaterCond(AGreaterCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outALessCond(ALessCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outAGreaterEqualCond(AGreaterEqualCond node) {
        removeIndentationLevel();
    }

    @Override
    public void outALessEqualCond(ALessEqualCond node) {
        removeIndentationLevel();
    }

    /* Function call */

    @Override
    public void outAFuncCall(AFuncCall node) {
        removeIndentationLevel();
    }

    /* L_value */

    @Override
    public void outAIdentifierLValue(AIdentifierLValue node) {
        removeIndentationLevel();
    }

    @Override
    public void outAStringLValue(AStringLValue node) {
        removeIndentationLevel();
    }

    /* Expr */

    @Override
    public void outAFuncCallExpr(AFuncCallExpr node) {
        removeIndentationLevel();
    }

    @Override
    public void outALValueExpr(ALValueExpr node) {
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
