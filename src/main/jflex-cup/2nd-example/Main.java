import java_cup.runtime.*;
import java.io.*;

class Main {
    public static void main(String[] argv) throws Exception{
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();
    }
}
/*
class Main {
    public static void main(String[] argv) throws Exception {
        Symbol currToken;
        InputStreamReader input = new InputStreamReader(System.in);
        try {
            Scanner scanner = new Scanner(input);
            do {
                currToken = scanner.next_token();
                System.out.println(currToken.sym + " " +  ((currToken.value != null) ? currToken.value : scanner.yytext()));
            } while (currToken.sym != sym.EOF);
        } catch (Exception e) {
            throw new RuntimeException("IO Error (brutal_Exit)" + e.toString());
        }
    }
}
*/
