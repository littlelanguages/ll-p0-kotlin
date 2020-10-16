package io.littlelanguages.p0.static

import io.littlelanguages.data.Tuple2
import io.littlelanguages.data.Tuple3
import io.littlelanguages.data.Tuple4
import io.littlelanguages.data.Union2
import io.littlelanguages.p0.lexer.Token

interface Visitor<
        T_Program, T_VariableDeclaration, T_LiteralExpression, T_FunctionDeclaration,
        T_FunctionDeclarationSuffix, T_Type, T_TypedIdentifier, T_Statement,
        T_Expression, T_OrExpression, T_AndExpression, T_RelationalExpression,
        T_RelationalOp, T_AdditiveExpression, T_AdditiveOp, T_MultiplicativeExpression,
        T_MultiplicativeOp, T_Factor, T_UnaryOp> {
    /**
     * Program: {VariableDeclaration | FunctionDeclaration};
     */
    fun visitProgram(a: List<Union2<T_VariableDeclaration, T_FunctionDeclaration>>): T_Program


    /**
     * VariableDeclaration: ("const" | "let") Identifier "=" LiteralExpression ";";
     */
    fun visitVariableDeclaration(
            a1: Union2<Token, Token>,
            a2: Token,
            a3: Token,
            a4: T_LiteralExpression,
            a5: Token): T_VariableDeclaration


    /**
     * LiteralExpression: "true";
     */
    fun visitLiteralExpression1(a: Token): T_LiteralExpression

    /**
     * LiteralExpression: "false";
     */
    fun visitLiteralExpression2(a: Token): T_LiteralExpression

    /**
     * LiteralExpression: ["+" | "-"] (LiteralInt | LiteralFloat);
     */
    fun visitLiteralExpression3(
            a1: Union2<Token, Token>?,
            a2: Union2<Token, Token>): T_LiteralExpression


    /**
     * FunctionDeclaration: "fun" Identifier "(" [TypedIdentifier {"," TypedIdentifier}] ")" FunctionDeclarationSuffix;
     */
    fun visitFunctionDeclaration(
            a1: Token,
            a2: Token,
            a3: Token,
            a4: Tuple2<T_TypedIdentifier, List<Tuple2<Token, T_TypedIdentifier>>>?,
            a5: Token,
            a6: T_FunctionDeclarationSuffix): T_FunctionDeclaration

    /**
     * FunctionDeclarationSuffix: ":" Type "{" {Statement} "return" Expression ";" "}";
     */
    fun visitFunctionDeclarationSuffix1(
            a1: Token,
            a2: T_Type,
            a3: Token,
            a4: List<T_Statement>,
            a5: Token,
            a6: T_Expression,
            a7: Token,
            a8: Token): T_FunctionDeclarationSuffix

    /**
     * FunctionDeclarationSuffix: "{" {Statement} "}";
     */
    fun visitFunctionDeclarationSuffix2(
            a1: Token,
            a2: List<T_Statement>,
            a3: Token): T_FunctionDeclarationSuffix


    /**
     * Type: "Int";
     */
    fun visitType1(a: Token): T_Type

    /**
     * Type: "Float";
     */
    fun visitType2(a: Token): T_Type

    /**
     * Type: "Bool";
     */
    fun visitType3(a: Token): T_Type

    /**
     * TypedIdentifier: Identifier ":" Type;
     */
    fun visitTypedIdentifier(
            a1: Token,
            a2: Token,
            a3: T_Type): T_TypedIdentifier

    /**
     * Statement: ("const" | "let") Identifier "=" Expression ";";
     */
    fun visitStatement1(
            a1: Union2<Token, Token>,
            a2: Token,
            a3: Token,
            a4: T_Expression,
            a5: Token): T_Statement

    /**
     * Statement: "if" Expression Statement ["else" Statement];
     */
    fun visitStatement2(
            a1: Token,
            a2: T_Expression,
            a3: T_Statement,
            a4: Tuple2<Token, T_Statement>?): T_Statement

    /**
     * Statement: "while" Expression Statement;
     */
    fun visitStatement3(
            a1: Token,
            a2: T_Expression,
            a3: T_Statement): T_Statement

    /**
     * Statement: "{" {Statement} "}";
     */
    fun visitStatement4(
            a1: Token,
            a2: List<T_Statement>,
            a3: Token): T_Statement

    /**
     * Statement: Identifier ("{" [Expression {"," Expression}] "}" | "=' Expression) ";";
     */
    fun visitStatement5(
            a1: Token,
            a2: Union2<Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>, Tuple2<Token, T_Expression>>,
            a3: Token): T_Statement

    /**
     * Statement: ";";
     */
    fun visitStatement6(a: Token): T_Statement

    /**
     * Expression: OrExpression ["?" Expression ":" Expression];
     */
    fun visitExpression(
            a1: T_OrExpression,
            a2: Tuple4<Token, T_Expression, Token, T_Expression>?): T_Expression

    /**
     * OrExpression: AndExpression {"||" AndExpression};
     */
    fun visitOrExpression(
            a1: T_AndExpression,
            a2: List<Tuple2<Token, T_AndExpression>>): T_OrExpression

    /**
     * AndExpression: RelationalExpression {"&&" RelationalExpression};
     */
    fun visitAndExpression(
            a1: T_RelationalExpression,
            a2: List<Tuple2<Token, T_RelationalExpression>>): T_AndExpression

    /**
     * RelationalExpression: AdditiveExpression [RelationalOp AdditiveExpression];
     */
    fun visitRelationalExpression(
            a1: T_AdditiveExpression,
            a2: Tuple2<T_RelationalOp, T_AdditiveExpression>?): T_RelationalExpression

    /**
     * RelationalOp: "==";
     */
    fun visitRelationalOp1(a: Token): T_RelationalOp

    /**
     * RelationalOp: "!=";
     */
    fun visitRelationalOp2(a: Token): T_RelationalOp

    /**
     * RelationalOp: "<=";
     */
    fun visitRelationalOp3(a: Token): T_RelationalOp

    /**
     * RelationalOp: "<";
     */
    fun visitRelationalOp4(a: Token): T_RelationalOp

    /**
     * RelationalOp: ">=";
     */
    fun visitRelationalOp5(a: Token): T_RelationalOp

    /**
     * RelationalOp: ">";
     */
    fun visitRelationalOp6(a: Token): T_RelationalOp


    /**
     * AdditiveExpression: MultiplicativeExpression {AdditiveOp MultiplicativeExpression};
     */
    fun visitAdditiveExpression(
            a1: T_MultiplicativeExpression,
            a2: List<Tuple2<T_AdditiveOp, T_MultiplicativeExpression>>): T_AdditiveExpression


    /**
     * AdditiveOp: "+";
     */
    fun visitAdditiveOp1(a: Token): T_AdditiveOp

    /**
     * AdditiveOp: "-";
     */
    fun visitAdditiveOp2(a: Token): T_AdditiveOp


    /**
     * MultiplicativeExpression: Factor {MultiplicativeOp Factor};
     */
    fun visitMultiplicativeExpression(
            a1: T_Factor,
            a2: List<Tuple2<T_MultiplicativeOp, T_Factor>>): T_MultiplicativeExpression


    /**
     * MultiplicativeOp: "*";
     */
    fun visitMultiplicativeOp1(a: Token): T_MultiplicativeOp

    /**
     * MultiplicativeOp: "/";
     */
    fun visitMultiplicativeOp2(a: Token): T_MultiplicativeOp


    /**
     * Factor: LiteralInt
     */
    fun visitFactor1(a: Token): T_Factor

    /**
     * Factor: LiteralFloat
     */
    fun visitFactor2(a: Token): T_Factor

    /**
     * Factor: LiteralString
     */
    fun visitFactor3(a: Token): T_Factor

    /**
     * Factor: "true"
     */
    fun visitFactor4(a: Token): T_Factor

    /**
     * Factor: "false"
     */
    fun visitFactor5(a: Token): T_Factor

    /**
     * Factor: UnaryOp Factor
     */
    fun visitFactor6(
            a1: T_UnaryOp,
            a2: T_Factor): T_Factor

    /**
     * Factor: "(" Expression ")"
     */
    fun visitFactor7(
            a1: Token,
            a2: T_Expression,
            a3: Token): T_Factor

    /**
     * Factor: Identifier ["(" [Expression {"," Expression}] ")"];
     */
    fun visitFactor8(
            a1: Token,
            a2: Tuple3<Token, Tuple2<T_Expression, List<Tuple2<Token, T_Expression>>>?, Token>?): T_Factor


    /**
     * UnaryOp: "!";
     */
    fun visitUnaryOp1(a: Token): T_UnaryOp

    /**
     * UnaryOp: "-";
     */
    fun visitUnaryOp2(a: Token): T_UnaryOp

    /**
     * UnaryOp: "+";
     */
    fun visitUnaryOp3(a: Token): T_UnaryOp
}
