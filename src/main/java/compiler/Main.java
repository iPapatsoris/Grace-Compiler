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
                                                  options.getPrintAST());
        tree.apply(treeVisitor);
        if (options.getPrintIR()) {
            treeVisitor.getIR().print();
        }

        /* Not secure */
        try {
            Runtime.getRuntime().exec("gcc -m32 " + options.getOutputCode() +
                                  " src/main/standard-library/sl.s" +
                                  (options.getOutputProgram() != null ?
                                   " -o " + options.getOutputProgram() : ""));
        } catch (IOException e) {
            System.err.println("I/O error in producing binary from final code: " +
                                e.getMessage());
        }
        System.exit(0);
    }

    public static class Options {
        String input;
        String outputCode;
        String outputProgram;
        boolean printAST;
        boolean printIR;

        public Options(String args[]) {
            input = null;
            outputProgram = null;
            printAST = false;
            printIR = false;
            for (int i = 0 ; i < args.length ; i++) {
                switch (args[i]) {
                    case "-ast":
                        printAST = true;
                        break;
                    case "-ir":
                        printIR = true;
                        break;
                    case "-o":
                        if (++i >= args.length) {
                            throw new IllegalArgumentException("No output program file");
                        }
                        outputProgram = args[i];
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

        public String getOutputProgram() {
            return outputProgram;
        }

        public boolean getPrintAST() {
            return printAST;
        }

        public boolean getPrintIR() {
            return printIR;
        }
    }
}
