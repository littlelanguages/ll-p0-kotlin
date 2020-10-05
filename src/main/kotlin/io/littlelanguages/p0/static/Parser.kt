package io.littlelanguages.p0.static

import io.littlelanguages.data.Either
import io.littlelanguages.data.Left
import io.littlelanguages.data.Right
import io.littlelanguages.p0.Errors
import io.littlelanguages.p0.ParseError
import io.littlelanguages.p0.lexer.LA
import io.littlelanguages.p0.lexer.Location
import io.littlelanguages.p0.lexer.TToken
import io.littlelanguages.p0.lexer.Token
import io.littlelanguages.p0.static.ast.*

fun parse(la: LA): Either<Errors, Program> =
        Parser(la).program()


class Parser(private val la: LA) {
    fun program(): Either<Errors, Program> =
            try {
                val declarations =
                        declarations()

                if (peek().tToken != TToken.TEOS) {
                    throw ParsingException(peek(), firstDeclaration)
                }

                Right(Program(declarations))
            } catch (e: ParsingException) {
                Left(ParseError(e.found, e.expected))
            }

    private fun declarations(): List<Declaration> {
        val result =
                mutableListOf<Declaration>()

        while (firstDeclaration.contains(peek().tToken))
            result.add(declaration())

        return result
    }

    private fun declaration(): Declaration =
            when {
                firstVariableDeclaration.contains(peek().tToken) -> variableDeclaration()
                firstFunctionDeclaration.contains(peek().tToken) -> functionDeclaration()
                else -> throw ParsingException(peek(), firstDeclaration)
            }

    private fun variableDeclaration(): Declaration {
        if (!firstVariableDeclaration.contains(peek().tToken)) {
            throw ParsingException(peek(), firstVariableDeclaration)
        }

        val variableAccess =
                variableDeclarationAccess()

        val identifier =
                matchToken(TToken.TIdentifier)

        matchToken(TToken.TEqual)

        val expression =
                literalExpression()

        matchToken(TToken.TSemicolon)

        return VariableDeclaration(variableAccess, Identifier(identifier.location, identifier.lexeme), expression)
    }

    private fun variableDeclarationAccess(): VariableAccess {
        if (!firstVariableDeclarationAccess.contains(peek().tToken))
            throw ParsingException(peek(), firstVariableDeclarationAccess)

        val variableAccess =
                if (peek().tToken == TToken.TConst) VariableAccess.ReadOnly else VariableAccess.ReadWrite

        skipToken()

        return variableAccess
    }

    private fun literalExpression(): LiteralExpression =
            when {
                peek().tToken == TToken.TTrue -> {
                    val position =
                            nextToken().location

                    LiteralExpressionValue(LiteralBool(position, true))
                }
                peek().tToken == TToken.TFalse -> {
                    val position =
                            nextToken().location

                    LiteralExpressionValue(LiteralBool(position, false))
                }
                firstLiteralExpressionSign.contains(peek().tToken) -> {
                    val literalExpressionSign =
                            literalExpressionSign()

                    val literalExpressionValue =
                            literalExpressionValue()

                    if (literalExpressionSign == null)
                        LiteralExpressionValue(literalExpressionValue)
                    else
                        LiteralExpressionUnaryValue(literalExpressionSign.first, literalExpressionSign.second, literalExpressionValue)
                }
                else -> throw ParsingException(peek(), firstLiteralExpression)
            }

    private fun literalExpressionSign(): Pair<Location, UnaryOp>? =
            when (peek().tToken) {
                TToken.TPlus -> {
                    val position =
                            nextToken().location

                    Pair(position, UnaryOp.UnaryPlus)
                }
                TToken.TMinus -> {
                    val position =
                            nextToken().location

                    Pair(position, UnaryOp.UnaryMinus)
                }
                else -> null
            }

    private fun literalExpressionValue(): LiteralValue =
            when (peek().tToken) {
                TToken.TLiteralInt -> {
                    val symbol =
                            nextToken()

                    LiteralInt(symbol.location, symbol.lexeme)
                }
                TToken.TLiteralFloat -> {
                    val symbol =
                            nextToken()

                    LiteralFloat(symbol.location, symbol.lexeme)
                }
                else -> throw ParsingException(peek(), firstLiteralExpressionValue)
            }

    private fun functionDeclaration(): Declaration {
        matchToken(TToken.TFun)

        val identifier =
                matchToken(TToken.TIdentifier)

        matchToken(TToken.TLParen)

        val parameters =
                parameters()

        matchToken(TToken.TRParen)

        val functionDeclarationSuffix =
                functionDeclarationSuffix()

        return FunctionDeclaration(Identifier(identifier.location, identifier.lexeme), parameters, functionDeclarationSuffix.first, functionDeclarationSuffix.second)
    }

    private fun parameters(): List<Pair<Identifier, Type>> =
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

