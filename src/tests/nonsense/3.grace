fun main () : nothing
  var r : char;
  var x: char;

  fun reverse () : nothing
    fun foo() : nothing
        fun bar() : nothing
        {
            r <- 'c';
            putc(r);
            main();
        }
    {
        r <- 'b';
        putc(r);
        bar();
        putc(r);
    }
  {
    r <- 'a';
    putc(r);
    foo();
    putc(r);
  }

{
    r <- 'z';
if (r = 'c') then {puts("ni"); return;}
  reverse();
  putc(r);
}
