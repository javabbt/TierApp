import groovy.util.Node
import groovy.xml.MarkupBuilder
import groovy.xml.XmlParser
import java.io.StringWriter

/**
 * This task  should be run after a lint tasks
 * It converts the lint XML report to a JUnit report
 *
 * Output files are located at `build/junit-reports`
 */
val convertLintResultToJUnit: Task by tasks.creating {
    this.group = "Verification"
    this.description = "Convert Lint XML report to XML Junit Report"
    this.setMustRunAfter(mutableListOf("lint"))
    this.doLast {
        val reportDirectory = File(project.buildDir, "reports")
        val jUnitReportDirectory = File(rootDir, "junit-reports").apply {
            if(!this.exists()) this.mkdir()
        }
        val reports = reportDirectory.list { _, s ->
            s.endsWith(".xml") && !s.startsWith("junit")
        }
        if (reports.isNullOrEmpty()) {
            println("No reports found")
            return@doLast
        }
        reports.forEach {
            val issues = XmlParser().parse(File(reportDirectory, it).also { file ->
                println("Source file : ${file.path}")
            })
            val issuesCount = issues.children().size
            println("Found $issuesCount issue(s) in report $it")
            val xmlWriter = StringWriter()
            val xml = MarkupBuilder(xmlWriter)
            xml.doubleQuotes = true
            xml.mkp.xmlDeclaration(
                    mapOf(
                            "version" to "1.0",
                            "encoding" to "utf-8"
                    )
            )
            xml.withGroovyBuilder {
                "testsuites"(
                        "name" to "lint-result",
                        "tests" to "$issuesCount",
                        "failure" to "$issuesCount"
                ) {
                    var i = 1
                    issues.children().map { child -> child as Node }.forEach { issue ->
                        "testsuite"("id" to "lint-$i") {
                            val location = issue.children().firstOrNull() as? Node
                            val file = location?.attribute("file")
                            val line = location?.attribute("line")
                            val column = location?.attribute("column")
                            "testcase"(
                                    "id" to "lint-${i}",
                                    "name" to "${issue.attribute("summary")} at $file:$line:$column"
                            ) {
                                "failure"(
                                        "message" to "${issue.attribute("message")}",
                                        "type" to "${issue.attribute("severity")}"
                                ) {
                                    "${issue.attribute("message")}"
                                }
                            }
                        }
                        i++
                    }
                }
            }
            println("Report parsed : $it ")
            val output = File(jUnitReportDirectory, "${project.name}-junit-$it")
            output.writeText(xmlWriter.toString(), Charsets.UTF_8)

            println("Wrote JUnit report to file://${output.absolutePath}")
        }
    }
}
