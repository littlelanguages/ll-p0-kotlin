package io.littlelanguages.p0.static.ast

import io.littlelanguages.data.*
import io.littlelanguages.p0.static.Token
import io.littlelanguages.p0.static.Visitor
import io.littlelanguages.scanpiler.Location
import io.littlelanguages.scanpiler.Locationable


data class Program(
        val declarations: List<Declaration>) : Yamlable {
    override fun yaml(): Any =
            singletonMap("Program", declarations.map { it.yaml() })
}

sealed class Declaration(
        open val identifier: Identifier) : Yamlable

data class VariableDeclaration(
        val access: VariableAccess,
        override val identifier: Identifier,
        val expression: LiteralExpression) : Declaration(identifier) {
    override fun yaml(): Any =
            singletonMap("VariableDeclaration", mapOf(
                    Pair("access", access.yaml()),
                    Pair("identifier", identifier.yaml()),
                    Pair("e", expression.yaml())
            ))
}

data class FunctionDeclaration(
        override val identifier: Identifier,
        val arguments: List<Tuple2<Identifier, Type>>,
        val statements: List<Statement>,
        val suffix: Tuple2<Type, Expression>?) : Declaration(identifier) {
    override fun yaml(): Any {
        val suffix =
                suffix

        return if (suffix == null)
            singletonMap("FunctionDeclaration", mapOf(
                    Pair("identifier", identifier.yaml()),
                    Pair("arguments", arguments.map {
                        mapOf(
                                Pair("name", it.a.yaml()),
                                Pair("type", it.b.yaml()))
                    }),
                    Pair("s", statements.map { it.yaml() })
            ))
        else
            singletonMap("FunctionDeclaration", mapOf(
                    Pair("identifier", identifier.yaml()),
                    Pair("arguments", arguments.map {
                        mapOf(
                                Pair("name", it.a.yaml()),
                                Pair("type", it.b.yaml()))
                    }),
                    Pair("result", suffix.a.yaml()),
                    Pair("s", statements.map { it.yaml() }),
                    Pair("e", suffix.b.yaml())
            ))
    }
}


enum class VariableAccess : Yamlable {
    ReadOnly, ReadWrite;

    override fun yaml(): String =
            when (this) {
                ReadOnly -> "ReadOnly"
                ReadWrite -> "ReadWrite"
            }
}

enum class Type : Yamlable {
    Int, Float, Bool;

    override fun yaml(): Any =
            when (this) {
                Bool -> "Bool"
                Int -> "Int"
                Float -> "Float"
            }
}


sealed class Statement : Yamlable

data class AssignmentStatement(
        val identifier: Identifier,
        val expression: Expression) : Statement() {
    override fun yaml(): Any =
            singletonMap("AssignmentStatement", mapOf(
                    Pair("identifier", identifier.yaml()),
                    Pair("e", expression.yaml())
            ))
}

data class DeclarationStatement(
        val access: VariableAccess,
        val identifier: Identifier,
        val expression: Expression) : Statement() {
    override fun yaml(): Any =
            singletonMap("VariableDeclarationStatement", mapOf(
                    Pair("access", access.yaml()),
                    Pair("identifier", identifier.yaml()),
                    Pair("e", expression.yaml())
            ))
}

data class IfThenElseStatement(
        val expression: Expression,
        val statement1: Statement,
        val statement2: Statement?) : Statement() {
    override fun yaml(): Any {
        val s2 =
                statement2

        return if (s2 == null)
            singletonMap("IfThenElseStatement", mapOf(
                    Pair("e", expression.yaml()),
                    Pair("s1", statement1.yaml())
            ))
        else
            singletonMap("IfThenElseStatement", mapOf(
                    Pair("e", expression.yaml()),
                    Pair("s1", statement1.yaml()),
                    Pair("s2", s2.yaml())
            ))
    }
}

data class WhileStatement(
        val expression: Expression,
        val statement: Statement) : Statement() {
    override fun yaml(): Any =
            singletonMap("WhileStatement", mapOf(
                    Pair("e", expression.yaml()),
                    Pair("s", statement.yaml())
            ))
}

