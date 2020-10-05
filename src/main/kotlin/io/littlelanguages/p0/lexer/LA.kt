package io.littlelanguages.p0.lexer

import java.io.Reader

private val keywords = mapOf(
        Pair("const", TToken.TConst),
        Pair("else", TToken.TElse),
        Pair("false", TToken.TFalse),
        Pair("fun", TToken.TFun),
        Pair("if", TToken.TIf),
        Pair("let", TToken.TLet),
        Pair("return", TToken.TReturn),
        Pair("true", TToken.TTrue),
        Pair("while", TToken.TWhile),
        Pair("Bool", TToken.TBool),
        Pair("Float", TToken.TFloat),
        Pair("Int", TToken.TInt))

class LA(private val reader: Reader, private val ignoreComments: Boolean = true) {
    private var offset: Int = -1
    private var line: Int = 1
    private var column: Int = 0
    private var nextCh = reader.read()

    private var lexeme: StringBuilder? = null
    private var startOffset: Int? = null
    private var startLine: Int? = null
    private var startColumn: Int? = null

    private lateinit var currentToken: Token

    val current: Token
        get() = currentToken

    init {
        setToken(TToken.TEOS)

        next()
    }

    fun next() {
        if (nextCh == -1) {
            if (currentToken.tToken != TToken.TEOS) {
                setToken(TToken.TEOS)
            }
        } else {
            while (nextCh in 0..32) {
                nextCharacter()
            }

            when (nextCh) {
                -1 -> setToken(TToken.TEOS)
                '&'.toInt() -> {
                    mark()
                    nextCharacter()
                    if (nextCh == '&'.toInt()) {
                        nextCharacter()
                        setToken(TToken.TAmpersandAmpersand, "&&")
                    } else {
                        setToken(TToken.TERROR)
                    }
                }
                '|'.toInt() -> {
                    mark()
                    nextCharacter()
                    if (nextCh == '|'.toInt()) {
                        nextCharacter()
                        setToken(TToken.TBarBar, "||")
                    } else {
                        setToken(TToken.TERROR)
                    }
                }
                ','.toInt() -> matchSymbol(TToken.TComma, ",")
                '?'.toInt() -> matchSymbol(TToken.TQuestion, "?")
                ':'.toInt() -> matchSymbol(TToken.TColon, ":")
                '!'.toInt() -> matchSymbolEqual(TToken.TBang, "!", TToken.TBangEqual, "!=")
                '='.toInt() -> matchSymbolEqual(TToken.TEqual, "=", TToken.TEqualEqual, "==")
                '>'.toInt() -> matchSymbolEqual(TToken.TGreaterThan, ">", TToken.TGreaterEqual, ">=")
                '{'.toInt() -> matchSymbol(TToken.TLCurly, "{")
                '<'.toInt() -> matchSymbolEqual(TToken.TLessThan, "<", TToken.TLessEqual, "<=")
                '('.toInt() -> matchSymbol(TToken.TLParen, "(")
                '-'.toInt() -> matchSymbol(TToken.TMinus, "-")
                '+'.toInt() -> matchSymbol(TToken.TPlus, "+")
                '}'.toInt() -> matchSymbol(TToken.TRCurly, "}")
                ')'.toInt() -> matchSymbol(TToken.TRParen, ")")
                ';'.toInt() -> matchSymbol(TToken.TSemicolon, ";")
                '/'.toInt() -> {
                    mark()
                    nextCharacter()

                    if (nextCh == '/'.toInt()) {
                        nextCharacter()
                        while (!isEndOfLine() && !isEndOfStream()) {
                            nextCharacter()
                        }
                        this.next()
                    } else if (nextCh == '*'.toInt()) {
                        nextCharacter()
                        var nesting = 0

                        while (true) {
                            if (nextCh == '*'.toInt()) {
                                nextCharacter()
                                if (nextCh == '/'.toInt()) {
                                    nextCharacter()
                                    if (nesting == 0) {
                                        this.next()
                                        break
                                    } else {
                                        nesting -= 1
                                    }
                                }
                            } else if (nextCh == '/'.toInt()) {
                                nextCharacter()
                                if (nextCh == '*'.toInt()) {
                                    nextCharacter()
                                    nesting += 1
                                }
                            } else if (isEndOfStream()) {
                                setToken(TToken.TERROR)
                                break
                            } else {
                                nextCharacter()
                            }
                        }
                    } else {
                        setToken(TToken.TSlash, "/")
                    }
                }
                '*'.toInt() -> matchSymbol(TToken.TStar, "*")
                '.'.toInt() -> {
                    mark()
                    nextCharacter()
                    if (isDigit()) {
                        nextCharacter()
                        while (isDigit()) {
                            nextCharacter()
                        }
                        matchOptionalExponent()
                    } else {
                        setToken(TToken.TERROR)
                    }
                }
                '"'.toInt() -> {
                    mark()
                    nextCharacter()
                    while (true) {
                        if (nextCh == -1) {
                            setToken(TToken.TERROR)
                            break
                        } else if (nextCh == '\\'.toInt()) {
                            nextCharacter()
                            if (nextCh == '\\'.toInt() || nextCh == '"'.toInt()) {
                                nextCharacter()
                            } else {
                                setToken(TToken.TERROR)
                                break
                            }
                        } else if (nextCh == '\"'.toInt()) {
                            nextCharacter()
                            setToken(TToken.TLiteralString)
                            break
                        } else {
                            nextCharacter()
                        }
                    }
                }
                else ->
                    if (isAlpha()) {
                        mark()
                        nextCharacter()
                        while (isAlpha() || isDigit()) {
                            nextCharacter()
                        }

                        val text = lexeme.toString()
                        val keywordCode = keywords[text]

                        if (keywordCode == null) {
                            setToken(TToken.TIdentifier, text)
                        } else {
                            setToken(keywordCode, text)
                        }
                    } else if (isDigit()) {
                        mark()
                        nextCharacter()
                        while (isDigit()) {
                            nextCharacter()
                        }
                        if (nextCh == '.'.toInt()) {
                            nextCharacter()
                            if (isDigit()) {
                                while (isDigit()) {
                                    nextCharacter()
                                }
                                matchOptionalExponent()
                            } else {
                                setToken(TToken.TERROR)
                            }
                        } else if (isExponent()) {
                            matchExponent()
                        } else {
                            setToken(TToken.TLiteralInt)
                        }
                    } else {
                        mark()
                        nextCharacter()
                        setToken(TToken.TERROR)
                    }
            }
        }
    }

