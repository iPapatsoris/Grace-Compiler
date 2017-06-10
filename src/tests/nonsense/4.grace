fun main() : nothing
{
    puts("sting: tab\ttab\ncarriage return\rbackslash\\\nquote\'\ndouble quote\"\n\nchars:");
    putc('t'); putc('\t'); putc('t');
    putc('\n');
    putc('\r');
    putc('\\');
    putc('\n');
    putc('\'');
    putc('\n');
    putc('\"');

    puts("\x6b <- string ");
    putc('\x6b');
}