data class BlockStatement(
        val statements: List<Statement>) : Statement() {
    override fun yaml(): Any =
            singletonMap("BlockStatement", statements.map { it.yaml() })
}

data class CallStatement(
        val identifier: Identifier,
        val expressions: List<Expression>) : Statement() {
    override fun yaml(): Any =
            singletonMap("CallStatement", mapOf(
                    Pair("identifier", identifier.yaml()),
                    Pair("parameters", expressions.map { it.yaml() })
            ))
}

object EmptyStatement : Statement() {
    override fun yaml(): Any =
            singletonMap("EmptyStatement", emptyMap<String, Any>())
}


sealed class Expression : Locationable, Yamlable

data class TernaryExpression(
        val expression1: Expression,
        val expression2: Expression,
        val expression3: Expression) : Expression() {
    override fun position(): Location = expression1.position() + expression3.position()

    override fun yaml(): Any =
            singletonMap("TernaryExpression", mapOf(
                    Pair("e1", expression1.yaml()),
                    Pair("e2", expression2.yaml()),
                    Pair("e3", expression3.yaml())))
}

data class BinaryExpression(
        val expression1: Expression,
        val op: BinaryOp,
        val expression2: Expression) : Expression() {
    override fun position(): Location = expression1.position() + expression2.position()

    override fun yaml(): Any =
            singletonMap("BinaryExpression", mapOf(
                    Pair("e1", expression1.yaml()),
                    Pair("op", op.yaml()),
                    Pair("e2", expression2.yaml())))
}

data class UnaryExpression(
        val location: Location,
        val op: UnaryOp,
        val expression: Expression) : Expression() {
    override fun position(): Location = location + expression.position()

    override fun yaml(): Any =
            singletonMap("UnaryExpression", mapOf(
                    Pair("position", location.yaml()),
                    Pair("op", op.yaml()),
                    Pair("e", expression.yaml())))
}

data class CallExpression(
        val identifier: Identifier,
        val expressions: List<Expression>) : Expression() {
    override fun position(): Location =
            if (expressions.isEmpty())
                identifier.location
            else
                identifier.location + expressions.last().position()

    override fun yaml(): Any =
            singletonMap("CallExpression", mapOf(
                    Pair("name", identifier.yaml()),
                    Pair("parameters", expressions.map { it.yaml() })))
}

data class IdentifierReference(
        val identifier: Identifier) : Expression() {
    override fun position(): Location = identifier.location

    override fun yaml(): Any =
            mapOf(Pair("IdentifierReference", identifier.yaml()))
}

data class Parenthesis(
        val location: Location,
        val expression: Expression) : Expression() {
    override fun position(): Location = location

    override fun yaml(): Any =
            singletonMap("Parenthesis", mapOf(
                    Pair("e", expression.yaml()),
                    Pair("position", location.yaml())))
}

data class LiteralValueExpression(
        val value: LiteralValue) : Expression() {
    override fun position(): Location = value.position()

    override fun yaml(): Any =
            singletonMap("LiteralValue", mapOf(
                    Pair("value", value.yaml())))
}


sealed class LiteralExpression : Locationable, Yamlable

data class LiteralExpressionValue(
        val value: LiteralValue) : LiteralExpression() {
    override fun position(): Location = value.position()

    override fun yaml(): Any =
            singletonMap("LiteralExpressionValue", mapOf(
                    Pair("value", value.yaml())))
}

data class LiteralExpressionUnaryValue(
        val location: Location,
        val op: UnaryOp,
        val value: LiteralValue) : LiteralExpression() {
    override fun position(): Location = location + value.position()

    override fun yaml(): Any =
            singletonMap("LiteralExpressionUnaryValue", mapOf(
                    Pair("position", location.yaml()),
                    Pair("op", op.yaml()),
                    Pair("value", value.yaml())))
}

sealed class LiteralValue : Locationable, Yamlable

data class LiteralBool(
        val location: Location,
        val value: Boolean) : LiteralValue() {
    override fun position(): Location = location

    override fun yaml(): Any =
            singletonMap("LiteralBool", mapOf(
                    Pair("value", if (value) "True" else "False"),
                    Pair("position", location.yaml())))
}

