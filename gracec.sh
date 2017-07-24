#!/bin/bash
exec java -cp target/compiler-1.0-SNAPSHOT.jar compiler.Main "$@"
