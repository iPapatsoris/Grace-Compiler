# Grace-Compiler
A full compiler for the Grace programming language

## Instructions
 * Compile and create a packaged JAR file with `mvn package`
 * Run with `java -cp target/compiler-1.0-SNAPSHOT.jar compiler.Main <input_file>`
 * Clean with `mvn clean`

## Output
We print the name of each Node in the CST in a DFS manner along with the
matched input. This is implemented by simply overriding the *Node.defaultIn* method.

## Notes
* reduce dimenionality by passing to method: temp var types DEN diakrinontai ws arrays
* tempVar gia ola ta ReturnInfo, meta isws perioristei
* 'a'mod 2 apodekto?

## Samples
`fun main() : nothing
    fun foo(ref array : int[5][6]) : nothing {}
    var myVar : int[5][6][7];
{
    foo(myVar[0]);
}`

`fun main() : nothing
    var myVar :int[15][123];
    fun myFun(ref myVar: int[][1234]): char
        fun myOtherFun(): int
        {}
    {
    }
{
    myVar["Asdf"[myFun(myVar)]] <- 12;
}
`

`fun main() : nothing
    fun myFun(myInt: int): char
        fun myOtherFun(): int
        {myFun(myOtherFun());}
    {}
{
}
`

`fun main() : nothing
    fun myFun(myInt: char): char
        fun myOtherFun(): int
        {}
    {}
{
    i <- 13 + myFun(myFun('2'));
}`

`fun main() : nothing
    fun myFun(myVar : int) : nothing;
    var myVar : int;
    fun myFun(myVar : int) : nothing
    {}
{}
`

`fun main() : nothing
    fun myFun(myInt1, myInt2 : int ; ref myChar : char) : int
        fun myOtherFun(ref myArray: int[12]; ref myOtherArray: char[][14][15]) : char
        {}
    {}
{}
`

`fun main() : nothing
    var myInt1, myInt2 : int;
    var myChar : char;
    fun myFun() : int
        var myInt1 : char;
        var myArray: int[13][14];
    {}
{}
`

`fun main() : nothing
    fun myFun() : nothing
        fun myOtherFun() : nothing;
    {}
    fun myOtherFun() : nothing
    {}
{}
`

`fun myFun() : nothing
    var a : int;
    var b : char;
    var c : int;

    fun myOtherFun() : char
        var d: int;
        var a: int;

        fun lel() : int
            var d: int;
            var a: int; {}
        fun lel2() : int
            var d: int;
            var a: int; {}
        {}
{
}`

-

`a['a'][++--(1-+-1)]`

`a[+-+-+-(b["c"[2]])]`

`array[func   (a,1+2, func2("Asf", func3()))]`

`1 >= 2 and (3 <= 4 or 5 = 6) and not 6 # 5`

`if condition(123) > 0 then
    if (50 < 0) then {
        return crazy_func(123, a[b[123]]);
        a <- neti;
    }
    else
        a[123*4] <- myfun();
    return "OK";
while (x > 0)  do {
    while y > 0 do
        ;
}
`

`ref myVar, mySecondVar: char [] [1] [23]`

`fun myFunc (ref myVar, mySecondVar: char [] [1] [23] ; myThirdVar: int [] ; myFourthVar: int [2]) : nothing`

`var myVar1, myVar2 : int [12][15][2];`
