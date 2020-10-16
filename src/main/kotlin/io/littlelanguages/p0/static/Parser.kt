package io.littlelanguages.p0.static

import io.littlelanguages.data.*
import io.littlelanguages.p0.Errors
import io.littlelanguages.p0.ParseError
import io.littlelanguages.p0.lexer.Scanner
import io.littlelanguages.p0.lexer.TToken
import io.littlelanguages.p0.lexer.Token

fun <T_Program, T_VariableDeclaration, T_LiteralExpression, T_FunctionDeclaration,
        T_FunctionDeclarationSuffix, T_Type, T_TypedIdentifier, T_Statement,
        T_Expression, T_OrExpression, T_AndExpression, T_RelationalExpression,
        T_RelationalOp, T_AdditiveExpression, T_AdditiveOp, T_MultiplicativeExpression,
        T_MultiplicativeOp, T_Factor, T_UnaryOp> parse(la: Scanner, visitor: Visitor<T_Program, T_VariableDeclaration, T_LiteralExpression, T_FunctionDeclaration,
        T_FunctionDeclarationSuffix, T_Type, T_TypedIdentifier, T_Statement,
        T_Expression, T_OrExpression, T_AndExpression, T_RelationalExpression,
        T_RelationalOp, T_AdditiveExpression, T_AdditiveOp, T_MultiplicativeExpression,
        T_MultiplicativeOp, T_Factor, T_UnaryOp>): Either<Errors, T_Program> =
        try {
            Right(Parser(la, visitor).program())
        } catch (e: ParsingException) {
            Left(ParseError(e.found, e.expected))
        }


