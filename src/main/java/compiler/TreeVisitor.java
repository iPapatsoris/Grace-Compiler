package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Iterator;
import java.lang.String;
import java.io.IOException;


class TreeVisitor extends DepthFirstAdapter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";

    private SymbolTable symbolTable;
    private ArrayDeque<ReturnInfo> returnInfo;
    private IntermediateRepresentation ir;
    FinalCode finalCode;
    private int indentation;
    private final boolean printAST;

    public TreeVisitor(String output, boolean printAST) {
        this.symbolTable = new SymbolTable();
        this.returnInfo = new ArrayDeque<ReturnInfo>();
        this.ir = new IntermediateRepresentation();
        try {
            this.finalCode = new FinalCode(ir, symbolTable, output);
        } catch (IOException e) {
            System.err.println("I/O error regarding output file: " + e.getMessage());
        }
        this.indentation = 0;
        this.printAST = printAST;
    }

    private void addIndentationLevel() {
        if (printAST) {
            indentation++;
        }
    }

    private void removeIndentationLevel() {
        if (printAST) {
            indentation--;
        }
    }

    private void printIndentation() {
        System.out.print(String.join("", Collections.nCopies(indentation, " ")));
    }

    public void printIR() {
        ir.print();
    }

    public IntermediateRepresentation getIR() {
        return ir;
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
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node));
            addIndentationLevel();
        }
    }

    private void printTokenVal(Node node) {
        if (printAST) {
            printIndentation();
            System.out.println(node);
        }
    }

    /* ******** Out & Case  ******** */

    @Override
    public void outAFuncDefLocalDef(AFuncDefLocalDef node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void outAFuncDeclLocalDef(AFuncDeclLocalDef node) {
        if (printAST) {
            removeIndentationLevel();
        }

        FunctionInfo functionInfo = (FunctionInfo) returnInfo.pop();
        ArrayDeque<Argument> arguments = new ArrayDeque<Argument>();
        for (ArgumentInfo argumentInfo : functionInfo.getArguments()) {
            for (Token argument : argumentInfo.getIdentifiers()) {
                arguments.add(new Argument(argument, argumentInfo.getType(), argumentInfo.getDimensions(), argumentInfo.hasReference(), argumentInfo.hasNoFirstDimension()));
            }
        }
        Symbol function = new Function(functionInfo.getToken(), arguments, functionInfo.getType(), false);
        symbolTable.insert(function);
    }

    @Override
    public void outAVarDefLocalDef(AVarDefLocalDef node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void outAFuncDef(AFuncDef node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void caseAFuncDef(AFuncDef node)
    {
        inAFuncDef(node);
        long functionScope = -2;
        if(node.getHeader() != null)
        {
            node.getHeader().apply(this);
            FunctionInfo functionInfo = (FunctionInfo) returnInfo.peek();

            /* Function symbol on current scope and arguments on new, unless it's main function */
            boolean mainFunction = symbolTable.onFirstScope();
            if (mainFunction) {
                symbolTable.loadStandardLibrary();
                symbolTable.enter();
                if (functionInfo.getArguments().size() > 0 || functionInfo.getType() != Type.NOTHING) {
                    System.err.println("Semantic error: first method should have no arguments and \'nothing\' as return type");
                    System.exit(1);
                }
                finalCode.addMainFunction(functionInfo.getToken().getText());
            }

            /* Get arguments */
            ArrayDeque<Argument> arguments = new ArrayDeque<Argument>(); // For Symbol creation
            for (ArgumentInfo argumentInfo : functionInfo.getArguments()) {
                for (Token argument : argumentInfo.getIdentifiers()) {
                    arguments.add(new Argument(argument, argumentInfo.getType(), argumentInfo.getDimensions(), argumentInfo.hasReference(), argumentInfo.hasNoFirstDimension()));
                }
            }

            /* Add function symbol on current scope */
            Symbol function = new Function(functionInfo.getToken(), arguments, functionInfo.getType(), true);
            symbolTable.insert(function);
            functionScope = symbolTable.getCurScope();


            /* Add arguments on new scope */
            if (! mainFunction) {
                symbolTable.enter();
            }
            for (Argument argument : arguments) {
                symbolTable.insert(argument);
            }
        }
        {
            List<PLocalDef> copy = new ArrayList<PLocalDef>(node.getLocalDef());
            for(PLocalDef e : copy)
            {
                e.apply(this);
            }
        }

        FunctionInfo functionInfo = ((FunctionInfo)returnInfo.peek());

        /* Append scope to function name for unique labeling */
        String uniqueFunctionName = "_" + functionInfo.getToken().getText() + "_" +
                                    String.valueOf(functionScope);
        Quad quad = new Quad(Quad.Op.UNIT,
                             new QuadOperand(QuadOperand.Type.IDENTIFIER, uniqueFunctionName),
                             null, null);
        ir.insertQuad(quad);

        ArrayList<Integer> blockList = new ArrayList<Integer>();
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getStatement());
            int i = 0;
            for(PStatement e : copy)
            {
                if (i > 0) {
                    ir.backpatch(blockList, ir.getNextQuadIndex());
                }
                e.apply(this);
                BackpatchInfo backpatchStatement = (BackpatchInfo)(returnInfo.pop());
                blockList = backpatchStatement.getNextList();
                i++;
            }
        }
        outAFuncDef(node);
        assert returnInfo.peek() instanceof FunctionInfo;
        functionInfo = ((FunctionInfo)returnInfo.pop());
        if (functionInfo.getType() != Type.NOTHING && !functionInfo.getFoundReturn()) {
            System.err.println("Semantic error: method '" + functionInfo.getToken().getText() +
                               "' defined at " + Symbol.getLocation(functionInfo.getToken()) +
                               " returns type " + functionInfo.getType() +
                               " but has no return statement");
            System.exit(1);
        }
        ir.backpatch(blockList, ir.getNextQuadIndex());
        quad = new Quad(Quad.Op.ENDU,
                             new QuadOperand(QuadOperand.Type.IDENTIFIER, uniqueFunctionName),
                             null, null);
        ir.insertQuad(quad);
        finalCode.generate();
        symbolTable.exit();
        if (symbolTable.onFirstScope()) {
            finalCode.closeWriter();
        }
    }

    @Override
    public void outAHeader(AHeader node) {
        if (printAST) {
            removeIndentationLevel();
        }

        ArrayDeque<ArgumentInfo> arguments = new ArrayDeque<ArgumentInfo>();
        for (int argumentCount = node.getFparDef().size() ; argumentCount > 0 ; argumentCount--) {
            ArgumentInfo argumentInfo = (ArgumentInfo) returnInfo.pop();
            if (!argumentInfo.hasReference() && (argumentInfo.getDimensions().size() > 0 || argumentInfo.hasNoFirstDimension())) {
                System.err.println("Semantic error: argument(s) '" + argumentInfo.getIdentifiers() + "' in method '" +
                                    node.getIdentifier().getText() +"' at " + Symbol.getLocation(node.getIdentifier()) + " are of type array but not reference");
                System.exit(1);
            }
            arguments.addFirst(argumentInfo);
        }
        ReturnInfo functionInfo = new FunctionInfo(node.getIdentifier(), arguments, convertNodeToType(node.getDataType()));
        returnInfo.push(functionInfo);
    }

    @Override
    public void outAFparDef(AFparDef node) {
        if (printAST) {
            removeIndentationLevel();
        }

        ArgumentInfo argumentInfo = (ArgumentInfo) returnInfo.peek();
        boolean reference =  (node.getRef() != null ? true : false);
        argumentInfo.setReference(reference);
        argumentInfo.setIdentifiers(new ArrayDeque(node.getIdentifier()));
    }

    @Override
    public void outAFparType(AFparType node) {
        if (printAST) {
            removeIndentationLevel();
        }

        boolean noFirstDimension = (node.getLsquareBracket() != null ? true : false);
        ArgumentInfo argumentInfo = new ArgumentInfo(convertNodeToType(node.getDataType()), convertTokensToNumbers(node.getIntConstant()), noFirstDimension);
        returnInfo.push(argumentInfo);
    }

    @Override
    public void outAIntDataType(AIntDataType node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void outACharDataType(ACharDataType node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void outANothingDataType(ANothingDataType node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    /* Variable */

    @Override
    public void outAVarDef(AVarDef node) {
        if (printAST) {
            removeIndentationLevel();
        }

        VariableInfo variableInfo = (VariableInfo) returnInfo.pop();
        for (Token token : node.getIdentifier()) {
            Symbol symbol = new Variable(token, variableInfo.getType(), variableInfo.getDimensions());
            symbolTable.insert(symbol);
        }
    }

    @Override
    public void outAVarType(AVarType node) {
        if (printAST) {
            removeIndentationLevel();
        }

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
    public void caseAIfStatement(AIfStatement node)
    {
        inAIfStatement(node);
        if(node.getCond() != null)
        {
            node.getCond().apply(this);
        }
        BackpatchInfo backpatchCond = (BackpatchInfo)(returnInfo.pop());
        ir.backpatch(backpatchCond.getTrueList(), ir.getNextQuadIndex());
        ArrayList<Integer> list = backpatchCond.getFalseList();
        ArrayList<Integer> blockThenList = new ArrayList<Integer>();
        ArrayList<Integer> blockElseList = new ArrayList<Integer>();
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getThen());
            int i = 0;
            for(PStatement e : copy)
            {
                if (i > 0) {
                    ir.backpatch(blockThenList, ir.getNextQuadIndex());
                }
                e.apply(this);
                BackpatchInfo backpatchStatement = (BackpatchInfo)(returnInfo.pop());
                blockThenList = backpatchStatement.getNextList();
                i++;
            }
        }
        {
            if (node.getElse().size() > 0) {
                list = new ArrayList<Integer>();
                list.add(ir.getNextQuadIndex());
                Quad quad = new Quad(Quad.Op.JUMP, null, null, new QuadOperand(QuadOperand.Type.BACKPATCH));
                ir.insertQuad(quad);
                ir.backpatch(backpatchCond.getFalseList(), ir.getNextQuadIndex());
            }
            List<PStatement> copy = new ArrayList<PStatement>(node.getElse());
            int i = 0;
            for(PStatement e : copy)
            {
                if (i > 0) {
                    ir.backpatch(blockElseList, ir.getNextQuadIndex());
                }
                e.apply(this);
                BackpatchInfo backpatchStatement = (BackpatchInfo)(returnInfo.pop());
                blockElseList = backpatchStatement.getNextList();
                i++;
            }
        }
        list.addAll(blockThenList);
        list.addAll(blockElseList);
        returnInfo.push(new BackpatchInfo(list));
        outAIfStatement(node);
    }

    @Override
    public void outAIfStatement(AIfStatement node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void caseAWhileStatement(AWhileStatement node)
    {
        inAWhileStatement(node);
        int firstQuad = ir.getNextQuadIndex();
        if(node.getCond() != null)
        {
            node.getCond().apply(this);
        }
        BackpatchInfo backpatchCond = (BackpatchInfo)(returnInfo.pop());
        ir.backpatch(backpatchCond.getTrueList(), ir.getNextQuadIndex());
        ArrayList<Integer> block = new ArrayList<Integer>();
        {
            List<PStatement> copy = new ArrayList<PStatement>(node.getStatement());
            int i = 0;
            for(PStatement e : copy)
            {
                if (i > 0) {
                    ir.backpatch(block, ir.getNextQuadIndex());
                }
                e.apply(this);
                BackpatchInfo backpatchStatement = (BackpatchInfo)(returnInfo.pop());
                block = backpatchStatement.getNextList();
                i++;
            }
        }
        ir.backpatch(block, firstQuad);
        Quad quad = new Quad(Quad.Op.JUMP, null, null, new QuadOperand(QuadOperand.Type.LABEL,
                                                                       firstQuad));
        ir.insertQuad(quad);
        returnInfo.push(new BackpatchInfo(backpatchCond.getFalseList()));
        outAWhileStatement(node);
    }

    @Override
    public void outAWhileStatement(AWhileStatement node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    private static void checkSameTypeAssignment(ExprInfo lvalue, ExprInfo expr) {
        if (lvalue.getDimensions().size() > 0 || expr.getDimensions().size() > 0 ||
            lvalue.getType() != expr.getType()) {
            System.err.println("Semantic error: assignment at " + Symbol.getLocation(lvalue.getToken()) +
                               " expected lvalue and expression of the same type 'int' or 'char', but got '" +
                                Symbol.typeToString(lvalue.getType()) +
                                String.join("", Collections.nCopies(lvalue.getDimensions().size(), "[]")) + "' and '" +
                                Symbol.typeToString(expr.getType()) +
                                String.join("", Collections.nCopies(expr.getDimensions().size(), "[]")) + "' instead");
            System.exit(1);
        }
    }

    @Override
    public void outAAssignmentStatement(AAssignmentStatement node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo expr = (ExprInfo)returnInfo.pop();
        ExprInfo lvalue = (ExprInfo)returnInfo.pop();
        checkSameTypeAssignment(lvalue, expr);

        Quad quad = new Quad(Quad.Op.ASSIGN, new QuadOperand(expr.getIRInfo()), null,
                             new QuadOperand(lvalue.getIRInfo()));
        ir.insertQuad(quad);
        returnInfo.push(new BackpatchInfo(new ArrayList<Integer>()));
    }

    @Override
    public void outAFuncCallStatement(AFuncCallStatement node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo expr = ((ExprInfo)returnInfo.pop()); // Consume function type: no need to check outside expression fcall
        returnInfo.push(new BackpatchInfo(new ArrayList<Integer>()));
    }

    @Override
    public void outAReturnStatement(AReturnStatement node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo expr = (node.getExpr() == null ? null : ((ExprInfo)returnInfo.pop()));
        assert returnInfo.peek() instanceof FunctionInfo;
        FunctionInfo functionInfo = ((FunctionInfo)returnInfo.peek());
        if (expr != null && (expr.getDimensions().size() > 0 || functionInfo.getType() != expr.getType())) {
            System.err.println("Semantic error: method '" + functionInfo.getToken().getText() +
                               "' defined at " + Symbol.getLocation(functionInfo.getToken()) +
                               " is of return type '" + Symbol.typeToString(functionInfo.getType()) +
                               "', but found return type '" + Symbol.typeToString(expr.getType()) +
                               String.join("", Collections.nCopies(expr.getDimensions().size(), "[]")) +
                               "' at " + Symbol.getLocation(expr.getToken()));
            System.exit(1);
        } else if (expr == null && functionInfo.getType() != Type.NOTHING) {
            System.err.println("Semantic error: method '" + functionInfo.getToken().getText() +
                               "' defined at " + Symbol.getLocation(functionInfo.getToken()) +
                               " is of return type '" + Symbol.typeToString(functionInfo.getType()) +
                               "', but found no return type");
            System.exit(1);
        }
        functionInfo.setFoundReturn(true);
        Quad quad = new Quad(Quad.Op.ASSIGN, new QuadOperand(expr.getIRInfo()), null,
                             new QuadOperand(QuadOperand.Type.RETCALLED));
        ir.insertQuad(quad);
        quad = new Quad(Quad.Op.RET, null, null, null);
        ir.insertQuad(quad);
        returnInfo.push(new BackpatchInfo(new ArrayList<Integer>()));
    }

    @Override
    public void outANullStatement(ANullStatement node) {
        if (printAST) {
            removeIndentationLevel();
        }
        returnInfo.push(new BackpatchInfo(new ArrayList<Integer>()));
    }


    /* Condition */

    private static void checkSameTypeOperand(String operator, ExprInfo exprLeft, ExprInfo exprRight) {
        if (exprLeft.getDimensions().size() > 0 || exprRight.getDimensions().size() > 0 ||
            exprLeft.getType() != exprRight.getType() || exprLeft.getType() == Type.NOTHING) {
            System.err.println("Semantic error: operator '" + operator + "' expected operand of the same type 'int' or 'char', but got '" +
                                Symbol.typeToString(exprLeft.getType()) +
                                String.join("", Collections.nCopies(exprLeft.getDimensions().size(), "[]")) + "' and '" +
                                Symbol.typeToString(exprRight.getType()) +
                                String.join("", Collections.nCopies(exprRight.getDimensions().size(), "[]"))+ "' instead at " +
                                Symbol.getLocation(exprLeft.getToken()));
            System.exit(1);
        }
    }

    @Override
    public void caseADisjCond(ADisjCond node)
    {
        inADisjCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        BackpatchInfo leftCond = (BackpatchInfo)(returnInfo.pop());
        ir.backpatch(leftCond.getFalseList(), ir.getNextQuadIndex());

        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        BackpatchInfo rightCond = (BackpatchInfo)(returnInfo.pop());
        ArrayList<Integer> trueList = leftCond.getTrueList();
        trueList.addAll(rightCond.getTrueList());
        returnInfo.push(new BackpatchInfo(rightCond.getFalseList(), trueList));
        outADisjCond(node);
    }

    @Override
    public void outADisjCond(ADisjCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }


    @Override
    public void caseAConjCond(AConjCond node)
    {
        inAConjCond(node);
        if(node.getLeft() != null)
        {
            node.getLeft().apply(this);
        }
        BackpatchInfo leftCond = (BackpatchInfo)(returnInfo.pop());
        ir.backpatch(leftCond.getTrueList(), ir.getNextQuadIndex());

        if(node.getRight() != null)
        {
            node.getRight().apply(this);
        }
        BackpatchInfo rightCond = (BackpatchInfo)(returnInfo.pop());
        ArrayList<Integer> falseList = leftCond.getFalseList();
        falseList.addAll(rightCond.getFalseList());
        returnInfo.push(new BackpatchInfo(falseList, rightCond.getTrueList()));
        outAConjCond(node);
    }

    @Override
    public void outAConjCond(AConjCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void outANegCond(ANegCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        BackpatchInfo backpatchInfo = (BackpatchInfo)(returnInfo.peek());
        ArrayList<Integer> tmp = backpatchInfo.getFalseList();
        backpatchInfo.setFalseList(backpatchInfo.getTrueList());
        backpatchInfo.setTrueList(tmp);
    }

    private BackpatchInfo generateRelationalOpIR(Quad.Op op, ExprInfo exprLeft, ExprInfo exprRight) {
        ArrayList<Integer> trueList = new ArrayList<Integer>();
        trueList.add(ir.getNextQuadIndex());
        Quad quad = new Quad(op, new QuadOperand(exprLeft.getIRInfo()),
                             new QuadOperand(exprRight.getIRInfo()),
                             new QuadOperand(QuadOperand.Type.BACKPATCH));
        ir.insertQuad(quad);

        ArrayList<Integer> falseList = new ArrayList<Integer>();
        falseList.add(ir.getNextQuadIndex());
        quad = new Quad(Quad.Op.JUMP, null, null, new QuadOperand(QuadOperand.Type.BACKPATCH));
        ir.insertQuad(quad);
        return new BackpatchInfo(falseList, trueList);
    }

    @Override
    public void outAEqualCond(AEqualCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo exprRight = (ExprInfo)returnInfo.pop();
        ExprInfo exprLeft = (ExprInfo)returnInfo.pop();
        checkSameTypeOperand("=", exprLeft, exprRight);
        returnInfo.push(generateRelationalOpIR(Quad.Op.EQUAL, exprLeft, exprRight));
    }

    @Override
    public void outANotEqualCond(ANotEqualCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo exprRight = (ExprInfo)returnInfo.pop();
        ExprInfo exprLeft = (ExprInfo)returnInfo.pop();
        checkSameTypeOperand("#", exprLeft, exprRight);
        returnInfo.push(generateRelationalOpIR(Quad.Op.NOT_EQUAL, exprLeft, exprRight));
    }

    @Override
    public void outAGreaterCond(AGreaterCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo exprRight = (ExprInfo)returnInfo.pop();
        ExprInfo exprLeft = (ExprInfo)returnInfo.pop();
        checkSameTypeOperand(">", exprLeft, exprRight);
        returnInfo.push(generateRelationalOpIR(Quad.Op.GREATER, exprLeft, exprRight));
    }

    @Override
    public void outALessCond(ALessCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo exprRight = (ExprInfo)returnInfo.pop();
        ExprInfo exprLeft = (ExprInfo)returnInfo.pop();
        checkSameTypeOperand("<", exprLeft, exprRight);
        returnInfo.push(generateRelationalOpIR(Quad.Op.LESS, exprLeft, exprRight));
    }

    @Override
    public void outAGreaterEqualCond(AGreaterEqualCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo exprRight = (ExprInfo)returnInfo.pop();
        ExprInfo exprLeft = (ExprInfo)returnInfo.pop();
        checkSameTypeOperand(">=", exprLeft, exprRight);
        returnInfo.push(generateRelationalOpIR(Quad.Op.GREATER_EQUAL, exprLeft, exprRight));
    }

    @Override
    public void outALessEqualCond(ALessEqualCond node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo exprRight = (ExprInfo)returnInfo.pop();
        ExprInfo exprLeft = (ExprInfo)returnInfo.pop();
        checkSameTypeOperand("<=", exprLeft, exprRight);
        returnInfo.push(generateRelationalOpIR(Quad.Op.LESS_EQUAL, exprLeft, exprRight));
    }

    /* Function call */

    @Override
    public void outAFuncCall(AFuncCall node) {
        if (printAST) {
            removeIndentationLevel();
        }
        Symbol symbol = symbolTable.lookup(node.getIdentifier().getText());
        if (symbol == null || !(symbol instanceof Function)) {
            System.err.println("Semantic error: undeclared method \'" +
                                node.getIdentifier().getText() + "\' at " +
                                Symbol.getLocation(node.getIdentifier()));
            System.exit(1);
        }

        Function function = (Function)symbol;
        symbol = null;
        if (node.getExpr().size() != function.getArguments().size()) {
            System.err.println("Semantic error: method '" + node.getIdentifier().getText() +
                               "' called at " + Symbol.getLocation(node.getIdentifier()) +
                               " conflicts with header's argument number at " +
                               Symbol.getLocation(function.getToken()) + ": expected " +
                               function.getArguments().size() + " but got " + node.getExpr().size() +
                               " instead");
            System.exit(1);
        }

        /* Get arguments in the right order */
        ArrayDeque<ExprInfo> args = new ArrayDeque<ExprInfo>();
        for (int i = 0 ; i < node.getExpr().size() ; i++) {
            args.push(((ExprInfo)returnInfo.pop()));
        }

        int i = 0;
        for (Iterator<Argument> it = function.getArguments().iterator() ; it.hasNext() ; i++ ) {
            Argument argCorrect = it.next();
            int dimensionsNumCorrect = argCorrect.getDimensions().size();
            if (argCorrect.hasNoFirstDimension()) {
                dimensionsNumCorrect++;
            }
            ExprInfo arg = args.pop();
            if (arg.getType() != argCorrect.getType() || arg.getDimensions().size() != dimensionsNumCorrect) {
                System.err.println("Semantic error: method '" + node.getIdentifier().getText() +
                                   "' called at " + Symbol.getLocation(node.getIdentifier()) +
                                   " conflicts with header's argument types at " +
                                   Symbol.getLocation(function.getToken()) + ": expected argument " +
                                   (i+1) + " to be of type '" + Symbol.typeToString(argCorrect.getType()) +
                                   String.join("", Collections.nCopies(dimensionsNumCorrect, "[]")) +
                                   "', but got '" + Symbol.typeToString(arg.getType()) +
                                   String.join("", Collections.nCopies(arg.getDimensions().size(), "[]")) +
                                   "' instead at " + Symbol.getLocation(arg.getToken()));
                System.exit(1);
            } else if (!arg.isLvalue() && argCorrect.isReference()) {
                System.err.println("Semantic error: method '" + node.getIdentifier().getText() +
                                   "' called at " + Symbol.getLocation(node.getIdentifier()) +
                                   " conflicts with header's argument at " +
                                   Symbol.getLocation(function.getToken()) + ": argument " +
                                   (i+1) + " is passed by reference, but is not an lvalue'");
                System.exit(1);
            }
            QuadOperand.Type pass = (argCorrect.isReference() ? QuadOperand.Type.R
                                                               : QuadOperand.Type.V);
            Quad quad = new Quad(Quad.Op.PAR, new QuadOperand(arg.getIRInfo()),
                                 new QuadOperand(pass), null);
            ir.insertQuad(quad);
        }
        int result = ir.newTempVar(function.getType());
        Quad quad = new Quad(Quad.Op.PAR,
                             new QuadOperand(QuadOperand.Type.TEMPVAR, result),
                             new QuadOperand(QuadOperand.Type.RETCALLER), null);
        ir.insertQuad(quad);

        /* Append scope to function name for unique labeling */
        long calledFunctionScope = symbolTable.lookupEntry(function.getToken().getText()).getScope();
        String uniqueFunctionName = "_" + function.getToken().getText() + "_" + String.valueOf(calledFunctionScope);
        quad = new Quad(Quad.Op.CALL, null, null,
                        new QuadOperand(QuadOperand.Type.IDENTIFIER, uniqueFunctionName));
        ir.insertQuad(quad);
        returnInfo.push(new ExprInfo(function.getType(), node.getIdentifier(),
                        new IRInfo(IRInfo.Type.TEMPVAR, result)));
    }

    /* L_value */

    private static void checkNumericExpession(ExprInfo expr) {
        if (expr.getType() != Type.INT || expr.getDimensions().size() > 0) {
            System.err.println("Semantic error: expression at " + Symbol.getLocation(expr.getToken()) +
                               " should be of type 'int', but it is '" +
                               Symbol.typeToString(expr.getType()) +
                               String.join("", Collections.nCopies(expr.getDimensions().size(), "[]")) +
                               "' instead");
            System.exit(1);
        }
    }

    @Override
    public void outAIdentifierLValue(AIdentifierLValue node) {
        if (printAST) {
            removeIndentationLevel();
        }

        Symbol symbol = symbolTable.lookup(node.getIdentifier().getText());
        if (symbol == null || !(symbol instanceof Variable || symbol instanceof Argument)) {
            System.err.println("Semantic error: undeclared symbol \'" +
                                node.getIdentifier().getText() + "\' at " +
                                Symbol.getLocation(node.getIdentifier()));
            System.exit(1);
        }

        int dimensionsNum = ((Variable)symbol).getDimensions().size();
        if (symbol instanceof Argument && ((Argument)symbol).hasNoFirstDimension()) {
            dimensionsNum++;
        }


        Variable variable = (Variable)symbol;
        symbol = null;

        if (node.getExpr().size() > dimensionsNum) {
            System.err.println("Semantic error: lvalue '" + node.getIdentifier().getText() +
                               "' at " + Symbol.getLocation(node.getIdentifier()) +
                               " uses more dimensions than definition at: " +
                               Symbol.getLocation(variable.getToken()) + ": expected at most " +
                               dimensionsNum + " but got " + node.getExpr().size() +
                               " instead");
            System.exit(1);
        }

        ArrayList<ExprInfo> exprs = new ArrayList<ExprInfo>();
        for (int i = 0 ; i < node.getExpr().size() ; i++) {
            exprs.add(0, ((ExprInfo)returnInfo.pop()));
        }

        for (Iterator<ExprInfo> it = exprs.iterator(); it.hasNext() ; ) {
            ExprInfo expr = it.next();
            checkNumericExpession(expr);
        }

        IRInfo irInfo = null;
        switch (exprs.size()) {
            case 0:
                irInfo = new IRInfo(IRInfo.Type.IDENTIFIER, node.getIdentifier().getText());
                break;
            case 1: {
                ExprInfo expr = exprs.get(0);
                int tempVar = ir.newTempVar(exprs.size() == dimensionsNum ? variable.getType() : Type.INT);
                Quad quad = new Quad(Quad.Op.ARRAY, new QuadOperand(QuadOperand.Type.IDENTIFIER, node.getIdentifier().getText()),
                                     new QuadOperand(expr.getIRInfo()),
                                     new QuadOperand(QuadOperand.Type.ADDRESS, tempVar));
                ir.insertQuad(quad);
                irInfo = new IRInfo(IRInfo.Type.ADDRESS, tempVar);
            } break;
            default: {
                ExprInfo expr = exprs.get(0);
                int columns = variable.getDimensions().get(1);
                int multVar = ir.newTempVar(Type.INT);
                Quad quad = new Quad(Quad.Op.MULT, new QuadOperand(expr.getIRInfo()),
                                     new QuadOperand(QuadOperand.Type.IDENTIFIER, String.valueOf(columns)),
                                     new QuadOperand(QuadOperand.Type.TEMPVAR, multVar));
                ir.insertQuad(quad);

                expr = exprs.get(1);
                int addVar = ir.newTempVar(Type.INT);
                quad = new Quad(Quad.Op.ADD, new QuadOperand(QuadOperand.Type.TEMPVAR, multVar),
                                     new QuadOperand(expr.getIRInfo()),
                                     new QuadOperand(QuadOperand.Type.TEMPVAR, addVar));
                ir.insertQuad(quad);

                for (int i = 2 ; i < exprs.size() ; i++) {
                    int dimensionRange = variable.getDimensions().get(i);
                    multVar = ir.newTempVar(Type.INT);
                    quad = new Quad(Quad.Op.MULT, new QuadOperand(QuadOperand.Type.TEMPVAR, addVar),
                                    new QuadOperand(QuadOperand.Type.IDENTIFIER, String.valueOf(dimensionRange)),
                                    new QuadOperand(QuadOperand.Type.TEMPVAR, multVar));
                    ir.insertQuad(quad);

                    expr = exprs.get(i);
                    addVar = ir.newTempVar(Type.INT);
                    quad = new Quad(Quad.Op.ADD, new QuadOperand(QuadOperand.Type.TEMPVAR, multVar),
                                         new QuadOperand(expr.getIRInfo()),
                                         new QuadOperand(QuadOperand.Type.TEMPVAR, addVar));
                    ir.insertQuad(quad);
                }

                /* Specified all dimmensions: actual type instead of int */
                int arrayVar = ir.newTempVar(exprs.size() == dimensionsNum ? variable.getType() : Type.INT);
                quad = new Quad(Quad.Op.ARRAY, new QuadOperand(QuadOperand.Type.IDENTIFIER, node.getIdentifier().getText()),
                                     new QuadOperand(QuadOperand.Type.TEMPVAR, addVar),
                                     new QuadOperand(QuadOperand.Type.ADDRESS, arrayVar));
                ir.insertQuad(quad);
                irInfo = new IRInfo(IRInfo.Type.ADDRESS, arrayVar);
            }
    }

        /* Carry over the number of dimensions left, if any, to identify if lvalue remains an array */
        ArrayList<Integer> dimensionsLeft = new ArrayList<Integer>();
        for (int i = 0 ; i < dimensionsNum - node.getExpr().size() ; i++) {
            dimensionsLeft.add(null);
        }
        returnInfo.push(new ExprInfo(variable.getType(), dimensionsLeft, node.getIdentifier(), irInfo));
    }

    @Override
    public void outAStringLValue(AStringLValue node) {
        if (printAST) {
            removeIndentationLevel();
        }

        if (node.getExpr().size() > 1) {
            System.err.println("Semantic error: lvalue '" + node.getString().getText() +
                               "' at " + Symbol.getLocation(node.getString()) +
                               " expected at most 1 dimension but got " +
                               node.getExpr().size() + " instead");
            System.exit(1);
        }

        IRInfo irInfo = null;
        int tempVar = -1;
        ArrayList<Integer> dimensionsLeft = new ArrayList<Integer>();
        if (node.getExpr().size() == 1) {
            ExprInfo expr = (ExprInfo)returnInfo.pop();
            checkNumericExpession(expr);
            tempVar = ir.newTempVar(Type.CHAR);
            Quad quad = new Quad(Quad.Op.ARRAY, new QuadOperand(QuadOperand.Type.IDENTIFIER, node.getString().getText()),
                                 new QuadOperand(expr.getIRInfo()),
                                 new QuadOperand(QuadOperand.Type.ADDRESS, tempVar));
            ir.insertQuad(quad);
            irInfo = new IRInfo(IRInfo.Type.ADDRESS, tempVar);
        } else {
            dimensionsLeft.add(null);
            irInfo = new IRInfo(IRInfo.Type.IDENTIFIER, node.getString().getText());
        }
        returnInfo.push(new ExprInfo(Type.CHAR, dimensionsLeft, node.getString(), irInfo));
    }

    /* Expr */

    @Override
    public void outAFuncCallExpr(AFuncCallExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    @Override
    public void outALValueExpr(ALValueExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
    }

    private static void checkNumericOperand(String operator, ExprInfo expr) {
        if (expr.getType() != Type.INT || expr.getDimensions().size() > 0) {
            System.err.println("Semantic error: operator '" + operator + "' expected operand of type 'int', but got '" +
                                Symbol.typeToString(expr.getType()) +
                                String.join("", Collections.nCopies(expr.getDimensions().size(), "[]")) + "' instead at " +
                                Symbol.getLocation(expr.getToken()));
            System.exit(1);
        }
    }

    @Override
    public void outAAddExpr(AAddExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ArrayDeque<ExprInfo> exprs = new ArrayDeque<ExprInfo>();
        for (int i = 0 ; i < 2 ; i++) {
            exprs.push(((ExprInfo)returnInfo.pop()));
        }

        Token token = null;
        IRInfo irInfoLeft = null;
        IRInfo irInfoRight = null;
        for (int i = 0 ; i < 2 ; i++) {
            ExprInfo expr = exprs.pop();
            if (i == 0) {
                token = expr.getToken();
                irInfoLeft = expr.getIRInfo();
            } else if (i == 1) {
                irInfoRight = expr.getIRInfo();
            }
            checkNumericOperand("+", expr);
        }
        int tempVar = ir.newTempVar(Type.INT);
        Quad quad = new Quad(Quad.Op.ADD, new QuadOperand(irInfoLeft),
                             new QuadOperand(irInfoRight),
                             new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
        ir.insertQuad(quad);
        IRInfo irInfo = new IRInfo(IRInfo.Type.TEMPVAR, tempVar);
        returnInfo.push(new ExprInfo(Type.INT, token, irInfo));
    }

    @Override
    public void outASubExpr(ASubExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ArrayDeque<ExprInfo> exprs = new ArrayDeque<ExprInfo>();
        for (int i = 0 ; i < 2 ; i++) {
            exprs.push(((ExprInfo)returnInfo.pop()));
        }

        Token token = null;
        IRInfo irInfoLeft = null;
        IRInfo irInfoRight = null;
        for (int i = 0 ; i < 2 ; i++) {
            ExprInfo expr = exprs.pop();
            if (i == 0) {
                token = expr.getToken();
                irInfoLeft = expr.getIRInfo();
            } else if (i == 1) {
                irInfoRight = expr.getIRInfo();
            }
            checkNumericOperand("-", expr);
        }
        int tempVar = ir.newTempVar(Type.INT);
        Quad quad = new Quad(Quad.Op.SUB, new QuadOperand(irInfoLeft),
                             new QuadOperand(irInfoRight),
                             new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
        ir.insertQuad(quad);
        IRInfo irInfo = new IRInfo(IRInfo.Type.TEMPVAR, tempVar);
        returnInfo.push(new ExprInfo(Type.INT, token, irInfo));
    }

    @Override
    public void outAMultExpr(AMultExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ArrayDeque<ExprInfo> exprs = new ArrayDeque<ExprInfo>();
        for (int i = 0 ; i < 2 ; i++) {
            exprs.push(((ExprInfo)returnInfo.pop()));
        }

        Token token = null;
        IRInfo irInfoLeft = null;
        IRInfo irInfoRight = null;
        for (int i = 0 ; i < 2 ; i++) {
            ExprInfo expr = exprs.pop();
            if (i == 0) {
                token = expr.getToken();
                irInfoLeft = expr.getIRInfo();
            } else if (i == 1) {
                irInfoRight = expr.getIRInfo();
            }
            checkNumericOperand("*", expr);
        }
        int tempVar = ir.newTempVar(Type.INT);
        Quad quad = new Quad(Quad.Op.MULT, new QuadOperand(irInfoLeft),
                             new QuadOperand(irInfoRight),
                             new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
        ir.insertQuad(quad);
        IRInfo irInfo = new IRInfo(IRInfo.Type.TEMPVAR, tempVar);
        returnInfo.push(new ExprInfo(Type.INT, token, irInfo));
    }

    @Override
    public void outADivExpr(ADivExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ArrayDeque<ExprInfo> exprs = new ArrayDeque<ExprInfo>();
        for (int i = 0 ; i < 2 ; i++) {
            exprs.push(((ExprInfo)returnInfo.pop()));
        }

        Token token = null;
        IRInfo irInfoLeft = null;
        IRInfo irInfoRight = null;
        for (int i = 0 ; i < 2 ; i++) {
            ExprInfo expr = exprs.pop();
            if (i == 0) {
                token = expr.getToken();
                irInfoLeft = expr.getIRInfo();
            } else if (i == 1) {
                irInfoRight = expr.getIRInfo();
            }
            checkNumericOperand("div", expr);
        }
        int tempVar = ir.newTempVar(Type.INT);
        Quad quad = new Quad(Quad.Op.DIV, new QuadOperand(irInfoLeft),
                             new QuadOperand(irInfoRight),
                             new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
        ir.insertQuad(quad);
        IRInfo irInfo = new IRInfo(IRInfo.Type.TEMPVAR, tempVar);
        returnInfo.push(new ExprInfo(Type.INT, token, irInfo));
    }

    @Override
    public void outAModExpr(AModExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ArrayDeque<ExprInfo> exprs = new ArrayDeque<ExprInfo>();
        for (int i = 0 ; i < 2 ; i++) {
            exprs.push(((ExprInfo)returnInfo.pop()));
        }

        Token token = null;
        IRInfo irInfoLeft = null;
        IRInfo irInfoRight = null;
        for (int i = 0 ; i < 2 ; i++) {
            ExprInfo expr = exprs.pop();
            if (i == 0) {
                token = expr.getToken();
                irInfoLeft = expr.getIRInfo();
            } else if (i == 1) {
                irInfoRight = expr.getIRInfo();
            }
            checkNumericOperand("mod", expr);
        }
        int tempVar = ir.newTempVar(Type.INT);
        Quad quad = new Quad(Quad.Op.MOD, new QuadOperand(irInfoLeft),
                             new QuadOperand(irInfoRight),
                             new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
        ir.insertQuad(quad);
        IRInfo irInfo = new IRInfo(IRInfo.Type.TEMPVAR, tempVar);
        returnInfo.push(new ExprInfo(Type.INT, token, irInfo));
    }

    @Override
    public void outAPositiveExpr(APositiveExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo expr = (ExprInfo)returnInfo.peek();
        checkNumericOperand("+", expr);
    }

    @Override
    public void outANegativeExpr(ANegativeExpr node) {
        if (printAST) {
            removeIndentationLevel();
        }
        ExprInfo expr = (ExprInfo)returnInfo.peek();
        checkNumericOperand("-", expr);
        expr.toggleNegative();

        IRInfo irInfo = expr.getIRInfo();
        int tempVar = ir.newTempVar(Type.INT);
        Quad quad = new Quad(Quad.Op.MULT, new QuadOperand(irInfo),
                             new QuadOperand(QuadOperand.Type.IDENTIFIER, "-1"),
                             new QuadOperand(QuadOperand.Type.TEMPVAR, tempVar));
        ir.insertQuad(quad);
        irInfo.setType(IRInfo.Type.TEMPVAR);
        irInfo.setTempVar(tempVar);
    }

    @Override
    public void outAIntConstantExpr(AIntConstantExpr node) {
        IRInfo irInfo = new IRInfo(IRInfo.Type.IDENTIFIER, node.getIntConstant().getText());
        returnInfo.push(new ExprInfo(Type.INT, node.getIntConstant(), irInfo));
    }

    @Override
    public void outACharConstantExpr(ACharConstantExpr node) {
        IRInfo irInfo = new IRInfo(IRInfo.Type.IDENTIFIER, node.getCharConstant().getText());
        returnInfo.push(new ExprInfo(Type.CHAR, node.getCharConstant(), irInfo));
    }

    /* ******** In ******** */

    @Override
    public void defaultIn(Node node) {
        System.out.println(ANSI_RED + node.getClass() + " " + ANSI_RESET + node);
    }

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
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ":  " + node.getIdentifier());
            addIndentationLevel();
        }
    }

    @Override
    public void inAFparDef(AFparDef node) {
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ":  " +
                (node.getRef() != null ? "ref " : "")  + node.getIdentifier());
            addIndentationLevel();
        }
    }

    @Override
    public void inAFparType(AFparType node) {
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ":  " +
                (node.getLsquareBracket() != null ? "empty dimension, " : "")  + node.getIntConstant());
            addIndentationLevel();
        }
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
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ":  " + node.getIdentifier());
            addIndentationLevel();
        }
    }

    @Override
    public void inAVarType(AVarType node) {
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ":  " + node.getIntConstant());
            addIndentationLevel();
        }
    }

    /* Statement */

    @Override
    public void inAIfStatement(AIfStatement node) {
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ": then " + node.getThen().size() + ", else " + node.getElse().size());
            addIndentationLevel();
        }
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
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ": " + node.getIdentifier());
            addIndentationLevel();
        }
    }

   /* L_value */

    @Override
    public void inAIdentifierLValue(AIdentifierLValue node) {
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ": " + node.getIdentifier());
            addIndentationLevel();
        }
    }

    @Override
    public void inAStringLValue(AStringLValue node) {
        if (printAST) {
            printIndentation();
            System.out.println(getClassName(node) + ": " + node.getString());
            addIndentationLevel();
        }
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

}
