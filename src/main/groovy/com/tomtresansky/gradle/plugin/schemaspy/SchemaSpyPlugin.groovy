package com.tomtresansky.gradle.plugin.schemaspy

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskExecutionException

import com.google.common.base.Preconditions

/**
 * A {@link Plugin} which allows for running SchemaSpy.
 *
 * <p>This plugin adds the following extension objects to the project:</p>
 * <ul>
 * <li>{@link SchemaSpyExtension}</li>
 * </ul>
 * <p>
 * Just like {@link SchemaSpyExtension} this class can <strong>NOT</strong> be <code>final</code>.
 *
 * @author Tom
 */
class SchemaSpyPlugin extends ReportingBasePlugin {
  private static final String SCHEMA_SPY_TOOL_NAME = 'SchemaSpy'
  private static final String SCHEMA_SPY_TASK_NAME = 'schemaSpy'
  private static final String SCHEMA_SPY_REPORT_GROUP = 'db'

  private Project project
  private SchemaSpyExtension extension

  String getToolName() {
    return SCHEMA_SPY_TOOL_NAME
  }

  String getConfigurationName() {
    return toolName.toLowerCase()
  }

  String getExtensionName() {
    return toolName.toLowerCase()
  }

  String getReportName() {
    return toolName.toLowerCase()
  }

  String getReportGroupName() {
    return SCHEMA_SPY_REPORT_GROUP.toLowerCase()
  }

  void apply(Project project) {
    Preconditions.checkNotNull(project, 'project can NOT be null!')

    this.project = project

    project.plugins.apply(ReportingBasePlugin)

    createExtension()
    createConfiguration()
    createTask()
  }

  /**
   * Sets up the project properties extension script block for SchemaSpy; with some default property values.
   *
   * @return the new project extension
   */
  private SchemaSpyExtension createExtension() {
    String relPath = "config/${extensionName}/schemaspy.properties"

    File schemaSpyPropsFile = project.file(relPath)
    if (!schemaSpyPropsFile.exists()) {
      schemaSpyPropsFile = project.rootProject.file(relPath)
    }

    extension = project.extensions.create(extensionName, SchemaSpyExtension, project)
    extension.with {
      toolVersion = "5.0.0"
      config = project.resources.text.fromFile(schemaSpyPropsFile)
      reportsDir = project.extensions.getByType(ReportingExtension).file("${reportGroupName}/${reportName}")
    }

    return extension
  }

  /**
   * Create the 'schemaspy' configuration; add the SchemaSpy dependency to it.
   * <p>
   * Requires the SchemaSpy extension to be setup prior to call, see {@link #createExtension()}.
   *
   * @return the new configuration
   */
  private Configuration createConfiguration() {
    project.configurations.create(configurationName).with {
      transitive = true
      description = "The ${toolName} libraries to be used for this project."
    }

    def schemaspyConfiguration = project.configurations[configurationName]
    schemaspyConfiguration.dependencies.add(project.dependencies.create("org.jumpmind.symmetric.schemaspy:schemaspy:${extension.toolVersion}"))

    return schemaspyConfiguration
  }

  /**
   * Creates the task used to run SchemaSpy.
   * <p>
   * This task expects the SchemaSpy configuration and extension to be properly setup prior to calling.  See {@link #createExtension()} and {@link #createConfiguration()}.
   *
   * @return the new task
   */
  private SchemaSpyReportTask createTask() {
    final SchemaSpyReportTask schemaSpyTask = project.getTasks().create(SCHEMA_SPY_TASK_NAME, SchemaSpyReportTask.class)

    schemaSpyTask.setDescription("Runs SchemaSpy against the specified db.")
    schemaSpyTask.setGroup("reporting")

    schemaSpyTask.mainClass.set('net.sourceforge.schemaspy.Main')
    schemaSpyTask.setClasspath(project.configurations.schemaspy)

    schemaSpyTask.conventionMapping.with {
      config = { extension.config }
    }

    def outputDir = new File(extension.reportsDir.absolutePath)
    schemaSpyTask.reports.all { report ->
      report.conventionMapping.with {
        required = true
        destination = {
          outputDir
        }
      }
    }
    schemaSpyTask.outputs.dir(outputDir)

    // Pull SchemaSpy command line args into the call to be made to java
    schemaSpyTask.doFirst {
      logging.captureStandardOutput(LogLevel.INFO)
      logging.captureStandardError(LogLevel.ERROR)

      File schemaSpyPropsFile = project.file(getConfigFile())
      if (!schemaSpyPropsFile.exists()) {
        throw new TaskExecutionException(schemaSpyTask, new RuntimeException("schemaspy.properties file at ${schemaSpyPropsFile.absolutePath} does NOT exist!"))
      }
      if (!schemaSpyPropsFile.canRead()) {
        throw new TaskExecutionException(schemaSpyTask, new RuntimeException("schemaspy.properties file at ${schemaSpyPropsFile.absolutePath} can NOT be read!"))
      }

      List schemaSpyArgsList = []

      def schemaSpyProps = new Properties()
      schemaSpyPropsFile.withInputStream { stream ->
        schemaSpyProps.load(stream)
      }
      schemaSpyProps.each { String key, String value ->
        schemaSpyArgsList << "-$key" << value
      }

      schemaSpyTask.setArgs(schemaSpyArgsList)

      println "Running SchemaSpy with arguments: $schemaSpyArgsList"
    }

    return schemaSpyTask
  }
}
