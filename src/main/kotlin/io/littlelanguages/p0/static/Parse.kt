package io.littlelanguages.p0.static

import io.littlelanguages.data.Either
import io.littlelanguages.data.Left
import io.littlelanguages.data.Right
import io.littlelanguages.p0.Errors
import io.littlelanguages.p0.ParseError
import io.littlelanguages.p0.static.ast.Program
import io.littlelanguages.p0.static.ast.Visitor

fun parse(scanner: Scanner, visitor: Visitor): Either<Errors, Program> =
        try {
            Right(Parser(scanner, visitor).program())
        } catch (e: ParsingException) {
            Left(ParseError(e.found, e.expected))
        }
