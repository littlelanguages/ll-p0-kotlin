package io.littlelanguage.p0.dynamic

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.FunSpecDsl
import io.kotest.matchers.shouldBe
import io.littlelanguage.data.Either
import io.littlelanguage.data.Left
import io.littlelanguage.data.Right
import io.littlelanguage.p0.Errors
import io.littlelanguage.p0.dynamic.tst.Program
import io.littlelanguage.p0.lexer.LA
import org.yaml.snakeyaml.Yaml
import java.io.StringReader

private val yaml = Yaml()


class DynamicTests : FunSpec({
    context("Conformance Tests") {
//        val content = File("./dynamic.yaml").readText()
//        val content = File("./../overview/docs/p0/conformance/dynamic.yaml").readText()
        val content = khttp.get("https://little-languages.gitlab.io/overview/p0/conformance/dynamic.yaml").text

        val scenarios: Any = yaml.load(content)

        if (scenarios is List<*>) {
            conformanceTest(this, scenarios)
        }
    }
})


fun parse(input: String): Either<List<Errors>, Program> =
        io.littlelanguage.p0.static.parse(LA(StringReader(input)))
                .mapLeft { listOf(it) }
                .andThen { translate(it) }

suspend fun conformanceTest(ctx: FunSpecDsl.ContextScope, scenarios: List<*>) {
    scenarios.forEach { scenario ->
        val s = scenario as Map<*, *>

        val nestedScenario = s["scenario"] as Map<*, *>?
        if (nestedScenario == null) {
            val name = s["name"] as String
            val input = s["input"] as String
            val output = s["output"]

            ctx.test(fixName(name)) {
                val parseResult =
                        parse(input)

                val lhs =
                        when (parseResult) {
                            is Left ->
                                parseResult.left.map { it.yaml() }.toString()

                            is Right ->
                                parseResult.right.yaml().toString()
                        }

                val rhs =
                        (output as Any).toString()

                lhs shouldBe rhs
            }
        } else {
            val name = nestedScenario["name"] as String
            val tests = nestedScenario["tests"] as List<*>
            ctx.context(fixName(name)) {
                conformanceTest(this, tests)
            }
        }
    }
}

fun fixName(n: String): String =
        if (n.startsWith("!")) " $n" else n
