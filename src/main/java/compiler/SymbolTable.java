package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;

public class SymbolTable {

    private ArrayDeque<Symbol> symbolList;
    private HashMap<String, Symbol> lookupTable;
    private long curScope;

    public SymbolTable() {
        this.symbolList = new ArrayDeque<Symbol>();
        this.lookupTable = new HashMap<String, Symbol>();
        this.curScope = -1;
    }

    public void enter() {
        curScope++;
    }

    public void exit() {
        for (Iterator<Symbol> it = symbolList.iterator(); it.hasNext(); ) {
            Symbol symbol = it.next();
            if (symbol.getScope() != curScope) {
                break;
            }
            lookupTable.put(symbol.getToken().getText(), symbol);
            System.out.println("Removing " + symbol);
            it.remove();
        }
        curScope--;
    }

    public void insert(Token token) throws SemanticException {
        Symbol oldSymbol = lookupTable.get(token.getText());
        if (oldSymbol != null && oldSymbol.getScope() == curScope) {
            throw new SemanticException("Semantic error: symbol \'" + token.getText() +"\' at " + getLocation(token) +
                                        " is already defined at current scope at " + getLocation(oldSymbol.getToken()));
        }
        Symbol newSymbol = new Symbol(token, curScope, oldSymbol);
        symbolList.addFirst(newSymbol);
        lookupTable.put(token.getText(), newSymbol);

        System.out.println("Inserted " + newSymbol);
    }

    public Symbol lookup(Token token) throws SemanticException {
        Symbol symbol = lookupTable.get(token.getText());
        if (symbol == null) {
            throw new SemanticException("Semantic error: undeclared symbol \'" + token.getText() +"\' at " + getLocation(token));
        }
        return symbol;
    }

    private static String getLocation(Token token) {
        return "[" + token.getLine() + "," + token.getPos() + "]";
    }

    private class Symbol {
        private Token token;
        private long scope;
        private Symbol shadowedSymbol;

        public Symbol(Token token, long scope, Symbol shadowedSymbol) {
            this.token = token;
            this.scope = scope;
            this.shadowedSymbol = shadowedSymbol;
        }

        public Token getToken() {
            return token;
        }

        public long getScope() {
            return scope;
        }

        public Symbol getShadowedSymbol() {
            return shadowedSymbol;
        }

        @Override
        public String toString() {
            return token.getText() + " at scope " + scope + (shadowedSymbol != null ? " with shadowing" : "");
        }
    }

}
