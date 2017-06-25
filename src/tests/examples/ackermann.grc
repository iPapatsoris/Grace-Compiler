
fun main() : nothing
    var x,y: int;
    fun ackermann(x,y: int) : int
    {
        if (x = 0) then {
            return y + 1;
        } else if (y = 0) then {
            return ackermann(x - 1, 1);
        } else {
            ackermann(x - 1, ackermann(x, y - 1));
        }
    }
{
    puts("x: ");
    x <- geti();
    puts("y: ");
    y <- geti();
    if (x < 0 or y < 0) then {
        return;
    }
    puts("Ackermann(");
    puti(x);
    puts(",");
    puti(y);
    puts(") = ");
    puti(ackermann(x, y));
    putc('\n');
}
