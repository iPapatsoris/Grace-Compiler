fun main() : nothing
    var s : char[32][33];
    fun foo(ref s: char[]) : nothing {
        puts(s); return;
    }
{
    strcpy(s[32], "hello");
    foo(s[32]);
}
