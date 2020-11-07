# P0 - Kotlin

A Kotlin implementation of P0

## Building Source

The directory `~/.devcontainer` contains a Dockerfile used by [Visual Studio Code](https://code.visualstudio.com) to issolate the editor and build tools from being installed on the developer's workstation.

This Dockerfile installs:

- Java JDK 1.15
- Gradle 6.7
- Deno 1.4.6
- [entr](https://github.com/eradman/entr/)

The Dockerfile is straightforward with the interesting piece being [entr](https://github.com/eradman/entr/) which is used by the `etl.sh` to run `test.sh` whenever a source file has changed.

## Scripts

Three script can be found inside `~/.bin`

| Name   | Purpose |
|--------|----------------------------------|
| build.sh | Builds the scanner and parser in the event that the lexical ([./src/main/kotlin/io/littlelanguages/p0/static/Scanner.llld](./src/main/kotlin/io/littlelanguages/p0/static/Scanner.llld)) and syntactic ([./src/main/kotlin/io/littlelanguages/p0/static/Grammar.llgd](./src/main/kotlin/io/littlelanguages/p0/static/Grammar.llgd)) definitions have been changed. |
| etl.sh | Runs an edit-test-loop - loops indefinately running all of the tests whenever a source file has changed. |
| test.sh | Runs lint on the source code and executes the automated tests. |

These scripts must be run out of the project's root directory which, when using [Visual Studio Code](https://code.visualstudio.com), is done using a shell inside the container.

