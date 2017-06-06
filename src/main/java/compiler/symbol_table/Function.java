package compiler.symbol_table;

import compiler.node.*;
import java.lang.String;
import java.util.ArrayDeque;
import java.util.Iterator;

public class Function extends Symbol {
    private ArrayDeque<Argument> arguments;
    private boolean defined;

    public Function(Token token, ArrayDeque<Argument> arguments, Type type, boolean defined) {
        super(token, type);
        this.arguments = arguments;
        this.defined = defined;
    }

    public ArrayDeque<Argument> getArguments() {
        return arguments;
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean sameHeader(Function function) {
        return this.token.getText().equals(function.token.getText())
               && this.type == function.type
               && equalDeque(this.arguments, function.arguments);
    }

    @Override
    public String toString() {
        return super.toString() + " " + defined + " " + arguments ;
    }

    /* Dequeue does not implement Object.equals . Either implement ArrayDeque or use this method */
    private static boolean equalDeque(ArrayDeque<Argument> queue1, ArrayDeque<Argument> queue2) {
        if (queue1.size() != queue2.size()) {
            return false;
        }

        Iterator<Argument> it2 = queue2.iterator();
        for (Iterator<Argument> it1 = queue1.iterator(); it1.hasNext(); ) {
            if (! it1.next().equals(it2.next())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStandardLibrary(String function) {
        return function.equals("_puti_-1") || function.equals("_putc_-1") || function.equals("_puts_-1") ||
               function.equals("_geti_-1") || function.equals("_getc_-1") || function.equals("_gets_-1") ||
               function.equals("_abs_-1") || function.equals("_ord_-1") || function.equals("_chr_-1") ||
               function.equals("_strlen_-1") || function.equals("_strcpy_-1") || function.equals("_strcmp_-1") ||
               function.equals("_strcat_-1");
    }
}