data class LiteralInt(
        val location: Location,
        val value: String) : LiteralValue() {
    override fun position(): Location = location

    override fun yaml(): Any =
            singletonMap("LiteralInt", mapOf(
                    Pair("value", value),
                    Pair("position", location.yaml())))
}

data class LiteralFloat(
        val location: Location,
        val value: String) : LiteralValue() {
    override fun position(): Location = location

    override fun yaml(): Any =
            singletonMap("LiteralFloat", mapOf(
                    Pair("value", value),
                    Pair("position", location.yaml())))
}

data class LiteralString(
        val location: Location,
        val value: String) : LiteralValue() {
    override fun position(): Location = location

    override fun yaml(): Any =
            singletonMap("LiteralString", mapOf(
                    Pair("value", value),
                    Pair("position", location.yaml())))
}


enum class BinaryOp : Yamlable {
    Divide, Minus, Plus, Times, Equal, GreaterEqual, GreaterThan, LessEqual, LessThan, NotEqual, And, Or;

    override fun yaml(): Any =
            when (this) {
                Divide -> "Divide"
                Minus -> "Minus"
                Plus -> "Plus"
                Times -> "Times"
                Equal -> "Equal"
                GreaterEqual -> "GreaterEqual"
                GreaterThan -> "GreaterThan"
                LessEqual -> "LessEqual"
                LessThan -> "LessThan"
                NotEqual -> "NotEqual"
                And -> "And"
                Or -> "Or"
            }
}

enum class UnaryOp : Yamlable {
    UnaryNot, UnaryMinus, UnaryPlus;

    override fun yaml(): Any =
            when (this) {
                UnaryNot -> "UnaryNot"
                UnaryMinus -> "UnaryMinus"
                UnaryPlus -> "UnaryPlus"
            }
}


data class Identifier(
        val location: Location,
        val name: String) : Yamlable {
    override fun yaml(): Any =
            mapOf(
                    Pair("value", name),
                    Pair("position", location.yaml())
            )
}

typealias T_FunctionDeclarationSuffix = Tuple2<List<Statement>, Tuple2<Type, Expression>?>
typealias T_Type = Type
typealias T_TypedIdentifier = Tuple2<Identifier, Type>
typealias T_Statement = Statement
typealias T_Expression = Expression
typealias T_OrExpression = Expression
typealias T_AndExpression = Expression
typealias T_RelationalExpression = Expression
typealias T_RelationalOp = BinaryOp
typealias T_AdditiveExpression = Expression
typealias T_AdditiveOp = BinaryOp
typealias T_MultiplicativeExpression = Expression
typealias T_MultiplicativeOp = BinaryOp
typealias T_Factor = Expression

