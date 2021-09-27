package com.tomtresansky.gradle.plugin.schemaspy

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

class SchemaSpyReportTaskFuncTest extends Specification {
    @TempDir
    File testProjectDir
    File buildFile

    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.tomtresansky.gradle.plugin.schemaspy'
            }
        """
    }

    def "default task is created"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('tasks')
                .withPluginClasspath()
                .build()

        then:
        def taskOutput = "${SchemaSpyPlugin.SCHEMA_SPY_TASK_NAME} - ${SchemaSpyPlugin.SCHEMA_SPY_TASK_DESCRIPTION}"
        assert result.output.contains(taskOutput)
    }

    def "new tasks can be created"() {
        buildFile << """
            def ss = tasks.create('mySchemaSpyTask', com.tomtresansky.gradle.plugin.schemaspy.SchemaSpyReportTask)
            ss.description = 'mySchemaSpyTask - My custom SchemaSpyTask'
            ss.group = 'Verification'
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('tasks')
                .withPluginClasspath()
                .build()

        then:
        assert result.output.contains('mySchemaSpyTask - My custom SchemaSpyTask')
    }

    // TODO: new/existing tasks can be configured
}
