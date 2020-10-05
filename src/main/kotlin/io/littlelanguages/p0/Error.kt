package io.littlelanguages.p0

import io.littlelanguages.data.Yamlable
import io.littlelanguages.p0.dynamic.tst.BinaryOp
import io.littlelanguages.p0.dynamic.tst.Type
import io.littlelanguages.p0.dynamic.tst.UnaryOp
import io.littlelanguages.p0.lexer.Position
import io.littlelanguages.p0.lexer.TToken
import io.littlelanguages.p0.lexer.Token

sealed class Errors : Yamlable

data class ParseError(
        val found: Token,
        val expected: Set<TToken>) : Errors() {
    override fun yaml(): Any =
            singletonMap("ParseError", mapOf(
                    Pair("found", found),
                    Pair("expected", expected)
            ))
}

data class AttemptToRedefineDeclarationError(
        val position: Position,
        val name: String) : Errors() {
    override fun yaml(): Any =
            singletonMap("AttemptToRedefineDeclaration", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class BinaryExpressionOperandsIncompatible(
        val op: BinaryOp,
        val position1: Position,
        val type1: Type,
        val position2: Position,
        val type2: Type) : Errors() {
    override fun yaml(): Any =
            singletonMap("BinaryExpressionOperandsIncompatible", mapOf(
                    Pair("op", op),
                    Pair("position1", position1),
                    Pair("type1", type1),
                    Pair("position2", position2),
                    Pair("type2", type2)
            ))
}

data class BinaryExpressionRequiresOperandType(
        val op: BinaryOp,
        val type: Type,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("BinaryExpressionRequiresOperandType", mapOf(
                    Pair("op", op),
                    Pair("type", type),
                    Pair("position", position.yaml())
            ))
}

data class FunctionReturnTypeMismatch(
        val position: Position,
        val name: String,
        val typ: Type) : Errors() {
    override fun yaml(): Any =
            singletonMap("FunctionReturnTypeMismatch", mapOf(
                    Pair("position", position.yaml()),
                    Pair("name", name),
                    Pair("type", typ)
            ))
}

data class IfGuardNotBoolean(
        val type: Type,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("IfGuardNotBoolean", mapOf(
                    Pair("type", type),
                    Pair("position", position.yaml())
            ))
}

data class InvalidDeclarationOfMain(
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("InvalidDeclarationOfMain", mapOf(
                    Pair("position", position.yaml())
            ))
}

data class IncompatibleArgumentType(
        val argumentType: Type,
        val parameterType: Type,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("IncompatibleArgumentType", mapOf(
                    Pair("argumentType", argumentType),
                    Pair("parameterType", parameterType),
                    Pair("position", position.yaml())
            ))
}

data class LiteralFloatOverFlowError(
        val position: Position,
        val value: String) : Errors() {
    override fun yaml(): Any =
            singletonMap("LiteralFloatOverflow", mapOf(
                    Pair("text", value),
                    Pair("position", position.yaml())
            ))
}

data class LiteralIntOverFlowError(
        val position: Position,
        val value: String) : Errors() {
    override fun yaml(): Any =
            singletonMap("LiteralIntOverflow", mapOf(
                    Pair("text", value),
                    Pair("position", position.yaml())
            ))
}

data class MismatchInNumberOfParameters(
        val arguments: Int,
        val parameters: Int,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("MismatchInNumberOfParameters", mapOf(
                    Pair("arguments", arguments),
                    Pair("parameters", parameters),
                    Pair("position", position.yaml())
            ))
}

data class TernaryExpressionResultIncompatible(
        val thenPosition: Position,
        val elsePosition: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("TernaryExpressionResultIncompatible", mapOf(
                    Pair("thenPosition", thenPosition.yaml()),
                    Pair("elsePosition", elsePosition.yaml())
            ))
}

data class TernaryExpressionNotBoolean(
        val boolPosition: Position,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("TernaryExpressionNotBoolean", mapOf(
                    Pair("boolPosition", boolPosition.yaml()),
                    Pair("position", position.yaml())
            ))
}

data class UnableToAssignToConstant(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToAssignToConstant", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnableToAssignIncompatibleTypes(
        val type: Type,
        val position: Position,
        val expressionType: Type,
        val expressionPosition: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToAssignIncompatibleTypes", mapOf(
                    Pair("type", type),
                    Pair("position", position.yaml()),
                    Pair("expressionType", expressionType),
                    Pair("expressionPosition", expressionPosition.yaml())
            ))
}

data class UnableToAssignToFunction(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToAssignToFunction", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnableToCallUnitFunctionAsValueFunction(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToCallUnitFunctionAsValueFunction", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnableToCallConstantAsFunction(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToCallConstantAsFunction", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnableToCallVariableAsFunction(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToCallVariableAsFunction", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnaryExpressionRequiresOperandType(
        val op: UnaryOp,
        val type: Type,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnaryExpressionRequiresOperandType", mapOf(
                    Pair("op", op),
                    Pair("type", type),
                    Pair("position", position.yaml())
            ))
}

data class UnableToReferenceFunction(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToReferenceFunction", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnableToCallValueFunctionAsUnitFunction(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnableToCallValueFunctionAsUnitFunction", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class UnknownIdentifier(
        val name: String,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("UnknownIdentifier", mapOf(
                    Pair("name", name),
                    Pair("position", position.yaml())
            ))
}

data class WhileGuardNotBoolean(
        val type: Type,
        val position: Position) : Errors() {
    override fun yaml(): Any =
            singletonMap("WhileGuardNotBoolean", mapOf(
                    Pair("type", type),
                    Pair("position", position.yaml())
            ))
}
