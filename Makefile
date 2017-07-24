# This makefile is not an actual makefile, but a mere
# maven wrapper, solely for the instructors' convenience

all: 
	gcc -m32 -S -o src/main/standard-library/sl.s src/main/standard-library/sl.c 
	mvn package

clean:
	mvn clean