class Visitor : Visitor<
        Program,
        Declaration, LiteralExpression, Declaration,
        T_FunctionDeclarationSuffix, T_Type, T_TypedIdentifier, T_Statement,
        T_Expression, T_OrExpression, T_AndExpression, T_RelationalExpression,
        T_RelationalOp, T_AdditiveExpression, T_AdditiveOp, T_MultiplicativeExpression,
        T_MultiplicativeOp, T_Factor,
        Tuple2<Location, UnaryOp>> {
    override fun visitProgram(a: List<Union2<Declaration, Declaration>>): Program =
            Program(a.map { if (it.isA()) it.a() else it.b() })

    override fun visitVariableDeclaration(a1: Union2<Token, Token>, a2: Token, a3: Token, a4: LiteralExpression, a5: Token): Declaration =
            VariableDeclaration(
                    if (a1.isA()) VariableAccess.ReadOnly else VariableAccess.ReadWrite,
                    Identifier(a2.location, a2.lexeme),
                    a4)

    override fun visitLiteralExpression1(a: Token): LiteralExpression =
            LiteralExpressionValue(LiteralBool(a.location, true))

    override fun visitLiteralExpression2(a: Token): LiteralExpression =
            LiteralExpressionValue(LiteralBool(a.location, false))

    override fun visitLiteralExpression3(
            a1: Union2<Token, Token>?,
            a2: Union2<Token, Token>): LiteralExpression {
        val value =
                if (a2.isA())
                    LiteralInt(a2.a().location, a2.a().lexeme)
                else
                    LiteralFloat(a2.b().location, a2.b().lexeme)

        return if (a1 == null)
            LiteralExpressionValue(value)
        else {
            val sign =
                    if (a1.isA())
                        Tuple2(a1.a().location, UnaryOp.UnaryPlus)
                    else
                        Tuple2(a1.b().location, UnaryOp.UnaryMinus)

            LiteralExpressionUnaryValue(sign.a, sign.b, value)
        }
    }

    override fun visitFunctionDeclaration(a1: Token, a2: Token, a3: Token, a4: Tuple2<T_TypedIdentifier, List<Tuple2<Token, T_TypedIdentifier>>>?, a5: Token, a6: T_FunctionDeclarationSuffix): Declaration {
        val args =
                if (a4 == null)
                    listOf()
                else
                    listOf(a4.a) + a4.b.map { it.b }

        return FunctionDeclaration(Identifier(a2.location, a2.lexeme),
                args,
                a6.a, a6.b)
    }

    override fun visitFunctionDeclarationSuffix1(a1: Token, a2: T_Type, a3: Token, a4: List<T_Statement>, a5: Token, a6: T_Expression, a7: Token, a8: Token): T_FunctionDeclarationSuffix =
            Tuple2(a4, Tuple2(a2, a6))

    override fun visitFunctionDeclarationSuffix2(a1: Token, a2: List<T_Statement>, a3: Token): T_FunctionDeclarationSuffix =
            Tuple2(a2, null)

    override fun visitType1(a: Token): T_Type =
            Type.Int

    override fun visitType2(a: Token): T_Type =
            Type.Float

    override fun visitType3(a: Token): T_Type =
            Type.Bool

    override fun visitTypedIdentifier(a1: Token, a2: Token, a3: T_Type): T_TypedIdentifier =
            Tuple2(Identifier(a1.location, a1.lexeme), a3)

    override fun visitStatement1(a1: Union2<Token, Token>, a2: Token, a3: Token, a4: T_Expression, a5: Token): T_Statement =
            DeclarationStatement(if (a1.isA()) VariableAccess.ReadOnly else VariableAccess.ReadWrite, Identifier(a2.location, a2.lexeme), a4)

    override fun visitStatement2(a1: Token, a2: T_Expression, a3: T_Statement, a4: Tuple2<Token, T_Statement>?): T_Statement =
            IfThenElseStatement(a2, a3, a4?.b)

    override fun visitStatement3(a1: Token, a2: T_Expression, a3: T_Statement): T_Statement =
            WhileStatement(a2, a3)

    override fun visitStatement4(a1: Token, a2: List<T_Statement>, a3: Token): T_Statement =
            BlockStatement(a2)

    override fun visitStatement5(a1: Token, a2: Union2<Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>, Tuple2<Token, T_Expression>>, a3: Token): T_Statement =
            if (a2.isA()) {
                val args =
                        a2.a().b

                CallStatement(Identifier(a1.location, a1.lexeme),
                        if (args == null)
                            emptyList()
                        else
                            listOf(args.a) + args.b.map { it.b })
            } else
                AssignmentStatement(Identifier(a1.location, a1.lexeme), a2.b().b)

    override fun visitStatement6(a: Token): T_Statement =
            EmptyStatement

    override fun visitExpression(a1: T_OrExpression, a2: Tuple4<Token, T_Expression, Token, T_Expression>?): T_Expression =
            if (a2 == null)
                a1
            else
                TernaryExpression(a1, a2.b, a2.d)

    override fun visitOrExpression(a1: T_AndExpression, a2: List<Tuple2<Token, T_AndExpression>>): T_OrExpression =
            if (a2.isEmpty())
                a1
            else
                a2.fold(a1, { acc, t -> BinaryExpression(acc, BinaryOp.Or, t.b) })

    override fun visitAndExpression(a1: T_RelationalExpression, a2: List<Tuple2<Token, T_RelationalExpression>>): T_AndExpression =
            if (a2.isEmpty())
                a1
            else
                a2.fold(a1, { acc, t -> BinaryExpression(acc, BinaryOp.And, t.b) })

    override fun visitRelationalExpression(a1: T_AdditiveExpression, a2: Tuple2<T_RelationalOp, T_AdditiveExpression>?): T_RelationalExpression =
            if (a2 == null)
                a1
            else
                BinaryExpression(a1, a2.a, a2.b)

    override fun visitRelationalOp1(a: Token): T_RelationalOp =
            BinaryOp.Equal

    override fun visitRelationalOp2(a: Token): T_RelationalOp =
            BinaryOp.NotEqual

    override fun visitRelationalOp3(a: Token): T_RelationalOp =
            BinaryOp.LessEqual

    override fun visitRelationalOp4(a: Token): T_RelationalOp =
            BinaryOp.LessThan

    override fun visitRelationalOp5(a: Token): T_RelationalOp =
            BinaryOp.GreaterEqual

    override fun visitRelationalOp6(a: Token): T_RelationalOp =
            BinaryOp.GreaterThan

    override fun visitAdditiveExpression(a1: T_MultiplicativeExpression, a2: List<Tuple2<T_AdditiveOp, T_MultiplicativeExpression>>): T_AdditiveExpression =
            if (a2.isEmpty())
                a1
            else
                a2.fold(a1, { acc, t -> BinaryExpression(acc, t.a, t.b) })

    override fun visitAdditiveOp1(a: Token): T_AdditiveOp =
            BinaryOp.Plus

    override fun visitAdditiveOp2(a: Token): T_AdditiveOp =
            BinaryOp.Minus

    override fun visitMultiplicativeExpression(a1: T_Factor, a2: List<Tuple2<T_MultiplicativeOp, T_Factor>>): T_MultiplicativeExpression =
            if (a2.isEmpty())
                a1
            else
                a2.fold(a1, { acc, t -> BinaryExpression(acc, t.a, t.b) })

    override fun visitMultiplicativeOp1(a: Token): T_MultiplicativeOp =
            BinaryOp.Times

    override fun visitMultiplicativeOp2(a: Token): T_MultiplicativeOp =
            BinaryOp.Divide

    override fun visitFactor1(a: Token): T_Factor =
            LiteralValueExpression(LiteralInt(a.location, a.lexeme))

    override fun visitFactor2(a: Token): T_Factor =
            LiteralValueExpression(LiteralFloat(a.location, a.lexeme))

    override fun visitFactor3(a: Token): T_Factor =
            LiteralValueExpression(LiteralString(a.location, a.lexeme))

    override fun visitFactor4(a: Token): T_Factor =
            LiteralValueExpression(LiteralBool(a.location, true))

    override fun visitFactor5(a: Token): T_Factor =
            LiteralValueExpression(LiteralBool(a.location, false))

    override fun visitFactor6(a1: Tuple2<Location, UnaryOp>, a2: T_Factor): T_Factor =
            UnaryExpression(a1.a, a1.b, a2)

    override fun visitFactor7(a1: Token, a2: T_Expression, a3: Token): T_Factor =
            Parenthesis(a1.location + a3.location, a2)

    override fun visitFactor8(a1: Token, a2: Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>?): T_Factor =
            if (a2 == null) {
                IdentifierReference(Identifier(a1.location, a1.lexeme))
            } else {
                val arguments =
                        if (a2.b == null)
                            listOf()
                        else
                            listOf(a2.b.a).plus(a2.b.b.map { it.b })

                CallExpression(Identifier(a1.location, a1.lexeme), arguments)
            }

    override fun visitUnaryOp1(a: Token): Tuple2<Location, UnaryOp> =
            Tuple2(a.location, UnaryOp.UnaryNot)

    override fun visitUnaryOp2(a: Token): Tuple2<Location, UnaryOp> =
            Tuple2(a.location, UnaryOp.UnaryMinus)

    override fun visitUnaryOp3(a: Token): Tuple2<Location, UnaryOp> =
            Tuple2(a.location, UnaryOp.UnaryPlus)
}