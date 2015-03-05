package com.tomtresansky.gradle.plugin.schemaspy

import org.gradle.api.Project
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Nested

import com.google.common.base.Preconditions

/**
 * Extension to hold {@link SchemaSpyPlugin}'s properties.
 * 
 * @author Tom
 */
class SchemaSpyExtension {
  private final Project project
  
  /**
   * The version of SchemaSpy to be used.
   */
  String toolVersion
  
  /**
   * The directory where reports will be generated.
   */
  File reportsDir

  /**
   * The SchemaSpy configuration to use.
   */
  @Nested
  TextResource config

  /**
   * Constructor stores reference to project.
   * 
   * @param project project reference to store (<em>may <strong>NOT</strong> be <code>null</code></em>)
   */
  SchemaSpyExtension(Project project) {
    Preconditions.checkNotNull(project, 'project can NOT be null!')

    this.project = project
  }
  
  /**
   * Getter for the format type of the SchemaSpy report.
   *
   * @return always returns <tt>html</tt>
   */
  String getReportFormat() {
    return 'html'
  }

  /**
   * Getter for the SchemaSpy configuration file to use.
   *
   * @return File referencing the file specified by <code>config</code> property; or <code>null</code> if property not set
   */
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
}
