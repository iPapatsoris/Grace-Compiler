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
    fun bar(ref i: int[]; neti: char) : nothing {
        puti(i[4]); return;
        putc(neti);
        i[4] <- 57;
        putc(neti);
        puti(i[4]);
        neti <- 'z';
        putc(neti);
    }
{
    arrayInt[4] <- 6;
    arrayInt[8] <- 7;
    puti(foo(arrayInt[4], arrayInt[8]));
    puti(arrayInt[4]);
    puti(arrayInt[8]);

    $$neti <- 'a';
    bar(arrayInt, neti);
    putc(neti);$$




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
