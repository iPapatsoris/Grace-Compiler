package compiler;

import compiler.lexer.Lexer;
import compiler.lexer.LexerException;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import compiler.node.Start;

import java.io.PushbackReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {

    public static void main(String args[]) {
        Start tree = null;
        try {
            PushbackReader reader = new PushbackReader(new InputStreamReader(System.in));
            Parser p = new Parser(new Lexer(reader));
            tree = p.parse();
        } catch (Exception e) {
            if (e instanceof IOException) {
                System.err.println("I/O error: " + e.getMessage());
            } else if (e instanceof LexerException) {
                System.err.println("Lexing error: " + e.getMessage());
            } else if (e instanceof ParserException) {
                System.err.println("Parsing error: " + e.getMessage());
            }
            System.exit(1);
        }
        tree.apply(new PrintTree());
        /*Lexer lexer = new Lexer(reader);
        for(;;) {
            try {
            	Token t = lexer.next();
        		if (t instanceof EOF)
                	break;
            	System.out.println(t.getClass() + ": " +  t.toString());
        	} catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }*/
        System.exit(0);
    }
}
