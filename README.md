# Grace-Compiler
A full compiler for the Grace programming language

## Instructions
 * Compile and create a packaged JAR file with `mvn package`
 * Run with `java -cp target/compiler-1.0-SNAPSHOT.jar compiler.Main <input_file>`
 * Clean with `mvn clean`

## Output
We print the name of each Node in the CST in a DFS manner along with the
matched input. This is implemented by simply overriding the *Node.defaultIn* method.
