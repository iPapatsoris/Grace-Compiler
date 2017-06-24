
fun main() : nothing
    var x: int[70][150][26];
    var i,j,c: int;
    fun printArray(ref x: int[150][26]; ref y: int[26]) : nothing
    {
        i <- 0;
        while (i < 15) do {
            j <- 0;
            while (j < 15) do {
                puti(x[i][j]); putc(' ');
                j <- j+1;
            }
            putc('\n');
            i <- i+1;
        }
        puts("\ny is:\n");
        i <- 0;
        while (i < 15) do {
            puti(y[i]); putc(' ');
            i <- i+1;
        }
    }
{
    c <- 0;
    i <- 0;
    while (i < 15) do {
        j <- 0;
        while (j < 15) do {
            x[69][i][j] <- c;
            c <- c+1;
            j <- j+1;
        }
        i <- i+1;
    }
    printArray(x[69], x[69][5]);
    putc('\n');
}
