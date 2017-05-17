package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;

class SymbolTable {

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

    public void exit() throws SemanticException {
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
                        throw new SemanticException("Semantic error: function " + function.getToken().getText() +
                                                " is declared at " + Symbol.getLocation(function.getToken()) +
                                                " but not defined at the same scope");
                    } else {
                        if (!function.sameHeader(definedFunction)) {
                            throw new SemanticException("Semantic error: different headers between declared function " +
                                                        function.getToken().getText() + " at " + Symbol.getLocation(function.getToken()) +
                                                        " and defined one at " + Symbol.getLocation(definedFunction.getToken()));
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
            System.out.println(TreeVisitor.ANSI_BLUE + "Removing " + symbolEntry + TreeVisitor.ANSI_RESET);
            it.remove();
        }
        curScope--;
        //System.out.println(TreeVisitor.ANSI_BLUE + "After exit: " + this + TreeVisitor.ANSI_RESET);
    }

    public void insert(Symbol symbol) throws SemanticException {
        String identifier = symbol.getToken().getText();
        SymbolEntry oldSymbolEntry = lookupTable.get(identifier);
        if (oldSymbolEntry != null) {
            Symbol oldSymbol = oldSymbolEntry.getSymbol();
            if (symbol instanceof Function && ((Function)symbol).isDefined() && (oldSymbol instanceof Variable || oldSymbol instanceof Argument)
            || oldSymbol instanceof Function && ((Function)oldSymbol).isDefined() && (symbol instanceof Variable || symbol instanceof Argument)
            || oldSymbol instanceof Function && symbol instanceof Function && !( !((Function)oldSymbol).isDefined() && ((Function)symbol).isDefined())
            || !(oldSymbol instanceof Function && symbol instanceof Function) && oldSymbolEntry.getScope() == curScope) {
                throw new SemanticException("Semantic error: symbol \'" + identifier +"\' at " + Symbol.getLocation(symbol.getToken()) +
                                            " is already defined at " + Symbol.getLocation(oldSymbolEntry.getSymbol().getToken()));
            }
        }
        SymbolEntry newSymbolEntry = new SymbolEntry(symbol, curScope, oldSymbolEntry);
        symbolList.push(newSymbolEntry);
        lookupTable.put(identifier, newSymbolEntry);

        System.out.println(TreeVisitor.ANSI_BLUE + "Inserting " + newSymbolEntry + TreeVisitor.ANSI_RESET);
    }

    public Symbol lookup(Token token) throws SemanticException {
        SymbolEntry symbolEntry = lookupTable.get(token.getText());
        if (symbolEntry == null) {
            throw new SemanticException("Semantic error: undeclared symbol \'" + token.getText() +"\' at " + Symbol.getLocation(token));
        }
        return symbolEntry.getSymbol();
    }

    public boolean onFirstScope() {
        return curScope == -1;
    }

    @Override
    public String toString() {
        return symbolList.toString();
    }

    /* A function name may appear twice only if first one was declaration and current one is definition
    private static boolean duplicateFunction(Symbol oldSymbol, Symbol symbol) {
        return oldSymbol instanceof Function && symbol instanceof Function && !( !((Function)oldSymbol).isDefined() && ((Function)symbol).isDefined());
    }*/

    private class SymbolEntry {
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
