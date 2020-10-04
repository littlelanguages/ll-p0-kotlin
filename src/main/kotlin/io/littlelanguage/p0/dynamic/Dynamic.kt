package io.littlelanguage.p0.dynamic

import io.littlelanguage.data.Either
import io.littlelanguage.data.Left
import io.littlelanguage.data.Right
import io.littlelanguage.p0.*
import io.littlelanguage.p0.dynamic.tst.*
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt


open class Binding

data class Constant(
        val t: Type) : Binding()

data class Variable(
        val t: Type) : Binding()

data class Function(
        val ps: List<Type>,
        val r: Type?) : Binding()

typealias Bindings =
        Map<String, Binding>


fun translate(p: io.littlelanguage.p0.static.ast.Program): Either<List<Errors>, Program> {
    val translator =
            Translator()

    val pp =
            translator.p(p)

    return if (translator.errors.isEmpty()) Right(pp) else Left(translator.errors.toList())
}


class Translator {
    val errors =
            mutableListOf<Errors>()


    fun p(p: io.littlelanguage.p0.static.ast.Program): Program {
        val sigma =
                mutableMapOf<String, Binding>()

        for (d in p.declarations) {
            if (sigma.contains(d.identifier.name))
                reportError(AttemptToRedefineDeclarationError(d.identifier.position, d.identifier.name))
            else
                sigma[d.identifier.name] = binding(d)
        }

        val main =
                p.declarations.find { it.identifier.name == "main" }

        return when (main) {
            null ->
                Program(p.declarations.map { d(it, sigma + Pair("main", Function(emptyList(), null))) }, EmptyStatement)

            is io.littlelanguage.p0.static.ast.FunctionDeclaration -> {
                if (main.arguments.isNotEmpty() || main.suffix != null)
                    reportError(InvalidDeclarationOfMain(main.identifier.position))

                val ssp =
                        ss(main.statements, sigma).first

                Program(p.declarations.filter { it.identifier.name != "main" }.map { d(it, sigma) }, if (ssp.size == 1) ssp[0] else BlockStatement(ssp))
            }

            else -> {
                reportError(InvalidDeclarationOfMain(main.identifier.position))
                Program(p.declarations.map { d(it, sigma) }, EmptyStatement)
            }
        }
    }

    fun d(d: io.littlelanguage.p0.static.ast.Declaration, sigma: Bindings): Declaration =
            when (d) {
                is io.littlelanguage.p0.static.ast.VariableDeclaration -> {
                    if (d.access == io.littlelanguage.p0.static.ast.VariableAccess.ReadOnly)
                        ConstantDeclaration(d.identifier.name, le(d.expression))
                    else
                        VariableDeclaration(d.identifier.name, le(d.expression))
                }

                is io.littlelanguage.p0.static.ast.FunctionDeclaration -> {
                    fun validateParameters() {
                        val names =
                                mutableSetOf<String>()

                        for (a in d.arguments) {
                            if (names.contains(a.first.name))
                                reportError(AttemptToRedefineDeclarationError(a.first.position, a.first.name))

                            names.add(a.first.name)
                        }
                    }

                    fun validateDeclarations() {
                        val names =
                                mutableSetOf<String>()

                        for (s in d.statements) {
                            if (s is io.littlelanguage.p0.static.ast.DeclarationStatement) {
                                if (names.contains(s.identifier.name)) {
                                    reportError(AttemptToRedefineDeclarationError(s.identifier.position, s.identifier.name))
                                }

                                names.add(s.identifier.name)
                            }
                        }
                    }

                    validateParameters()
                    validateDeclarations()

                    val sigmap =
                            sigma.plus(d.arguments.map { Pair(it.first.name, Variable(it.second.toType())) })

                    val ssp =
                            ss(d.statements, sigmap)

                    val suffix =
                            d.suffix

                    if (suffix == null)
                        FunctionDeclaration(d.identifier.name, d.arguments.map { Pair(it.first.name, it.second.toType()) }, ssp.first, null)
                    else {
                        val ep =
                                e(suffix.second, ssp.second)

                        if (ep.typeOf() != suffix.first.toType()) {
                            reportError(FunctionReturnTypeMismatch(d.identifier.position, d.identifier.name, suffix.first.toType()))
                        }

                        FunctionDeclaration(d.identifier.name, d.arguments.map { Pair(it.first.name, it.second.toType()) }, ssp.first, ep)
                    }
                }
            }

