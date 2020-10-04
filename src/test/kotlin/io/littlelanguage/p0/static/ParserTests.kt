package io.littlelanguage.p0.static

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.FunSpecDsl
import io.kotest.matchers.shouldBe
import io.littlelanguage.data.Either
import io.littlelanguage.data.Right
import io.littlelanguage.p0.Errors
import io.littlelanguage.p0.lexer.LA
import org.yaml.snakeyaml.Yaml
import java.io.StringReader

private val yaml = Yaml()


class ParserTests : FunSpec({
    context("Conformance Tests") {
//        val content = File("./../overview/docs/p0/conformance/parser.yaml").readText()
        val content = khttp.get("https://little-languages.gitlab.io/overview/p0/conformance/parser.yaml").text

        val scenarios: Any = yaml.load(content)

        if (scenarios is List<*>) {
            conformanceTest(this, scenarios)
        }
    }
})


fun parse(input: String): Either<Errors, io.littlelanguage.p0.static.ast.Program> =
        parse(LA(StringReader(input)))


suspend fun conformanceTest(ctx: FunSpecDsl.ContextScope, scenarios: List<*>) {
    scenarios.forEach { scenario ->
        val s = scenario as Map<*, *>

        val nestedScenario = s["scenario"] as Map<*, *>?
        if (nestedScenario == null) {
            val name = s["name"] as String
            val input = s["input"] as String
            val output = s["output"]

            ctx.test(name) {
                val lhs =
                        parse(input).map { it.yaml() }.toString()

                val rhs =
                        Right<Errors, Any>(output as Any).toString()

                lhs shouldBe rhs
            }
        } else {
            val name = nestedScenario["name"] as String
            val tests = nestedScenario["tests"] as List<*>
            ctx.context(name) {
                conformanceTest(this, tests)
            }
        }
    }
}