    private fun nextCharacter() {
        if (nextCh != -1) {
            offset += 1
            if (nextCh == 10) {
                column = 0
                line += 1
            } else {
                column += 1
            }

            lexeme?.append(nextCh.toChar())
            nextCh = reader.read()
        }
    }

    private fun mark() {
        lexeme = StringBuilder()
        startOffset = offset + 1
        startLine = line
        startColumn = column + 1
    }

    private fun setToken(tToken: TToken, text: String? = null) {
        if (lexeme == null) {
            currentToken = Token(tToken, LocationCoordinate(offset + 1, line, column + 1), "")
        } else {
            currentToken = Token(tToken, LocationRange(LocationCoordinate(startOffset!!, startLine!!, startColumn!!), LocationCoordinate(offset, line, column)), text
                    ?: lexeme.toString())
            lexeme = null
        }
    }

    private fun isDigit() = nextCh >= '0'.toInt() && nextCh <= '9'.toInt()
    private fun isAlpha() = nextCh >= 'a'.toInt() && nextCh <= 'z'.toInt() || nextCh >= 'A'.toInt() && nextCh <= 'Z'.toInt()
    private fun isExponent() = nextCh == 'e'.toInt() || nextCh == 'E'.toInt()
    private fun isEndOfLine() = nextCh == 10 || nextCh == 13
    private fun isEndOfStream() = nextCh == -1

    private fun matchSymbol(tToken: TToken, text: String) {
        mark()
        nextCharacter()
        setToken(tToken, text)
    }

    private fun matchSymbolEqual(shortToken: TToken, shortText: String, longToken: TToken, longText: String) {
        mark()
        nextCharacter()
        if (nextCh == '='.toInt()) {
            nextCharacter()
            setToken(longToken, longText)
        } else {
            setToken(shortToken, shortText)
        }
    }

    private fun matchOptionalExponent() {
        if (isExponent()) {
            matchExponent()
        } else {
            setToken(TToken.TLiteralFloat)
        }
    }

    private fun matchExponent() {
        if (isExponent()) {
            nextCharacter()
            if (nextCh == '+'.toInt() || nextCh == '-'.toInt()) {
                nextCharacter()
            }
            if (isDigit()) {
                nextCharacter()
                while (isDigit()) {
                    nextCharacter()
                }
                setToken(TToken.TLiteralFloat)
            } else {
                setToken(TToken.TERROR)
            }
        } else {
            setToken(TToken.TERROR)
        }
    }
}

enum class TToken {
    TEOS, TERROR,

    TConst, TElse, TFalse, TFun, TIf, TLet, TReturn, TTrue, TWhile,

    TBool, TFloat, TInt,

    TAmpersandAmpersand, TBang, TBangEqual, TBarBar, TColon, TComma, TEqual, TEqualEqual, TGreaterEqual, TGreaterThan, TLCurly, TLessEqual, TLessThan,
    TLParen, TMinus, TPlus, TQuestion, TRCurly, TRParen, TSemicolon, TSlash, TStar,

    TIdentifier, TLiteralInt, TLiteralFloat, TLiteralString
}

data class Token(val tToken: TToken, val location: Location, val lexeme: String) {
    override fun toString(): String {
        fun pp(location: Location): String =
                when (location) {
                    is LocationCoordinate -> "${location.offset}:${location.line}:${location.column}"
                    is LocationRange -> pp(location.start) + "-" + pp(location.end)
                }

        return tToken.toString().drop(1) + " " + pp(location) + " [" + lexeme + "]"
    }
}

fun assembleTokens(la: LA): List<Token> {
    val result = mutableListOf<Token>()

    result += la.current
    while (la.current.tToken != TToken.TEOS) {
        la.next()
        result += la.current
    }

    return result
}