class Parser<
        T_Program, T_VariableDeclaration, T_LiteralExpression, T_FunctionDeclaration,
        T_FunctionDeclarationSuffix, T_Type, T_TypedIdentifier, T_Statement,
        T_Expression, T_OrExpression, T_AndExpression, T_RelationalExpression,
        T_RelationalOp, T_AdditiveExpression, T_AdditiveOp, T_MultiplicativeExpression,
        T_MultiplicativeOp, T_Factor, T_UnaryOp>(
        private val la: Scanner,
        private val visitor: Visitor<
                T_Program, T_VariableDeclaration, T_LiteralExpression, T_FunctionDeclaration,
                T_FunctionDeclarationSuffix, T_Type, T_TypedIdentifier, T_Statement,
                T_Expression, T_OrExpression, T_AndExpression, T_RelationalExpression,
                T_RelationalOp, T_AdditiveExpression, T_AdditiveOp, T_MultiplicativeExpression,
                T_MultiplicativeOp, T_Factor, T_UnaryOp>) {
    fun program(): T_Program {
        val a =
                declarations()

        if (peek().tToken != TToken.TEOS) {
            throw ParsingException(peek(), firstDeclaration)
        }

        return visitor.visitProgram(a)
    }

    private fun declarations(): List<Union2<T_VariableDeclaration, T_FunctionDeclaration>> {
        val result =
                mutableListOf<Union2<T_VariableDeclaration, T_FunctionDeclaration>>()

        while (firstDeclaration.contains(peek().tToken))
            result.add(declaration())

        return result
    }

    private fun declaration(): Union2<T_VariableDeclaration, T_FunctionDeclaration> =
            when {
                firstVariableDeclaration.contains(peek().tToken) -> Union2a(variableDeclaration())
                firstFunctionDeclaration.contains(peek().tToken) -> Union2b(functionDeclaration())
                else -> throw ParsingException(peek(), firstDeclaration)
            }

    private fun variableDeclaration(): T_VariableDeclaration {
        if (!firstVariableDeclaration.contains(peek().tToken)) {
            throw ParsingException(peek(), firstVariableDeclaration)
        }

        val a1 =
                if (peek().tToken == TToken.TConst)
                    Union2a<Token, Token>(nextToken())
                else
                    Union2b<Token, Token>(nextToken())

        val a2 =
                matchToken(TToken.TIdentifier)

        val a3 =
                matchToken(TToken.TEqual)

        val a4 =
                literalExpression()

        val a5 =
                matchToken(TToken.TSemicolon)

        return visitor.visitVariableDeclaration(a1, a2, a3, a4, a5)
    }

    private fun literalExpression(): T_LiteralExpression =
            when {
                peek().tToken == TToken.TTrue -> {
                    val a =
                            nextToken()

                    visitor.visitLiteralExpression1(a)
                }
                peek().tToken == TToken.TFalse -> {
                    val a =
                            nextToken()

                    visitor.visitLiteralExpression2(a)
                }
                firstLiteralExpressionSign.contains(peek().tToken) -> {
                    val a1 =
                            literalExpressionSign()

                    val a2 =
                            literalExpressionValue()

                    visitor.visitLiteralExpression3(a1, a2)
                }
                else -> throw ParsingException(peek(), firstLiteralExpression)
            }

    private fun literalExpressionSign(): Union2<Token, Token>? =
            when (peek().tToken) {
                TToken.TPlus -> {
                    val a =
                            nextToken()

                    Union2a(a)
                }
                TToken.TMinus -> {
                    val a =
                            nextToken()

                    Union2b(a)
                }
                else -> null
            }

    private fun literalExpressionValue(): Union2<Token, Token> =
            when (peek().tToken) {
                TToken.TLiteralInt -> {
                    val a =
                            nextToken()

                    Union2a(a)
                }
                TToken.TLiteralFloat -> {
                    val a =
                            nextToken()

                    Union2b(a)
                }
                else -> throw ParsingException(peek(), firstLiteralExpressionValue)
            }

    private fun functionDeclaration(): T_FunctionDeclaration {
        val a1 =
                matchToken(TToken.TFun)

        val a2 =
                matchToken(TToken.TIdentifier)

        val a3 =
                matchToken(TToken.TLParen)

        val a4: Tuple2<T_TypedIdentifier, List<Tuple2<Token, T_TypedIdentifier>>>? =
                if (firstTypedIdentifier.contains(peek().tToken)) {
                    val a41 =
                            typedIdentifier()

                    val a42 =
                            mutableListOf<Tuple2<Token, T_TypedIdentifier>>()

                    while (peek().tToken == TToken.TComma) {
                        val a421 =
                                nextToken()
                        val a422 =
                                typedIdentifier()

                        a42.add(Tuple2(a421, a422))
                    }

                    Tuple2(a41, a42)

                } else null

        val a5 =
                matchToken(TToken.TRParen)

        val a6 =
                functionDeclarationSuffix()

        return visitor.visitFunctionDeclaration(a1, a2, a3, a4, a5, a6)
    }

    private fun parameters(): List<T_TypedIdentifier> =
            if (firstTypedIdentifier.contains(peek().tToken)) {
                val result =
                        mutableListOf(typedIdentifier())

                while (peek().tToken == TToken.TComma) {
                    skipToken()
                    result.add(typedIdentifier())
                }

                result
            } else
                emptyList()

    private fun functionDeclarationSuffix(): T_FunctionDeclarationSuffix =
            when (peek().tToken) {
                TToken.TColon -> {
                    val a1 =
                            nextToken()

                    val a2 =
                            type()

                    val a3 =
                            matchToken(TToken.TLCurly)

                    val a4 =
                            statements()

                    val a5 =
                            matchToken(TToken.TReturn)

                    val a6 =
                            expression()

                    val a7 =
                            matchToken(TToken.TSemicolon)

                    val a8 =
                            matchToken(TToken.TRCurly)

                    visitor.visitFunctionDeclarationSuffix1(a1, a2, a3, a4, a5, a6, a7, a8)
                }
                TToken.TLCurly -> {
                    val a1 = matchToken(TToken.TLCurly)

                    val a2 =
                            statements()

                    val a3 =
                            matchToken(TToken.TRCurly)

                    visitor.visitFunctionDeclarationSuffix2(a1, a2, a3)
                }
                else -> throw ParsingException(peek(), setOf(TToken.TColon, TToken.TLCurly))
            }

    private fun typedIdentifier(): T_TypedIdentifier {
        val a1 =
                matchToken(TToken.TIdentifier)

        val a2 =
                matchToken(TToken.TColon)

        val a3 =
                type()

        return visitor.visitTypedIdentifier(a1, a2, a3)
    }

    private fun type(): T_Type =
            when (peek().tToken) {
                TToken.TInt -> {
                    val a =
                            nextToken()
                    visitor.visitType1(a)
                }
                TToken.TFloat -> {
                    val a =
                            nextToken()
                    visitor.visitType2(a)
                }
                TToken.TBool -> {
                    val a =
                            nextToken()
                    visitor.visitType3(a)
                }
                else -> {
                    throw ParsingException(peek(), firstType)
                }
            }

    private fun statements(): List<T_Statement> =
            if (firstStatement.contains(peek().tToken)) {
                val result =
                        mutableListOf(statement())

                while (firstStatement.contains(peek().tToken)) {
                    result.add(statement())
                }

                result
            } else
                emptyList()

    private fun statement(): T_Statement =
            when (peek().tToken) {
                TToken.TIdentifier -> {
                    val a1 =
                            nextToken()

                    val a2 =
                            if (peek().tToken == TToken.TEqual) {
                                val a21 =
                                        nextToken()

                                val a22 =
                                        expression()

                                Union2b<Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>, Tuple2<Token, T_Expression>>(Tuple2(a21, a22))
                            } else {
                                val a21 =
                                        matchToken(TToken.TLParen)

                                val a22: Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>? =
                                        if (firstExpression.contains(peek().tToken)) {
                                            val a221 =
                                                    expression()

                                            val a222 =
                                                    mutableListOf<Tuple2<Token, T_Expression>>()

                                            while (peek().tToken == TToken.TComma) {
                                                val a2221 =
                                                        nextToken()

                                                val a2222 =
                                                        expression()

                                                a222.add(Tuple2(a2221, a2222))
                                            }

                                            Tuple2(a221, a222)
                                        } else
                                            null

                                val a23 =
                                        matchToken(TToken.TRParen)

                                Union2a<Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>, Tuple2<Token, T_Expression>>(Tuple3(a21, a22, a23))
                            }

                    val a3 =
                            matchToken(TToken.TSemicolon)

                    visitor.visitStatement5(a1, a2, a3)
                }
                TToken.TIf -> {
                    val a1 =
                            nextToken()

                    val a2 =
                            expression()

                    val a3 =
                            statement()

                    var a4: Tuple2<Token, T_Statement>? =
                            null

                    if (peek().tToken == TToken.TElse) {
                        val a41 =
                                nextToken()
                        val a42 =
                                statement()

                        a4 = Tuple2(a41, a42)
                    }

                    visitor.visitStatement2(a1, a2, a3, a4)
                }
                TToken.TWhile -> {
                    val a1 =
                            nextToken()

                    val a2 =
                            expression()

                    val a3 =
                            statement()

                    visitor.visitStatement3(a1, a2, a3)
                }
                TToken.TLCurly -> {
                    val a1 =
                            nextToken()

                    val a2 =
                            statements()

                    val a3 =
                            matchToken(TToken.TRCurly)

                    visitor.visitStatement4(a1, a2, a3)
                }
                TToken.TSemicolon -> {
                    val a =
                            nextToken()

                    visitor.visitStatement6(a)
                }
                else ->
                    if (firstVariableDeclaration.contains(peek().tToken)) {
                        val a1 =
                                if (peek().tToken == TToken.TConst)
                                    Union2a<Token, Token>(nextToken())
                                else
                                    Union2b<Token, Token>(nextToken())

                        val a2 =
                                matchToken(TToken.TIdentifier)

                        val a3 =
                                matchToken(TToken.TEqual)

                        val a4 =
                                expression()

                        val a5 =
                                matchToken(TToken.TSemicolon)

                        visitor.visitStatement1(a1, a2, a3, a4, a5)
                    } else
                        throw ParsingException(peek(), firstStatement)
            }

    private fun expression(): T_Expression {
        val a1 =
                orExpression()

        val a2 =
                optionalTernaryExpressionSuffix()

        return visitor.visitExpression(a1, a2)
    }

    private fun optionalTernaryExpressionSuffix(): Tuple4<Token, T_Expression, Token, T_Expression>? =
            if (peek().tToken == TToken.TQuestion) {
                val a1 =
                        nextToken()

                val a2 =
                        expression()

                val a3 =
                        matchToken(TToken.TColon)

                val a4 =
                        expression()

                Tuple4(a1, a2, a3, a4)
            } else
                null

    private fun orExpression(): T_OrExpression {
        val a1 =
                andExpression()

        val a2 =
                mutableListOf<Tuple2<Token, T_AndExpression>>()

        while (peek().tToken == TToken.TBarBar) {
            val a21 =
                    nextToken()

            val a22 =
                    andExpression()

            a2.add(Tuple2(a21, a22))
        }

        return visitor.visitOrExpression(a1, a2)
    }

    private fun andExpression(): T_AndExpression {
        val a1 =
                relationalExpression()

        val a2 =
                mutableListOf<Tuple2<Token, T_RelationalExpression>>()

        while (peek().tToken == TToken.TAmpersandAmpersand) {
            val a21 =
                    nextToken()

            val a22 =
                    relationalExpression()

            a2.add(Tuple2(a21, a22))
        }

        return visitor.visitAndExpression(a1, a2)
    }

    private fun relationalExpression(): T_RelationalExpression {
        val a1 =
                additiveExpression()

        val a2 = if (firstRelationOp.contains(peek().tToken)) {
            val relationalOp =
                    relationalOp()

            val next =
                    additiveExpression()

            Tuple2(relationalOp, next)
        } else
            null

        return visitor.visitRelationalExpression(a1, a2)
    }

    private fun relationalOp(): T_RelationalOp =
            when (peek().tToken) {
                TToken.TEqualEqual -> {
                    val a =
                            nextToken()

                    visitor.visitRelationalOp1(a)
                }
                TToken.TBangEqual -> {
                    val a =
                            nextToken()

                    visitor.visitRelationalOp2(a)
                }
                TToken.TLessEqual -> {
                    val a =
                            nextToken()

                    visitor.visitRelationalOp3(a)
                }
                TToken.TLessThan -> {
                    val a =
                            nextToken()

                    visitor.visitRelationalOp4(a)
                }
                TToken.TGreaterEqual -> {
                    val a =
                            nextToken()

                    visitor.visitRelationalOp5(a)
                }
                TToken.TGreaterThan -> {
                    val a =
                            nextToken()

                    visitor.visitRelationalOp6(a)
                }
                else -> throw ParsingException(peek(), firstRelationOp)
            }

    private fun additiveExpression(): T_AdditiveExpression {
        val a1 =
                multiplicativeExpression()

        val a2 =
                mutableListOf<Tuple2<T_AdditiveOp, T_MultiplicativeExpression>>()

        while (firstAdditiveOp.contains(peek().tToken)) {
            val additiveOp =
                    additiveOp()

            val next =
                    multiplicativeExpression()

            a2.add(Tuple2(additiveOp, next))
        }

        return visitor.visitAdditiveExpression(a1, a2)
    }

    private fun additiveOp(): T_AdditiveOp =
            when (peek().tToken) {
                TToken.TPlus -> {
                    val a =
                            nextToken()

                    visitor.visitAdditiveOp1(a)
                }
                TToken.TMinus -> {
                    val a =
                            nextToken()

                    visitor.visitAdditiveOp2(a)
                }
                else -> throw ParsingException(peek(), firstAdditiveOp)
            }

    private fun multiplicativeExpression(): T_MultiplicativeExpression {
        val a1 =
                factor()

        val a2 =
                mutableListOf<Tuple2<T_MultiplicativeOp, T_Factor>>()

        while (firstMultiplicativeOp.contains(peek().tToken)) {
            val multiplicativeOp =
                    multiplicativeOp()

            val next =
                    factor()

            a2.add(Tuple2(multiplicativeOp, next))

        }
        return visitor.visitMultiplicativeExpression(a1, a2)
    }

    private fun multiplicativeOp(): T_MultiplicativeOp =
            when (peek().tToken) {
                TToken.TStar -> {
                    val a =
                            nextToken()

                    visitor.visitMultiplicativeOp1(a)
                }
                TToken.TSlash -> {
                    val a =
                            nextToken()

                    visitor.visitMultiplicativeOp2(a)
                }
                else -> throw ParsingException(peek(), firstAdditiveOp)
            }

    private fun factor(): T_Factor =
            when (peek().tToken) {
                TToken.TLiteralInt -> {
                    val a =
                            nextToken()

                    visitor.visitFactor1(a)
                }
                TToken.TLiteralFloat -> {
                    val a =
                            nextToken()

                    visitor.visitFactor2(a)
                }
                TToken.TLiteralString -> {
                    val a =
                            nextToken()

                    visitor.visitFactor3(a)
                }
                TToken.TTrue -> {
                    val a =
                            nextToken()

                    visitor.visitFactor4(a)
                }
                TToken.TFalse -> {
                    val a =
                            nextToken()

                    visitor.visitFactor5(a)
                }
                TToken.TLParen -> {
                    val a1 =
                            nextToken()

                    val a2 =
                            expression()

                    val a3 =
                            matchToken(TToken.TRParen)

                    visitor.visitFactor7(a1, a2, a3)
                }
                TToken.TIdentifier -> {
                    val a1 =
                            nextToken()

                    var a2: Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>? =
                            null

                    if (peek().tToken == TToken.TLParen) {
                        val a21 =
                                nextToken()

                        var a22: Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>? =
                                null

                        if (firstExpression.contains((peek().tToken))) {
                            val a221 =
                                    expression()

                            val a222: MutableList<Tuple2<Token, T_Expression>> =
                                    mutableListOf()

                            while (peek().tToken == TToken.TComma) {
                                val a2221 =
                                        nextToken()

                                val a2222 =
                                        expression()

                                a222.add(Tuple2(a2221, a2222))
                            }
                            a22 = Tuple2(a221, a222)
                        }
                        val a23 =
                                matchToken(TToken.TRParen)

                        a2 = Tuple3(a21, a22, a23)
                    }

                    visitor.visitFactor8(a1, a2)
                }
                else ->
                    if (firstUnaryOperator.contains(peek().tToken)) {
                        val unaryOperator =
                                unaryOperator()

                        val factor =
                                factor()

                        visitor.visitFactor6(unaryOperator, factor)
                    } else
                        throw ParsingException(peek(), firstFactor)
            }

    private fun unaryOperator(): T_UnaryOp =
            when (peek().tToken) {
                TToken.TBang -> {
                    val a =
                            nextToken()

                    visitor.visitUnaryOp1(a)
                }
                TToken.TMinus -> {
                    val a =
                            nextToken()

                    visitor.visitUnaryOp2(a)
                }
                TToken.TPlus -> {
                    val a =
                            nextToken()

                    visitor.visitUnaryOp3(a)
                }
                else ->
                    throw ParsingException(peek(), firstUnaryOperator)
            }

    private fun matchToken(tToken: TToken): Token =
            when (peek().tToken) {
                tToken -> nextToken()
                else -> throw ParsingException(peek(), setOf(tToken))
            }

    private fun nextToken(): Token {
        val result =
                peek()

        skipToken()

        return result
    }

    private fun skipToken() {
        la.next()
    }

    private fun peek(): Token =
            la.current()
}


