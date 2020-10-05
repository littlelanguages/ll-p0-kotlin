package io.littlelanguages.p0.lexer

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.FunSpecDsl
import io.kotest.matchers.shouldBe
import org.yaml.snakeyaml.Yaml
import java.io.StringReader

class LATests : FunSpec({
    context("Conformance Tests") {
//        val content = File("./../overview/docs/p0/conformance/lexical.yaml").readText()
        val content = khttp.get("https://little-languages.gitlab.io/overview/p0/conformance/lexical.yaml").text

        val yaml = Yaml()
        val scenarios: Any = yaml.load(content)

        if (scenarios is List<*>) {
            conformanceTest(this, scenarios)
        }
    }
})


private fun tokens(input: String): List<Token> =
        assembleTokens(LA(StringReader(input), false))


suspend fun conformanceTest(ctx: FunSpecDsl.ContextScope, scenarios: List<*>) {
    scenarios.forEach { scenario ->
        val s = scenario as Map<*, *>

        val nestedScenario = s["scenario"] as Map<*, *>?
        if (nestedScenario == null) {
            val name = s["name"] as String
            val input = s["input"] as String
            val output = s["output"] as List<*>

            ctx.test(name) {
                tokens(input).map { it.toString() } shouldBe output.map { (it as String).trim() }
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