    private fun functionDeclarationSuffix(): Pair<List<Statement>, Pair<Type, Expression>?> =
            when (peek().tToken) {
                TToken.TColon -> {
                    skipToken()

                    val type =
                            type()

                    matchToken(TToken.TLCurly)

                    val statements =
                            statements()

                    matchToken(TToken.TReturn)

                    val expression =
                            expression()

                    matchToken(TToken.TSemicolon)

                    matchToken(TToken.TRCurly)

                    Pair(statements, Pair(type, expression))
                }
                TToken.TLCurly -> {
                    matchToken(TToken.TLCurly)

                    val statements =
                            statements()

                    matchToken(TToken.TRCurly)

                    Pair(statements, null)
                }
                else -> throw ParsingException(peek(), setOf(TToken.TColon, TToken.TLCurly))
            }

    private fun typedIdentifier(): Pair<Identifier, Type> {
        val identifier =
                matchToken(TToken.TIdentifier)

        matchToken(TToken.TColon)

        val type =
                type()

        return Pair(Identifier(identifier.location, identifier.lexeme), type)
    }

    private fun type(): Type =
            when (peek().tToken) {
                TToken.TInt -> {
                    skipToken()
                    Type.Int
                }
                TToken.TFloat -> {
                    skipToken()
                    Type.Float
                }
                TToken.TBool -> {
                    skipToken()
                    Type.Bool
                }
                else -> {
                    throw ParsingException(peek(), firstType)
                }
            }

    private fun statements(): List<Statement> =
            if (firstStatement.contains(peek().tToken)) {
                val result =
                        mutableListOf(statement())

                while (firstStatement.contains(peek().tToken)) {
                    result.add(statement())
                }

                result
            } else
                emptyList()

    private fun statement(): Statement =
            when (peek().tToken) {
                TToken.TIdentifier -> {
                    val identifier =
                            nextToken()

                    if (peek().tToken == TToken.TEqual) {
                        skipToken()

                        val expression =
                                expression()

                        matchToken(TToken.TSemicolon)

                        AssignmentStatement(Identifier(identifier.location, identifier.lexeme), expression)
                    } else {
                        matchToken(TToken.TLParen)
                        val arguments =
                                arguments()

                        matchToken(TToken.TRParen)
                        matchToken(TToken.TSemicolon)

                        CallStatement(Identifier(identifier.location, identifier.lexeme), arguments)
                    }
                }
                TToken.TIf -> {
                    skipToken()

                    val expression =
                            expression()

                    val statement1 =
                            statement()

                    if (peek().tToken == TToken.TElse) {
                        skipToken()
                        val statement2 =
                                statement()

                        IfThenElseStatement(expression, statement1, statement2)
                    } else
                        IfThenElseStatement(expression, statement1, null)
                }
                TToken.TWhile -> {
                    skipToken()

                    val expression =
                            expression()

                    val statement =
                            statement()

                    WhileStatement(expression, statement)
                }
                TToken.TLCurly -> {
                    skipToken()

                    val statements =
                            statements()

                    matchToken(TToken.TRCurly)

                    BlockStatement(statements)
                }
                TToken.TSemicolon -> {
                    skipToken()

                    EmptyStatement
                }
                else ->
                    if (firstVariableDeclaration.contains(peek().tToken)) {
                        val variableDeclarationAccess =
                                variableDeclarationAccess()

                        val identifier =
                                matchToken(TToken.TIdentifier)

                        matchToken(TToken.TEqual)

                        val expression =
                                expression()

                        matchToken(TToken.TSemicolon)

                        DeclarationStatement(variableDeclarationAccess, Identifier(identifier.location, identifier.lexeme), expression)
                    } else
                        throw ParsingException(peek(), firstStatement)
            }

    private fun arguments(): List<Expression> =
            if (firstExpression.contains(peek().tToken)) {
                val expressions =
                        mutableListOf(expression())

                while (peek().tToken == TToken.TComma) {
                    skipToken()
                    expressions.add(expression())
                }

                expressions
            } else
                emptyList()

    private fun expression(): Expression {
        val orExpression =
                orExpression()

        val optionalTernaryExpressionSuffix =
                optionalTernaryExpressionSuffix()

        return if (optionalTernaryExpressionSuffix == null)
            orExpression
        else
            TernaryExpression(orExpression, optionalTernaryExpressionSuffix.first, optionalTernaryExpressionSuffix.second)
    }

    private fun optionalTernaryExpressionSuffix(): Pair<Expression, Expression>? =
            if (peek().tToken == TToken.TQuestion) {
                skipToken()

                val thenExpression =
                        expression()

                matchToken(TToken.TColon)

                val elseExpression =
                        expression()

                Pair(thenExpression, elseExpression)
            } else
                null

    private fun orExpression(): Expression {
        var current =
                andExpression()

        while (peek().tToken == TToken.TBarBar) {
            skipToken()

            val next =
                    andExpression()

            current =
                    BinaryExpression(current, BinaryOp.Or, next)
        }

        return current
    }

