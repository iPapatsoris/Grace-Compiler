import java_cup.runtime.*;
%%

%class Scanner
%line
%column
%cup

%{
StringBuffer stringBuffer = new StringBuffer();

private Symbol symbol(int type) {
   return new Symbol(type, yyline, yycolumn);
}

private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
}

private static Character formEscape(String str) {System.out.println(str);
    if (str.charAt(0) == 'x') {
        return (char)Integer.parseInt(str.substring(1, 3), 16);
    }
    switch (str) {
        case "n'": return '\n';
        case "t'": return '\t';
        case "r'": return '\r';
        case "0'": return 0;
        case "\\'": return '\\';
        case "''": return '\'';
        case "\"'": return '\"';
        default: System.out.println("No match"); return null;
    }
}

%}

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]
//LegalAfterDigit = [+\-*();] | {WhiteSpace}

%state STRING
%xstate ESCAPE

%%

<YYINITIAL> {

/* Keywords */
"and"  {return symbol(sym.AND);}
"char" {return symbol(sym.CHAR);}
"div" {return symbol(sym.DIV);}
"do" {return symbol(sym.DO);}
"else" {return symbol(sym.ELSE);}
"fun" {return symbol(sym.FUN);}
"if" {return symbol(sym.IF);}
"int" {return symbol(sym.INT);}
"mod" {return symbol(sym.MOD);}
"not" {return symbol(sym.NOT);}
"nothing" {return symbol(sym.NOTHING);}
"or" {return symbol(sym.OR);}
"ref" {return symbol(sym.REF);}
"return" {return symbol(sym.RETURN);}
"then" {return symbol(sym.THEN);}
"var" {return symbol(sym.VAR);}
"while" {return symbol(sym.WHILE);}


/* Operators */
"+"            { return symbol(sym.PLUS); }
"-"            { return symbol(sym.MINUS); }
"*"            { return symbol(sym.TIMES); }
"("            { return symbol(sym.LPAREN); }
")"            { return symbol(sym.RPAREN); }
";"            { return symbol(sym.SEMI); }
\"             { stringBuffer.setLength(0); }// yybegin(STRING); }
{WhiteSpace}   { }
}

[a-zA-Z][a-zA-Z0-9_]* {return symbol(sym.ID, yytext()); }
[0-9]+ {return symbol(sym.INT_LITERAL, new Integer(yytext()));}
[0-9]+([^+\-*();\n\t\r ])+  {throw new Error("Illegal character <"+yytext()+"> at line " + yyline + ", column " + yycolumn); }
\'[^\\'\"]\' {return symbol(sym.CHAR_LITERAL, yytext().charAt(1));}
\'\\ {System.out.println("OK"); yybegin(ESCAPE);}

<STRING> {
      \"                             { yybegin(YYINITIAL);
                                       return symbol(sym.STRING_LITERAL, stringBuffer.toString()); }
      [^\n\r\"\\]+                   { stringBuffer.append( yytext() ); }
      \\t                            { stringBuffer.append('\t'); }
      \\n                            { stringBuffer.append('\n'); }

      \\r                            { stringBuffer.append('\r'); }
      \\\"                           { stringBuffer.append('\"'); }
      \\                             { stringBuffer.append('\\'); }
}

<ESCAPE> {
    ([ntr0\\'\"] | x([0-9a-fA-F]{2}))'  { System.out.println("OKKK"); yybegin(YYINITIAL); return symbol(sym.CHAR_LITERAL, formEscape(yytext()));}
    [^]                    { throw new Error("Illegal character <"+yytext()+">"); }

}

[^]                    { throw new Error("Illegal character <"+yytext()+">"); }