    fun le(ast: io.littlelanguage.p0.static.ast.LiteralExpression): LiteralValue =
            when (ast) {
                is io.littlelanguage.p0.static.ast.LiteralExpressionValue ->
                    lv(ast.value)

                is io.littlelanguage.p0.static.ast.LiteralExpressionUnaryValue ->
                    when (ast.op) {
                        io.littlelanguage.p0.static.ast.UnaryOp.UnaryMinus ->
                            if (ast.value is io.littlelanguage.p0.static.ast.LiteralInt) {
                                if (ast.value.value == "2147483648")
                                    LiteralInt(Int.MIN_VALUE)
                                else
                                    try {
                                        LiteralInt(parseInt("-" + ast.value.value))
                                    } catch (e: NumberFormatException) {
                                        reportError(LiteralIntOverFlowError(ast.position + ast.value.position, "-" + ast.value.value))
                                        LiteralInt(0)
                                    }
                            } else {
                                val value =
                                        ast.value as io.littlelanguage.p0.static.ast.LiteralFloat

                                val v =
                                        parseFloat("-" + value.value)

                                if (v == Float.NEGATIVE_INFINITY)
                                    reportError(LiteralFloatOverFlowError(ast.position + value.position, "-" + value.value))

                                LiteralFloat(v)
                            }

                        else ->
                            lv(ast.value)
                    }
            }

    fun lv(ast: io.littlelanguage.p0.static.ast.LiteralValue): LiteralValue =
            when (ast) {
                is io.littlelanguage.p0.static.ast.LiteralBool ->
                    LiteralBool(ast.value)

                is io.littlelanguage.p0.static.ast.LiteralInt ->
                    try {
                        LiteralInt(parseInt(ast.value))
                    } catch (e: NumberFormatException) {
                        reportError(LiteralIntOverFlowError(ast.position, ast.value))
                        LiteralInt(0)
                    }

                is io.littlelanguage.p0.static.ast.LiteralFloat -> {
                    val v =
                            parseFloat(ast.value)

                    if (v == Float.POSITIVE_INFINITY)
                        reportError(LiteralFloatOverFlowError(ast.position, ast.value))

                    LiteralFloat(v)
                }
                is io.littlelanguage.p0.static.ast.LiteralString -> {
                    val r =
                            StringBuilder()

                    var lp =
                            1

                    while (ast.value[lp] != '"') {
                        if (ast.value[lp] == '\\') {
                            lp += 1
                            if (ast.value[lp] == '\\')
                                r.append('\\')
                            else
                                r.append('"')
                        } else {
                            r.append(ast.value[lp])
                            lp += 1
                        }
                    }
                    LiteralString(r.toString())
                }
            }

    fun ss(ss: List<io.littlelanguage.p0.static.ast.Statement>, sigma: Bindings): Pair<List<Statement>, Bindings> {
        var sigmap =
                sigma

        val ssp =
                mutableListOf<Statement>()

        for (s in ss) {
            val r =
                    s(s, sigmap)

            ssp.add(r.first)
            sigmap = r.second
        }

        return Pair(ssp.toList(), sigmap)
    }

