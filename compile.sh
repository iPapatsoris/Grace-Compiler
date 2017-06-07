java -cp target/compiler-1.0-SNAPSHOT.jar compiler.Main src/tests/input.grace 
 gcc -m32 src/tests/input.s src/main/standard-library/sl.s
./a.out 
