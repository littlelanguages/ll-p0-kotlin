#!/bin/bash

deno run --allow-read --allow-write --allow-net "https://raw.githubusercontent.com/littlelanguages/scanpiler-cli/main/mod.ts" kotlin --verbose --directory=src/main/kotlin --package=io.littlelanguages.p0.lexer.Scanner src/main/kotlin/io/littlelanguages/p0/lexer/Scanner.llld

./gradlew build