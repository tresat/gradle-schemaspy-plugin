# gradle-schemaspy-plugin
A plugin for Gradle to allow for running SchemaSpy as a part of the buildDashboard plugin.

## Purpose
This Plugin allows for running [SchemaSpy](http://schemaspy.sourceforge.net/) against a database as part of a Gradle build.  SchemaSpy's output will be accessible via Gradle's [BuildDashboard Plugin](https://docs.gradle.org/current/userguide/buildDashboard_plugin.html).

## Instructions
Add a file named `schemaspy.properties` in the `config\schemaspy` directory under your Gradle project's root.  That file should contain key=value pairs for all required SchemaSpy arguments as detailed in the [Running SchemaSpy section of the SchemaSpy documentation](http://schemaspy.sourceforge.net/).

For example, for a [Postgre database](http://www.postgresql.org/), your `schemaspy.properties` file should look something like this:

```
t=pgsql
db=TomsDB
s=public
u=UserForTomsDB
p=SecretPassword
dp=C:/Users/Tom/.gradle/caches/modules-2/files-2.1/postgresql/postgresql/9.1-901-1.jdbc4/9bfabe48876ec38f6cbaa6931bad05c64a9ea942/postgresql-9.1-901-1.jdbc4.jar
host=localhost
port=5432 
o=C:/Users/Tom/Programming/Projects/TestGradleSchemaspyPlugin/build/reports/db/schemaspy
```

You can use Gradle to substitute into the file the path to the downloaded jar of the dependency pretty easily using the `configurations.derby.asPath` value with Gradle's filter method:

```
filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: ['driver_path_token':'path_to_driver'])
```

In your `build.gradle` file, pass the `schemaspy` configuration dependency on the database driver for the database on which you wish to report.

For example, for a [Postgre database](http://www.postgresql.org/), your `build.gradle` should look something like this:

```groovy
configurations {
  postgres
}

dependencies {
  postgres(group: 'postgresql', name: 'postgresql', version: '9.1-901-1.jdbc4')
  
  configurations.schemaspy.dependencies.addAll(configurations.postgres.dependencies)
}
```

And then to link to SchemaSpy's output (and any other Reporting tasks you're interested in) via the generated Build Dashboard:

```groovy
gradle.taskGraph.whenReady { taskGraph ->
  // Check to add Reporting tasks to the Build Dashboard
  if (taskGraph.allTasks.any { Task t -> t instanceof GenerateBuildDashboard }) {
    List<Reporting> reportsToAggregrate = []
	
    // Add Schema Spy to build Dashboard (if it has been run)
    def schemaSpyTasks = taskGraph.allTasks.findAll { Task t -> t instanceof SchemaSpyReportTask }
    reportsToAggregrate.addAll(schemaSpyTasks)
    
    // ... aggregate any other Reporting tasks to the Dashboard here
    
    // Aggregate all aggregatable reports to be run to Dashboard
    if (reportsToAggregrate) {
      buildDashboard {
        aggregate(reportsToAggregrate as Reporting[])
      }
    }
  }
}

// Define ordering of tasks to ensure reports are generated prior to being accumulated by build dashboard
buildDashboard.mustRunAfter schemaSpy
```

## Release Notes

### Version 0.3
Defines report output directory.  This allows for clients to add a block like this:

```groovy
buildDashboard {
  inputs.files schemaSpy.outputs.files
}
```

in order to cause the SchemaSpy reporting plugin to cause the buildDashboard task to go out-of-date.

## Extras
Included in the project is a `net.sourceforge.schemaspy.SchemaAnalyzer` implementation for [Apache Derby](http://db.apache.org/derby/) running in file system mode (no support for in-memory databases currently).

One cavaet for running this plugin against a Derby DB on Windows is the need to quadrupal-escape any path separator characters in the Schemaspy db argument within the `schemaspy.properties` file.

```
dp=C:\\\\Users\\\\Tom\\\\Programming\\\\Projects\\\\TestDerbyProject\\\\testderbydb
```