    fun s(s: io.littlelanguage.p0.static.ast.Statement, sigma: Bindings): Pair<Statement, Bindings> =
            when (s) {
                is io.littlelanguage.p0.static.ast.AssignmentStatement -> {
                    val binding =
                            sigma[s.identifier.name]

                    val ep =
                            e(s.expression, sigma)

                    when (binding) {
                        null ->
                            reportError(UnknownIdentifier(s.identifier.name, s.identifier.position))

                        is Constant ->
                            reportError(UnableToAssignToConstant(s.identifier.name, s.identifier.position))

                        is Function ->
                            reportError(UnableToAssignToFunction(s.identifier.name, s.identifier.position))

                        is Variable ->
                            if (binding.t != ep.typeOf())
                                reportError(UnableToAssignIncompatibleTypes(binding.t, s.identifier.position, ep.typeOf(), s.expression.position()))
                    }

                    Pair(AssignmentStatement(s.identifier.name, ep), sigma)
                }

                is io.littlelanguage.p0.static.ast.DeclarationStatement ->
                    if (s.access == io.littlelanguage.p0.static.ast.VariableAccess.ReadOnly) {
                        val ep =
                                e(s.expression, sigma)

                        Pair(ConstantDeclarationStatement(s.identifier.name, ep), sigma + Pair(s.identifier.name, Constant(ep.typeOf())))
                    } else {
                        val ep =
                                e(s.expression, sigma)

                        Pair(VariableDeclarationStatement(s.identifier.name, ep), sigma + Pair(s.identifier.name, Variable(ep.typeOf())))
                    }

                is io.littlelanguage.p0.static.ast.IfThenElseStatement -> {
                    val ep =
                            e(s.expression, sigma)

                    if (!setOf(Type.Bool, Type.TError).contains(ep.typeOf()))
                        reportError(IfGuardNotBoolean(ep.typeOf(), s.expression.position()))

                    if (s.statement2 == null)
                        Pair(IfThenElseStatement(ep, s(s.statement1, sigma).first, null), sigma)
                    else
                        Pair(IfThenElseStatement(ep, s(s.statement1, sigma).first, s(s.statement2, sigma).first), sigma)
                }

                is io.littlelanguage.p0.static.ast.WhileStatement -> {
                    val ep =
                            e(s.expression, sigma)

                    if (!setOf(Type.Bool, Type.TError).contains(ep.typeOf()))
                        reportError(WhileGuardNotBoolean(ep.typeOf(), s.expression.position()))

                    Pair(WhileStatement(ep, s(s.statement, sigma).first), sigma)
                }

                is io.littlelanguage.p0.static.ast.CallStatement -> {
                    val binding =
                            sigma[s.identifier.name]

                    when (binding) {
                        is Constant -> {
                            reportError(UnableToCallConstantAsFunction(s.identifier.name, s.identifier.position))
                            Pair(CallStatement(s.identifier.name, emptyList()), sigma)
                        }

                        is Variable -> {
                            reportError(UnableToCallVariableAsFunction(s.identifier.name, s.identifier.position))
                            Pair(CallStatement(s.identifier.name, emptyList()), sigma)
                        }

                        is Function -> {
                            val argsp =
                                    s.expressions.map { e(it, sigma) }

                            if (argsp.size != binding.ps.size)
                                reportError(MismatchInNumberOfParameters(argsp.size, binding.ps.size, s.identifier.position))
                            else {
                                for ((index, parameter) in binding.ps.withIndex()) {
                                    if (parameter != argsp[index].typeOf())
                                        reportError(IncompatibleArgumentType(argsp[index].typeOf(), parameter, s.expressions[index].position()))
                                }
                            }

                            if (binding.r == null) {
                                Pair(CallStatement(s.identifier.name, argsp), sigma)
                            } else
                                reportError(UnableToCallValueFunctionAsUnitFunction(s.identifier.name, s.identifier.position))
                            Pair(CallStatement(s.identifier.name, argsp), sigma)
                        }

                        else -> {
                            if (s.identifier.name == "print" || s.identifier.name == "println")
                                Pair(CallStatement(s.identifier.name, s.expressions.map { e(it, sigma) }), sigma)
                            else {
                                reportError(UnknownIdentifier(s.identifier.name, s.identifier.position))
                                Pair(CallStatement(s.identifier.name, emptyList()), sigma)
                            }
                        }
                    }
                }

                is io.littlelanguage.p0.static.ast.BlockStatement ->
                    Pair(BlockStatement(ss(s.statements, sigma).first), sigma)

                is io.littlelanguage.p0.static.ast.EmptyStatement ->
                    Pair(EmptyStatement, sigma)
            }

