package io.littlelanguage.p0.lexer

enum class TToken {
    TEOS, TERROR,

    TSingleLine, TMultiLine,

    TConst, TElse, TFalse, TFun, TIf, TLet, TReturn, TTrue, TWhile,

    TBool, TFloat, TInt,

    TAmpersandAmpersand, TBang, TBangEqual, TBarBar, TColon, TComma, TEqual, TEqualEqual, TGreaterEqual, TGreaterThan, TLCurly, TLessEqual, TLessThan,
    TLParen, TMinus, TPlus, TQuestion, TRCurly, TRParen, TSemicolon, TSlash, TStar,

    TIdentifier, TLiteralInt, TLiteralFloat, TLiteralString
}


data class Token(val tToken: TToken, val position: Position, val lexeme: String) {
    override fun toString(): String {
        fun pp(position: Position): String =
                when (position) {
                    is PositionCoordinate -> "${position.offset}:${position.line}:${position.column}"
                    is PositionRange -> pp(position.start) + "-" + pp(position.end)
                }

        return tToken.toString().drop(1) + " " + pp(position) + " [" + lexeme + "]"
    }
}