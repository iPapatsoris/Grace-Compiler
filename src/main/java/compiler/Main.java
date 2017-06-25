package compiler;

import compiler.lexer.Lexer;
import compiler.lexer.LexerException;
import compiler.parser.Parser;
import compiler.parser.ParserException;
import compiler.node.Start;
import compiler.tree_visitor.TreeVisitor;

import java.io.PushbackReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.Runtime;

public class Main {

    public static void main(String args[]) {
        Options options = new Options(args);
        Start tree = null;
        try {
            PushbackReader reader = new PushbackReader(new FileReader(options.getInput()), 1024);
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
        TreeVisitor treeVisitor = new TreeVisitor(options.getOutputCode(),
                                                  options.getPrintAST(),
                                                  options.getOptimize());
        tree.apply(treeVisitor);
        if (options.getOptimize()) {
            treeVisitor.getOptimizer().print();
        }
        else if (options.getPrintIR()) {
            treeVisitor.getIR().print();
        }
        System.exit(0);
    }

    public static class Options {
        private String input;
        private String outputCode;
        private boolean printAST;
        private boolean printIR;
        private boolean optimize;

        public Options(String args[]) {
            input = null;
            printAST = false;
            printIR = false;
            optimize = false;
            for (int i = 0 ; i < args.length ; i++) {
                switch (args[i]) {
                    case "-ast":
                        printAST = true;
                        break;
                    case "-ir":
                        printIR = true;
                        break;
                    case "-opt":
                        optimize = true;
                        break;
                    default:
                        input = args[i];
                }
            }
            if (input == null) {
                throw new IllegalArgumentException("No input file");
            }
            int suffixIndex = input.lastIndexOf('.');
            if (suffixIndex < 0) {
                suffixIndex = input.length();
            }
            outputCode = input.substring(0, suffixIndex) + ".s";
        }

        public String getInput() {
            return input;
        }

        public String getOutputCode() {
            return outputCode;
        }

        public boolean getPrintAST() {
            return printAST;
        }

        public boolean getPrintIR() {
            return printIR;
        }
        public boolean getOptimize() {
            return optimize;
        }
    }
}
