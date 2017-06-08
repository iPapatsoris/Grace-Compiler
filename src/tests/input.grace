fun main() : nothing
    var myVar: int;
    var myOtherVar : int;
    var ne : char;
    var ti: int;
    var neti: char;
    var array: char[4];
    var arrayInt: int[500];
    var multi: int[3][5];
    var multiChar: char[10][11][12][13];
    var k, x,y,z: int;
    fun foo(ref i: int; ref j: int) : int
    {
        puti(i);
        i <- 57;
        puti(i);
        puti(j);
        return i;
    }
    fun bar(ref i: int[]; neti: char) : nothing
        fun nestedBar(ref i : int[]; ref neti: char; ref charArray: char[]) : nothing
        {
            puti(i[4]);
            i[4] <- 23;
            puti(i[4]);
            putc(neti);
            neti <- 'b';
            putc(neti);
            putc(charArray[6]);
            charArray[6] <- 'u';

        }
        var charArray: char[7];
    {
        puti(i[4] * i[4]);
        putc(neti);
        i[4] <- 57;
        putc(neti);
        puti(i[4]);
        neti <- 'z';
        putc(neti);
        putc('\n');

        charArray[6] <- 'y';
        nestedBar(i, neti, charArray);
        puti(i[4]);
        putc(neti);
        putc(charArray[6]);

    }
{
    x <- geti();
    y <- geti();
    z <- geti();

    while ((x < y and y < z) or y = 100) do {
        puts("ni");
        y <- geti();
    }


}
