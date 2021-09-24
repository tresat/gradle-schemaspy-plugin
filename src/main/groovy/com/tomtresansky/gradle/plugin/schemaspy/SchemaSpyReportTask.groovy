package com.tomtresansky.gradle.plugin.schemaspy

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile

import static org.gradle.api.logging.LogLevel.*

import javax.inject.Inject

import org.gradle.api.Task
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.reporting.Reporting
import org.gradle.api.resources.TextResource
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Nested
import org.gradle.internal.reflect.Instantiator

import com.google.common.base.Preconditions

/**
 * Generates SchemaSpy's html report which describes a database.
 * <p>
 * The report is generated in the <code>build/reports/project/db/schemaspy</code> directory by default.
 * This can also be changed by setting the <code>outputDirectory</code>
 * property.
 * <p>
 * SchemaSpy is run via a JavaExec call, to ensure everything terminates as expected instead of
 * continuing to exist and lock the db.
 *
 * @author Tom
 */
class SchemaSpyReportTask extends JavaExec implements Reporting<SchemaSpyReportContainer>{
  private final SchemaSpyReportsImpl reports

  /**
   * The SchemaSpy configuration to use.
   */
  @Nested
  TextResource config

  SchemaSpyReportTask() {
    reports = getObjectFactory().newInstance(SchemaSpyReportsImpl.class, this);

    // This task will never be considered up-to-date - who knows what might have changed the db?
    outputs.upToDateWhen(new Spec<Task>() {
              public boolean isSatisfiedBy(Task element) {
                return false
              }
            })
  }

  @Inject
  protected ObjectFactory getObjectFactory() {
    throw new UnsupportedOperationException();
  }

  /**
   * Getter for the SchemaSpy configuration file to use.
   *
   * @return File referencing the file specified by <code>config</code> property; or <code>null</code> if property not set
   */
  @InputFile
  File getConfigFile() {
    getConfig()?.asFile()
  }

  /**
   * Sets the SchemaSpy configuration file to use.
   *
   * @param configFile the config file to use (<em>may <strong>NOT</strong> be <code>null</code></em)
   */
  void setConfigFile(File configFile) {
    Preconditions.checkNotNull(configFile, 'configFile can NOT be null!')

    setConfig(project.resources.text.fromFile(configFile))
  }

  @Override
  @Nested
  SchemaSpyReportContainer getReports() {
    return reports
  }

  @Override
  SchemaSpyReportContainer reports(Closure closure) {
    Preconditions.checkNotNull(closure, 'closure can NOT be null!')

    return reports.configure(closure)
  }

  @Override
  SchemaSpyReportContainer reports(Action<? super SchemaSpyReportContainer> configureAction) {
    configureAction.execute(reports)
    return reports
  }

  @Inject
  protected Instantiator getInstantiator() {
    throw new UnsupportedOperationException()
  }

  @Inject
  IsolatedAntBuilder getAntBuilder() {
    throw new UnsupportedOperationException()
  }
}