    private fun andExpression(): Expression {
        var current =
                relationalExpression()

        while (peek().tToken == TToken.TAmpersandAmpersand) {
            skipToken()

            val next =
                    relationalExpression()

            current =
                    BinaryExpression(current, BinaryOp.And, next)
        }

        return current
    }

    private fun relationalExpression(): Expression {
        val current =
                additiveExpression()

        return if (firstRelationOp.contains(peek().tToken)) {
            val relationalOp =
                    relationalOp()

            val next =
                    additiveExpression()

            BinaryExpression(current, relationalOp, next)
        } else
            current
    }

    private fun relationalOp(): BinaryOp {
        val result =
                when (peek().tToken) {
                    TToken.TEqualEqual -> BinaryOp.Equal
                    TToken.TBangEqual -> BinaryOp.NotEqual
                    TToken.TLessEqual -> BinaryOp.LessEqual
                    TToken.TLessThan -> BinaryOp.LessThan
                    TToken.TGreaterEqual -> BinaryOp.GreaterEqual
                    TToken.TGreaterThan -> BinaryOp.GreaterThan
                    else -> throw ParsingException(peek(), firstRelationOp)
                }
        skipToken()

        return result
    }

    private fun additiveExpression(): Expression {
        var current =
                multiplicativeExpression()

        while (firstAdditiveOp.contains(peek().tToken)) {
            val additiveOp =
                    additiveOp()

            val next =
                    multiplicativeExpression()

            current =
                    BinaryExpression(current, additiveOp, next)
        }

        return current
    }

    private fun additiveOp(): BinaryOp {
        val result =
                when (peek().tToken) {
                    TToken.TPlus -> BinaryOp.Plus
                    TToken.TMinus -> BinaryOp.Minus
                    else -> throw ParsingException(peek(), firstAdditiveOp)
                }
        skipToken()

        return result
    }

    private fun multiplicativeExpression(): Expression {
        var current =
                factor()

        while (firstMultiplicativeOp.contains(peek().tToken)) {
            val multiplicativeOp =
                    multiplicativeOp()

            val next =
                    factor()

            current =
                    BinaryExpression(current, multiplicativeOp, next)
        }

        return current
    }

    private fun multiplicativeOp(): BinaryOp {
        val result =
                when (peek().tToken) {
                    TToken.TStar -> BinaryOp.Times
                    TToken.TSlash -> BinaryOp.Divide
                    else -> throw ParsingException(peek(), firstAdditiveOp)
                }
        skipToken()

        return result
    }

    private fun factor(): Expression =
            when (peek().tToken) {
                TToken.TLiteralInt -> {
                    val token =
                            nextToken()

                    LiteralValueExpression(LiteralInt(token.location, token.lexeme))
                }
                TToken.TLiteralFloat -> {
                    val token =
                            nextToken()

                    LiteralValueExpression(LiteralFloat(token.location, token.lexeme))
                }
                TToken.TLiteralString -> {
                    val token =
                            nextToken()

                    LiteralValueExpression(LiteralString(token.location, token.lexeme))
                }
                TToken.TTrue -> {
                    val token =
                            nextToken()

                    LiteralValueExpression(LiteralBool(token.location, true))
                }
                TToken.TFalse -> {
                    val token =
                            nextToken()

                    LiteralValueExpression(LiteralBool(token.location, false))
                }
                TToken.TLParen -> {
                    val leftToken =
                            nextToken()

                    val expression =
                            expression()

                    val rightToken =
                            matchToken(TToken.TRParen)

                    Parenthesis(leftToken.location + rightToken.location, expression)
                }
                TToken.TIdentifier -> {
                    val identifier =
                            nextToken()

                    val optionalParameters =
                            optionalParameters()

                    if (optionalParameters == null)
                        IdentifierReference(Identifier(identifier.location, identifier.lexeme))
                    else
                        CallExpression(Identifier(identifier.location, identifier.lexeme), optionalParameters.first)
                }
                else ->
                    if (firstUnaryOperator.contains(peek().tToken)) {
                        val position =
                                peek().location

                        val unaryOperator =
                                unaryOperator()

                        val factor =
                                factor()

                        UnaryExpression(position, unaryOperator, factor)
                    } else
                        throw ParsingException(peek(), firstFactor)
            }

    private fun unaryOperator(): UnaryOp {
        val result =
                when (peek().tToken) {
                    TToken.TBang ->
                        UnaryOp.UnaryNot
                    TToken.TMinus ->
                        UnaryOp.UnaryMinus
                    TToken.TPlus ->
                        UnaryOp.UnaryPlus
                    else ->
                        throw ParsingException(peek(), firstUnaryOperator)
                }

        skipToken()

        return result
    }

    private fun optionalParameters(): Pair<List<Expression>, Location>? =
            if (peek().tToken == TToken.TLParen) {
                val startToken =
                        nextToken()

                val parameters =
                        arguments()

                val endToken =
                        matchToken(TToken.TRParen)

                Pair(parameters, startToken.location + endToken.location)
            } else
                null

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
            la.current
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