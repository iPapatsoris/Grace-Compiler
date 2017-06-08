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
    var k: int;
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
        puti(i[4]);
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
    arrayInt[4] <- 6;
    $$arrayInt[8] <- 7;
    foo(arrayInt[4], arrayInt[8]);
    puti(arrayInt[4]);
    puti(arrayInt[8]);$$

    neti <- 'a';
    bar(arrayInt, neti);
    puti(arrayInt[4]);
    putc(neti);




    $$myVar <- 1;
    strcpy(array, "abc");
    puts(array);
    strcat(array, "d");
    puts(array);
    k <- strlen(array);
    puti(k);
    putc('\n');

    k <- 5;
    arrayInt[k] <- 10;
    puti(arrayInt[k]);
    array[3] <- 'w';
    putc(array[3]);
    putc('\n');

    array[0] <- 'n';
    array[1] <- 'e';
    array[2] <- '\n';
    array[3] <- '\0';

    $strcpy(array, "ne");
    puts(array);
    putc(array[0]);
    putc(array[1]);
    putc(array[2]);
    $putc(array[3]);

    ne <- 'd';
    neti <- ne;
    putc(getc());
    ti <- geti();
    puti(ti);

    puti(abs(ti));

    putc(chr(ti));
    puti(ord(chr(ti)));

    puti(foo(9));

    multi[2][3] <- 13;
    multiChar[2][3][4][5] <- 'r';
    puti(multi[2][3]);
    putc(multiChar[2][3][4][5]);  $$
}