    fun e(e: io.littlelanguage.p0.static.ast.Expression, sigma: Bindings): Expression =
            when (e) {
                is io.littlelanguage.p0.static.ast.TernaryExpression -> {
                    val e1p =
                            e(e.expression1, sigma)
                    val e2p =
                            e(e.expression2, sigma)
                    val e3p =
                            e(e.expression3, sigma)

                    if (!setOf(Type.Bool, Type.TError).contains(e1p.typeOf()))
                        reportError(TernaryExpressionNotBoolean(e.expression1.position(), e.position()))

                    if (e2p.typeOf() != Type.TError && e3p.typeOf() != Type.TError && e2p.typeOf() != e3p.typeOf())
                        reportError(TernaryExpressionResultIncompatible(e.expression2.position(), e.expression3.position()))

                    TernaryExpression(e1p, e2p, e3p)
                }

                is io.littlelanguage.p0.static.ast.BinaryExpression -> {
                    val opp =
                            bo(e.op)

                    val e1p =
                            e(e.expression1, sigma)
                    val e2p =
                            e(e.expression2, sigma)

                    if (opp == BinaryOp.And || opp == BinaryOp.Or) {
                        if (!setOf(Type.Bool, Type.TError).contains(e1p.typeOf()))
                            reportError(BinaryExpressionRequiresOperandType(opp, e1p.typeOf(), e.expression1.position()))

                        if (!setOf(Type.Bool, Type.TError).contains(e2p.typeOf()))
                            reportError(BinaryExpressionRequiresOperandType(opp, e2p.typeOf(), e.expression2.position()))

                        BinaryExpression(opp, e1p, e2p)
                    } else if (opp == BinaryOp.Equal || opp == BinaryOp.NotEqual) {
                        if (e1p.typeOf() != Type.TError && e2p.typeOf() != Type.TError && e1p.typeOf() != e2p.typeOf())
                            reportError(BinaryExpressionOperandsIncompatible(opp, e.expression1.position(), e1p.typeOf(), e.expression2.position(), e2p.typeOf()))

                        BinaryExpression(opp, e1p, e2p)
                    } else {
                        if (!setOf(Type.Int, Type.Float, Type.TError).contains(e1p.typeOf()))
                            reportError(BinaryExpressionRequiresOperandType(opp, e1p.typeOf(), e.expression1.position()))

                        if (!setOf(Type.Int, Type.Float, Type.TError).contains(e2p.typeOf()))
                            reportError(BinaryExpressionRequiresOperandType(opp, e2p.typeOf(), e.expression2.position()))

                        if (e1p.typeOf() != Type.TError && e2p.typeOf() != Type.TError && e1p.typeOf() != e2p.typeOf())
                            reportError(BinaryExpressionOperandsIncompatible(opp, e.expression1.position(), e1p.typeOf(), e.expression2.position(), e2p.typeOf()))

                        BinaryExpression(opp, e1p, e2p)
                    }
                }

                is io.littlelanguage.p0.static.ast.UnaryExpression -> {
                    if (e.op == io.littlelanguage.p0.static.ast.UnaryOp.UnaryMinus
                            && e.expression is io.littlelanguage.p0.static.ast.LiteralValueExpression
                            && e.expression.value is io.littlelanguage.p0.static.ast.LiteralInt) {
                        LiteralValueExpression(le(io.littlelanguage.p0.static.ast.LiteralExpressionUnaryValue(e.position, e.op, e.expression.value)))
                    } else {
                        val opp =
                                uo(e.op)

                        val ep =
                                e(e.expression, sigma)

                        if (opp == UnaryOp.UnaryNot) {
                            if (!setOf(Type.Bool, Type.TError).contains(ep.typeOf()))
                                reportError(UnaryExpressionRequiresOperandType(opp, ep.typeOf(), e.expression.position()))

                            UnaryExpression(Type.Bool, opp, ep)
                        } else {
                            if (!setOf(Type.Int, Type.Float, Type.TError).contains(ep.typeOf()))
                                reportError(UnaryExpressionRequiresOperandType(opp, ep.typeOf(), e.expression.position()))

                            UnaryExpression(ep.typeOf(), opp, ep)
                        }
                    }
                }

                is io.littlelanguage.p0.static.ast.CallExpression -> {
                    val binding =
                            sigma[e.identifier.name]

                    when (binding) {
                        is Constant -> {
                            reportError(UnableToCallConstantAsFunction(e.identifier.name, e.identifier.position))
                            CallExpression(Type.TError, e.identifier.name, emptyList())
                        }

                        is Variable -> {
                            reportError(UnableToCallVariableAsFunction(e.identifier.name, e.identifier.position))
                            CallExpression(Type.TError, e.identifier.name, emptyList())
                        }

                        is Function -> {
                            val argsp =
                                    e.expressions.map { e(it, sigma) }

                            if (argsp.size != binding.ps.size)
                                reportError(MismatchInNumberOfParameters(argsp.size, binding.ps.size, e.identifier.position))
                            else {
                                for ((index, parameter) in binding.ps.withIndex()) {
                                    if (parameter != argsp[index].typeOf())
                                        reportError(IncompatibleArgumentType(argsp[index].typeOf(), parameter, e.expressions[index].position()))
                                }
                            }

                            if (binding.r == null) {
                                reportError(UnableToCallUnitFunctionAsValueFunction(e.identifier.name, e.identifier.position))
                                CallExpression(Type.TError, e.identifier.name, argsp)
                            } else
                                CallExpression(binding.r, e.identifier.name, argsp)
                        }

                        else -> {
                            reportError(UnknownIdentifier(e.identifier.name, e.identifier.position))
                            IdentifierReference(Type.TError, e.identifier.name)
                        }
                    }
                }

                is io.littlelanguage.p0.static.ast.IdentifierReference -> {
                    val binding =
                            sigma[e.identifier.name]

                    when (binding) {
                        is Constant ->
                            IdentifierReference(binding.t, e.identifier.name)

                        is Variable ->
                            IdentifierReference(binding.t, e.identifier.name)

                        is Function -> {
                            reportError(UnableToReferenceFunction(e.identifier.name, e.position()))
                            IdentifierReference(Type.TError, e.identifier.name)
                        }

                        else -> {
                            reportError(UnknownIdentifier(e.identifier.name, e.identifier.position))
                            IdentifierReference(Type.TError, e.identifier.name)
                        }
                    }
                }

                is io.littlelanguage.p0.static.ast.Parenthesis ->
                    e(e.expression, sigma)

                is io.littlelanguage.p0.static.ast.LiteralValueExpression ->
                    LiteralValueExpression(lv(e.value))
            }

