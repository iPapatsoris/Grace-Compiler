package compiler.symbol_table;

import compiler.node.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;

public class SymbolTable {

    private ArrayDeque<SymbolEntry> symbolList;
    private HashMap<String, SymbolEntry> lookupTable;
    private long curScope;

    public SymbolTable() {
        symbolList = new ArrayDeque<SymbolEntry>();
        lookupTable = new HashMap<String, SymbolEntry>();
        curScope = -1;
    }

    public void enter() {
        curScope++;
    }

    public void exit() {
        HashMap<String, Function> definedFunctions = new HashMap<String, Function>();
        for (Iterator<SymbolEntry> it = symbolList.iterator(); it.hasNext(); ) {
            SymbolEntry symbolEntry = it.next();
            if (symbolEntry.getScope() != curScope) {
                break;
            }
            Symbol symbol = symbolEntry.getSymbol();

            /* Function declaration and definition checks */
            if (symbol instanceof Function) {
                Function function = (Function)symbol;
                if (function.isDefined()) {
                    definedFunctions.put(function.getToken().getText(), function);
                } else {
                    Function definedFunction = definedFunctions.get(function.getToken().getText());
                    if (definedFunction == null) {
                        System.err.println("Semantic error: function " + function.getToken().getText() +
                                                " is declared at " + Symbol.getLocation(function.getToken()) +
                                                " but not defined at the same scope");
                        System.exit(1);
                    } else {
                        if (!function.sameHeader(definedFunction)) {
                            System.err.println("Semantic error: different headers between declared function " +
                                                        function.getToken().getText() + " at " + Symbol.getLocation(function.getToken()) +
                                                        " and defined one at " + Symbol.getLocation(definedFunction.getToken()));
                            System.exit(1);
                        }
                    }
                }
            }


            /* Remove symbol */
            if (symbolEntry.getShadowedSymbolEntry() != null) {
                lookupTable.put(symbol.getToken().getText(), symbolEntry.getShadowedSymbolEntry());
            } else {
                lookupTable.remove(symbol.getToken().getText());
            }
            //System.out.println(TreeVisitor.ANSI_BLUE + "Removing " + symbolEntry + TreeVisitor.ANSI_RESET);
            it.remove();
        }
        curScope--;
        //System.out.println(TreeVisitor.ANSI_BLUE + "After exit: " + this + TreeVisitor.ANSI_RESET);
    }

    public ArrayDeque<Variable> getLocalVars(long scope) {
        ArrayDeque<Variable> symbols = new ArrayDeque<Variable>();
        for (Iterator<SymbolEntry> it = symbolList.iterator(); it.hasNext(); ) {
            SymbolEntry symbolEntry = it.next();
            if (symbolEntry.getScope() > scope) {
                continue;
            }
            Symbol symbol = symbolEntry.getSymbol();
            if (symbolEntry.getScope() != scope || symbol instanceof Argument) {
                break;
            }
            if (symbol instanceof Variable) {
                symbols.push((Variable)symbol);
            }
        }
        return symbols;
    }

    public ArrayDeque<Argument> getArguments(long scope) {
        ArrayDeque<Argument> symbols = new ArrayDeque<Argument>();
        for (Iterator<SymbolEntry> it = symbolList.iterator(); it.hasNext(); ) {
            SymbolEntry symbolEntry = it.next();
            if (symbolEntry.getScope() > scope) {
                continue;
            }
            Symbol symbol = symbolEntry.getSymbol();
            if (symbolEntry.getScope() != scope) {
                break;
            }
            if (symbol instanceof Argument) {
                symbols.push((Argument)symbol);
            }
        }
        return symbols;
    }

    public void insert(Symbol symbol) {
        String identifier = symbol.getToken().getText();
        SymbolEntry oldSymbolEntry = lookupTable.get(identifier);
        if (oldSymbolEntry != null) {
            Symbol oldSymbol = oldSymbolEntry.getSymbol();
            if(oldSymbolEntry.getScope() == curScope && !(oldSymbol instanceof Function
                && symbol instanceof Function && !((Function)oldSymbol).isDefined()
                && ((Function)symbol).isDefined())) {
                System.err.println("Semantic error: symbol \'" + identifier +"\' at " + Symbol.getLocation(symbol.getToken()) +
                                            " is already defined at " + Symbol.getLocation(oldSymbolEntry.getSymbol().getToken()) +
                                            " at current scope");
                System.exit(1);
            }
        }
        SymbolEntry newSymbolEntry = new SymbolEntry(symbol, curScope, oldSymbolEntry);
        symbolList.push(newSymbolEntry);
        lookupTable.put(identifier, newSymbolEntry);
        //System.out.println(TreeVisitor.ANSI_BLUE + "Inserting " + newSymbolEntry + TreeVisitor.ANSI_RESET);
    }

