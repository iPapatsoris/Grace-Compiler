fun main() : nothing
    $$var myVar: int;
    var myOtherVar : int;
    var ne : char;
    var ti: int;
    var neti: char;
    var array: char[5];$$
    var arrayInt: int[6];
    var k: int;
    $$fun foo(i: int) : int
    {
        return 10;
        puts("str");
    }$$
{
    $myVar <- 1;
    $$strcpy(array, "abc");
    puts(array);
    strcat(array, "d");
    puts(array);
    k <- strlen(array);
    puti(k);
    putc('\n');$$

    k <- 3;
    arrayInt[3] <- 4;
    puti(arrayInt[3]);


$$    ne <- 'd';
    neti <- ne;
    putc(getc());
    ti <- geti();
    puti(ti);

    puti(abs(ti));

    putc(chr(ti));
    puti(ord(chr(ti)));

    puti(foo(9));$$
}
