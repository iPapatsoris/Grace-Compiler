fun main() : nothing
    var x : int[10][11][12];
    var y: int[123][45];
    fun foo(ref x: int[]; y: int): char {return 'a';}
    fun bar() : int {return 1;}
{
    foo(x[1][2], bar());
}
