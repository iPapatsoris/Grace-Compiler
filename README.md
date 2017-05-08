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
vasi enotitas 1.4.1, a["asdf"] de simainei kati, parolo pou einai syntaktika swsto.

## Samples
`a['a'][++--(1-+-1)]`
`a[+-+-+-(b["c"[2]])]`
`array[func   (a,1+2, func2("Asf", func3()))]`
`1 >= 2 and (3 <= 4 or 5 = 6) and not 6 # 5`
