package compiler;

import compiler.analysis.DepthFirstAdapter;
import compiler.node.*;

import java.util.ArrayDeque;
import java.util.HashMap;
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
        this.curScope++;
    }

    public void insert(String identifier) throws SemanticException {
        Symbol oldSymbol = lookupTable.get(identifier);
        if (oldSymbol != null && oldSymbol.getScope() == curScope) {
            throw new SemanticException("Semantic error: symbol " + oldSymbol + " at line "  +
                                         "is already defined at current scope at line " + oldSymbol.getLine() + ", column " + oldSymbol.getPos());
        }
        System.out.println("Success");
    }

    private class Symbol {
        private String identifier;
        private long scope;
        private Symbol shadowedSymbol;
        private long line;
        private long pos;

        public Symbol(String identifier, long scope, long line, long pos) {
            this.identifier = identifier;
            this.scope = scope;
            this.line = line;
            this.pos = pos;
        }

        public String getIdentifier() {
            return identifier;
        }

        public long getScope() {
            return scope;
        }

        public long getLine() {
            return line;
        }

        public long getPos() {
            return pos;
        }

        public Symbol getShadowedSymbol() {
            return shadowedSymbol;
        }

        @Override
        public String toString() {
            return identifier + (shadowedSymbol != null ? " with shadowing" : "");
        }
    }

}