    public Symbol lookup(String symbol) {
        SymbolEntry symbolEntry = lookupTable.get(symbol);
        return (symbolEntry == null ? null : symbolEntry.getSymbol());
    }

    public SymbolEntry lookupEntry(String symbol) {
        return lookupTable.get(symbol);
    }

    public long getCurScope() {
        return curScope;
    }

    public boolean onFirstScope() {
        return curScope == -1;
    }

    public void loadStandardLibrary() {

        /* puti */
        TIdentifier token = new TIdentifier("n", -1, -1);
        Type type = Type.INT;
        ArrayList<Integer> dimensions = new ArrayList<Integer>();
        boolean reference = false;
        boolean noFirstDimension = false;
        Argument argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        ArrayDeque<Argument> arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("puti", -1, -1);
        type = Type.NOTHING;

        Function function = new Function(token, arguments, type, true);
        this.insert(function);

        /* putc */
        token = new TIdentifier("c", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = false;
        noFirstDimension = false;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("putc", -1, -1);
        type = Type.NOTHING;

        function = new Function(token, arguments, type, true);
        this.insert(function);

        /* puts */
        token = new TIdentifier("s", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("puts", -1, -1);
        type = Type.NOTHING;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* geti */
        arguments = new ArrayDeque<Argument>();

        token = new TIdentifier("geti", -1, -1);
        type = Type.INT;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* getc */
        arguments = new ArrayDeque<Argument>();

        token = new TIdentifier("getc", -1, -1);
        type = Type.CHAR;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* gets */
        token = new TIdentifier("n", -1, -1);
        type = Type.INT;
        dimensions = new ArrayList<Integer>();
        reference = false;
        noFirstDimension = false;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("s", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments.add(argument);

        token = new TIdentifier("gets", -1, -1);
        type = Type.NOTHING;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* abs */
        token = new TIdentifier("n", -1, -1);
        type = Type.INT;
        dimensions = new ArrayList<Integer>();
        reference = false;
        noFirstDimension = false;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("abs", -1, -1);
        type = Type.INT;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* ord */
        token = new TIdentifier("c", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = false;
        noFirstDimension = false;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("ord", -1, -1);
        type = Type.INT;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* chr */
        token = new TIdentifier("n", -1, -1);
        type = Type.INT;
        dimensions = new ArrayList<Integer>();
        reference = false;
        noFirstDimension = false;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("chr", -1, -1);
        type = Type.CHAR;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* strlen */
        token = new TIdentifier("s", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("strlen", -1, -1);
        type = Type.INT;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* strcmp */
        token = new TIdentifier("s1", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("s2", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments.add(argument);

        token = new TIdentifier("strcmp", -1, -1);
        type = Type.INT;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* strcpy */
        token = new TIdentifier("trg", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("src", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments.add(argument);

        token = new TIdentifier("strcpy", -1, -1);
        type = Type.NOTHING;

        function = new Function(token, arguments, type, true);
        this.insert(function);


        /* strcat */
        token = new TIdentifier("trg", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments = new ArrayDeque<Argument>();
        arguments.add(argument);

        token = new TIdentifier("src", -1, -1);
        type = Type.CHAR;
        dimensions = new ArrayList<Integer>();
        reference = true;
        noFirstDimension = true;
        argument = new Argument(token, type, dimensions, reference, noFirstDimension);

        arguments.add(argument);

        token = new TIdentifier("strcat", -1, -1);
        type = Type.NOTHING;

        function = new Function(token, arguments, type, true);
        this.insert(function);
    }

    @Override
    public String toString() {
        return symbolList.toString();
    }

    public class SymbolEntry {
        private Symbol symbol;
        private long scope;
        private SymbolEntry shadowedSymbolEntry;

        public SymbolEntry(Symbol symbol, long scope, SymbolEntry shadowedSymbolEntry) {
            this.symbol = symbol;
            this.scope = scope;
            this.shadowedSymbolEntry = shadowedSymbolEntry;
        }

        public Symbol getSymbol() {
            return symbol;
        }

        public long getScope() {
            return scope;
        }

        public SymbolEntry getShadowedSymbolEntry() {
            return shadowedSymbolEntry;
        }

        @Override
        public String toString() {
            return symbol + " at scope " + scope; //return token.getText() + " at scope " + scope + " found at " + Symbol.getLocation(token) + (shadowedSymbol != null ? " overshadowing the one at " + Symbol.getLocation(shadowedSymbol.getToken()) : "");
        }
    }

}
