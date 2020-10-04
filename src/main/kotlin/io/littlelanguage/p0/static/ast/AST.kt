package io.littlelanguage.p0.static.ast

import io.littlelanguage.data.Yamlable
import io.littlelanguage.p0.lexer.Position
import io.littlelanguage.p0.lexer.Positionable


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
        val arguments: List<Pair<Identifier, Type>>,
        val statements: List<Statement>,
        val suffix: Pair<Type, Expression>?) : Declaration(identifier) {
    override fun yaml(): Any {
        val suffix =
                suffix

        return if (suffix == null)
            singletonMap("FunctionDeclaration", mapOf(
                    Pair("identifier", identifier.yaml()),
                    Pair("arguments", arguments.map {
                        mapOf(
                                Pair("name", it.first.yaml()),
                                Pair("type", it.second.yaml()))
                    }),
                    Pair("s", statements.map { it.yaml() })
            ))
        else
            singletonMap("FunctionDeclaration", mapOf(
                    Pair("identifier", identifier.yaml()),
                    Pair("arguments", arguments.map {
                        mapOf(
                                Pair("name", it.first.yaml()),
                                Pair("type", it.second.yaml()))
                    }),
                    Pair("result", suffix.first.yaml()),
                    Pair("s", statements.map { it.yaml() }),
                    Pair("e", suffix.second.yaml())
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


sealed class Expression : Positionable, Yamlable

data class TernaryExpression(
        val expression1: Expression,
        val expression2: Expression,
        val expression3: Expression) : Expression() {
    override fun position(): Position = expression1.position() + expression3.position()

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
    override fun position(): Position = expression1.position() + expression2.position()

    override fun yaml(): Any =
            singletonMap("BinaryExpression", mapOf(
                    Pair("e1", expression1.yaml()),
                    Pair("op", op.yaml()),
                    Pair("e2", expression2.yaml())))
}

data class UnaryExpression(
        val position: Position,
        val op: UnaryOp,
        val expression: Expression) : Expression() {
    override fun position(): Position = position + expression.position()

    override fun yaml(): Any =
            singletonMap("UnaryExpression", mapOf(
                    Pair("position", position.yaml()),
                    Pair("op", op.yaml()),
                    Pair("e", expression.yaml())))
}

data class CallExpression(
        val identifier: Identifier,
        val expressions: List<Expression>) : Expression() {
    override fun position(): Position =
            if (expressions.isEmpty())
                identifier.position
            else
                identifier.position + expressions.last().position()

    override fun yaml(): Any =
            singletonMap("CallExpression", mapOf(
                    Pair("name", identifier.yaml()),
                    Pair("parameters", expressions.map { it.yaml() })))
}

data class IdentifierReference(
        val identifier: Identifier) : Expression() {
    override fun position(): Position = identifier.position

    override fun yaml(): Any =
            mapOf(Pair("IdentifierReference", identifier.yaml()))
}

data class Parenthesis(
        val position: Position,
        val expression: Expression) : Expression() {
    override fun position(): Position = position

    override fun yaml(): Any =
            singletonMap("Parenthesis", mapOf(
                    Pair("e", expression.yaml()),
                    Pair("position", position.yaml())))
}

data class LiteralValueExpression(
        val value: LiteralValue) : Expression() {
    override fun position(): Position = value.position()

    override fun yaml(): Any =
            singletonMap("LiteralValue", mapOf(
                    Pair("value", value.yaml())))
}


sealed class LiteralExpression : Positionable, Yamlable

data class LiteralExpressionValue(
        val value: LiteralValue) : LiteralExpression() {
    override fun position(): Position = value.position()

    override fun yaml(): Any =
            singletonMap("LiteralExpressionValue", mapOf(
                    Pair("value", value.yaml())))
}

data class LiteralExpressionUnaryValue(
        val position: Position,
        val op: UnaryOp,
        val value: LiteralValue) : LiteralExpression() {
    override fun position(): Position = position + value.position()

    override fun yaml(): Any =
            singletonMap("LiteralExpressionUnaryValue", mapOf(
                    Pair("position", position.yaml()),
                    Pair("op", op.yaml()),
                    Pair("value", value.yaml())))
}

sealed class LiteralValue : Positionable, Yamlable

data class LiteralBool(
        val position: Position,
        val value: Boolean) : LiteralValue() {
    override fun position(): Position = position

    override fun yaml(): Any =
            singletonMap("LiteralBool", mapOf(
                    Pair("value", if (value) "True" else "False"),
                    Pair("position", position.yaml())))
}

data class LiteralInt(
        val position: Position,
        val value: String) : LiteralValue() {
    override fun position(): Position = position

    override fun yaml(): Any =
            singletonMap("LiteralInt", mapOf(
                    Pair("value", value),
                    Pair("position", position.yaml())))
}

data class LiteralFloat(
        val position: Position,
        val value: String) : LiteralValue() {
    override fun position(): Position = position

    override fun yaml(): Any =
            singletonMap("LiteralFloat", mapOf(
                    Pair("value", value),
                    Pair("position", position.yaml())))
}

data class LiteralString(
        val position: Position,
        val value: String) : LiteralValue() {
    override fun position(): Position = position

    override fun yaml(): Any =
            singletonMap("LiteralString", mapOf(
                    Pair("value", value),
                    Pair("position", position.yaml())))
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
        val position: Position,
        val name: String) : Yamlable {
    override fun yaml(): Any =
            mapOf(
                    Pair("value", name),
                    Pair("position", position.yaml())
            )
}

