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

private static Character formEscape(String str) {
    if (str.charAt(0) == 'x') {
        return (char)Integer.parseInt(str.substring(1, 3), 16);
    }
    switch (str) {
        case "n": return '\n';
        case "t": return '\t';
        case "r": return '\r';
        case "0": return 0;
        case "\\": return '\\';
        case "'": return '\'';
        case "\"": return '\"';
        default: System.out.println("No match"); return null;
    }
}

%}

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]
EscapeChar = [ntr0\\'\"] | x([0-9a-fA-F]{2})
//LegalAfterDigit = [+\-*();] | {WhiteSpace}

%xstate STRING, LINE_COMMENT, BLOCK_COMMENT

%%

<YYINITIAL> {

    /* Keywords */
    "and"  { return symbol(sym.AND);}
    "char" { return symbol(sym.CHAR);}
    "div" { return symbol(sym.DIV);}
    "do" { return symbol(sym.DO);}
    "else" { return symbol(sym.ELSE);}
    "fun" { return symbol(sym.FUN);}
    "if" { return symbol(sym.IF);}
    "int" { return symbol(sym.INT);}
    "mod" { return symbol(sym.MOD);}
    "not" { return symbol(sym.NOT);}
    "nothing" { return symbol(sym.NOTHING);}
    "or" { return symbol(sym.OR);}
    "ref" { return symbol(sym.REF);}
    "return" { return symbol(sym.RETURN);}
    "then" { return symbol(sym.THEN);}
    "var" { return symbol(sym.VAR);}
    "while" { return symbol(sym.WHILE);}

    /* Operators */
    "+"            { return symbol(sym.PLUS); }
    "-"            { return symbol(sym.MINUS); }
    "*"            { return symbol(sym.TIMES); }
    "#"            { return symbol(sym.HASH); }
    "="            { return symbol(sym.EQ); }
    "<>"            { return symbol(sym.NEQ); }
    "<"            { return symbol(sym.LESS); }
    ">"            { return symbol(sym.GREATER); }
    "<="            { return symbol(sym.LESSEQ); }
    ">="            { return symbol(sym.GREATEREQ); }

    /* Delimiters */
    "("            { return symbol(sym.LPAREN); }
    ")"            { return symbol(sym.RPAREN); }
    "["            { return symbol(sym.LSQR); }
    "]"            { return symbol(sym.RSQR); }
    "{"            { return symbol(sym.LBRACK); }
    "}"            { return symbol(sym.RBRACK); }
    ","            { return symbol(sym.COMMA); }
    ";"            { return symbol(sym.SEMICOLON); }
    ":"            { return symbol(sym.COLON); }
    "<-"           { return symbol(sym.LARROW);}

    [a-zA-Z][a-zA-Z0-9_]* {return symbol(sym.ID, yytext()); }
    [0-9]+ {return symbol(sym.INT_CONST, new Integer(yytext()));}
    [0-9]+([^+\-*/#=<>()\[\]{},;:\n\t\r ])+  {throw new Error("Illegal character <"+yytext()+"> at line " + yyline + ", column " + yycolumn); }
    \'[^\\'\"]\' {return symbol(sym.CHAR_CONST, yytext().charAt(1));}
    \'\\{EscapeChar}\' {
        String toEscape = yytext().substring(2, yytext().length()-1);
        return symbol(sym.CHAR_CONST, formEscape(toEscape));
    }
    \" { stringBuffer.setLength(0); yybegin(STRING); }
    \$\n { }
    \$[^\n$] { yybegin(LINE_COMMENT); }
    \$\$ { yybegin(BLOCK_COMMENT); }
    {WhiteSpace} { }
}

<STRING> {
    \" {
        yybegin(YYINITIAL);
        return symbol(sym.STRING_LITERAL, stringBuffer.toString());
    }
    [^\n\r\\\"']+ { stringBuffer.append(yytext()); }
    \\{EscapeChar} {
        String toEscape = yytext().substring(1, yytext().length());
        stringBuffer.append(formEscape(toEscape));
    }
}

<LINE_COMMENT> {
    \n { yybegin(YYINITIAL); }
    [^] { }
}

<BLOCK_COMMENT> {
    \$\$ { yybegin(YYINITIAL); }
    [^$] { }
}

[^]                    { throw new Error("Illegal character <"+yytext()+">"); }