private val firstVariableDeclarationAccess = setOf(TToken.TConst, TToken.TLet)
private val firstFunctionDeclaration = setOf(TToken.TFun)
private val firstVariableDeclaration = firstVariableDeclarationAccess
private val firstDeclaration = firstVariableDeclaration.union(firstFunctionDeclaration)
private val firstLiteralExpressionValue = setOf(TToken.TLiteralInt, TToken.TLiteralFloat)
private val firstLiteralExpressionSign = setOf(TToken.TPlus, TToken.TMinus).union(firstLiteralExpressionValue)
private val firstLiteralExpression = setOf(TToken.TTrue, TToken.TFalse).union(firstLiteralExpressionSign)
private val firstType = setOf(TToken.TInt, TToken.TFloat, TToken.TBool)
private val firstUnaryOperator = setOf(TToken.TBang, TToken.TMinus, TToken.TPlus)
private val firstFactor = setOf(TToken.TLiteralInt, TToken.TLiteralFloat, TToken.TLiteralString, TToken.TTrue, TToken.TFalse,
        TToken.TLParen, TToken.TIdentifier).union(firstUnaryOperator)
private val firstRelationOp = setOf(TToken.TEqualEqual, TToken.TBangEqual, TToken.TLessEqual, TToken.TLessThan, TToken.TGreaterEqual,
        TToken.TGreaterThan)
private val firstExpression = firstFactor
private val firstAdditiveOp = setOf(TToken.TPlus, TToken.TMinus)
private val firstMultiplicativeOp = setOf(TToken.TStar, TToken.TSlash)
private val firstTypedIdentifier = setOf(TToken.TIdentifier)
private val firstStatement = setOf(TToken.TIdentifier, TToken.TIf, TToken.TWhile, TToken.TLCurly, TToken.TSemicolon).union(firstVariableDeclaration)


class ParsingException(
        val found: Token,
        val expected: Set<TToken>) : Exception()