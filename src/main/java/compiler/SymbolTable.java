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

    public void exit() {
        for (Iterator<SymbolEntry> it = symbolList.iterator(); it.hasNext(); ) {
            SymbolEntry symbolEntry = it.next();
            if (symbolEntry.getScope() != curScope) {
                break;
            }
            if (symbolEntry.getShadowedSymbolEntry() != null) {
                lookupTable.put(symbolEntry.getSymbol().getToken().getText(), symbolEntry.getShadowedSymbolEntry());
            } else {
                lookupTable.remove(symbolEntry.getSymbol().getToken().getText());
            }
            System.out.println(TreeVisitor.ANSI_BLUE + "Removing " + symbolEntry + TreeVisitor.ANSI_RESET);
            it.remove();
        }
        curScope--;
        System.out.println(TreeVisitor.ANSI_BLUE + "After exit: " + this + TreeVisitor.ANSI_RESET);
    }

    public void insert(Symbol symbol) throws SemanticException {
        String identifier = symbol.getToken().getText();
        SymbolEntry oldSymbolEntry = lookupTable.get(identifier);
        if (oldSymbolEntry != null && oldSymbolEntry.getScope() == curScope) {
            throw new SemanticException("Semantic error: symbol \'" + identifier +"\' at " + getLocation(symbol.getToken()) +
                                        " is already defined at current scope at " + getLocation(oldSymbolEntry.getSymbol().getToken()));
        }
        SymbolEntry newSymbolEntry = new SymbolEntry(symbol, curScope, oldSymbolEntry);
        symbolList.push(newSymbolEntry);
        lookupTable.put(identifier, newSymbolEntry);

        System.out.println(TreeVisitor.ANSI_BLUE + "Inserted " + newSymbolEntry + ":\n" + this + TreeVisitor.ANSI_RESET);
    }

    public Symbol lookup(Token token) throws SemanticException {
        SymbolEntry symbolEntry = lookupTable.get(token.getText());
        if (symbolEntry == null) {
            throw new SemanticException("Semantic error: undeclared symbol \'" + token.getText() +"\' at " + getLocation(token));
        }
        return symbolEntry.getSymbol();
    }

    @Override
    public String toString() {
        return symbolList.toString();
    }

    private static String getLocation(Token token) {
        return "[" + token.getLine() + "," + token.getPos() + "]";
    }

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
            return symbol + " at scope " + scope; //return token.getText() + " at scope " + scope + " found at " + getLocation(token) + (shadowedSymbol != null ? " overshadowing the one at " + getLocation(shadowedSymbol.getToken()) : "");
        }
    }

}