    private fun bo(op: io.littlelanguage.p0.static.ast.BinaryOp): BinaryOp =
            when (op) {
                io.littlelanguage.p0.static.ast.BinaryOp.Divide -> BinaryOp.Divide
                io.littlelanguage.p0.static.ast.BinaryOp.Minus -> BinaryOp.Minus
                io.littlelanguage.p0.static.ast.BinaryOp.Plus -> BinaryOp.Plus
                io.littlelanguage.p0.static.ast.BinaryOp.Times -> BinaryOp.Times
                io.littlelanguage.p0.static.ast.BinaryOp.Equal -> BinaryOp.Equal
                io.littlelanguage.p0.static.ast.BinaryOp.GreaterEqual -> BinaryOp.GreaterEqual
                io.littlelanguage.p0.static.ast.BinaryOp.GreaterThan -> BinaryOp.GreaterThan
                io.littlelanguage.p0.static.ast.BinaryOp.LessEqual -> BinaryOp.LessEqual
                io.littlelanguage.p0.static.ast.BinaryOp.LessThan -> BinaryOp.LessThan
                io.littlelanguage.p0.static.ast.BinaryOp.NotEqual -> BinaryOp.NotEqual
                io.littlelanguage.p0.static.ast.BinaryOp.And -> BinaryOp.And
                io.littlelanguage.p0.static.ast.BinaryOp.Or -> BinaryOp.Or
            }

    private fun uo(op: io.littlelanguage.p0.static.ast.UnaryOp): UnaryOp =
            when (op) {
                io.littlelanguage.p0.static.ast.UnaryOp.UnaryNot -> UnaryOp.UnaryNot
                io.littlelanguage.p0.static.ast.UnaryOp.UnaryMinus -> UnaryOp.UnaryMinus
                io.littlelanguage.p0.static.ast.UnaryOp.UnaryPlus -> UnaryOp.UnaryPlus
            }

    private fun reportError(error: Errors) {
        errors.add(error)
    }
}


private fun binding(d: io.littlelanguage.p0.static.ast.Declaration): Binding =
        when (d) {
            is io.littlelanguage.p0.static.ast.VariableDeclaration ->
                if (d.access == io.littlelanguage.p0.static.ast.VariableAccess.ReadOnly)
                    Constant(d.expression.typeOf())
                else
                    Variable(d.expression.typeOf())
            is io.littlelanguage.p0.static.ast.FunctionDeclaration -> {
                val suffix = d.suffix

                if (suffix == null)
                    Function(d.arguments.map { it.second.toType() }, null)
                else
                    Function(d.arguments.map { it.second.toType() }, suffix.first.toType())
            }
        }


private fun io.littlelanguage.p0.static.ast.LiteralExpression.typeOf(): Type =
        when (this) {
            is io.littlelanguage.p0.static.ast.LiteralExpressionValue ->
                this.value.typeOf()

            is io.littlelanguage.p0.static.ast.LiteralExpressionUnaryValue ->
                when (this.op) {
                    io.littlelanguage.p0.static.ast.UnaryOp.UnaryNot ->
                        Type.Bool

                    else ->
                        this.value.typeOf()
                }
        }

private fun io.littlelanguage.p0.static.ast.LiteralValue.typeOf(): Type =
        when (this) {
            is io.littlelanguage.p0.static.ast.LiteralBool -> Type.Bool
            is io.littlelanguage.p0.static.ast.LiteralInt -> Type.Int
            is io.littlelanguage.p0.static.ast.LiteralFloat -> Type.Float
            is io.littlelanguage.p0.static.ast.LiteralString -> Type.String
        }

private fun io.littlelanguage.p0.static.ast.Type.toType(): Type =
        when (this) {
            io.littlelanguage.p0.static.ast.Type.Int -> Type.Int
            io.littlelanguage.p0.static.ast.Type.Float -> Type.Float
            io.littlelanguage.p0.static.ast.Type.Bool -> Type.Bool
        }
