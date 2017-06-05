fun main() : nothing
    var myVar: int;
    var myOtherVar : int;
    var ne : char;
    var ti: int;
    var neti: char;
    fun foo(i: int) : int
    {
        return 10;
    }
{
    ne <- 'd';
    neti <- ne;
    putc(getc());
    ti <- geti();
    puti(ti);

    puti(abs(ti));

    putc(chr(ti));
    puti(ord(chr(ti)));

    puti(foo(9));
}
