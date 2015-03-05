package com.tomtresansky.gradle.plugin.schemaspy

import java.sql.DriverManager
import java.sql.SQLException

import net.sourceforge.schemaspy.Config
import net.sourceforge.schemaspy.SchemaAnalyzer
import net.sourceforge.schemaspy.model.Database

class DerbySchemaAnalyzer extends SchemaAnalyzer {
  @Override
  public Database analyze(Config conf) throws Exception {
    def result = super.analyze(conf)
    
    String type = conf.getDbType()
    switch (type) {
      case 'derby':
        File derbyDBDir = new File(conf.getDb())
        
        // Windows paths with backslash '\' chars don't work properly unless they are double escaped4
        String path = derbyDBDir.absolutePath.replaceAll($/\\/$, '/')
        
        // Ensure derby driver is loaded
        Class.forName('org.apache.derby.jdbc.EmbeddedDriver')
        
        try {
          // Ensure db is started
          DriverManager.getConnection("jdbc:derby:${path};")
        } catch (SQLException e) {
          if ('XJ040'.equals(e.getSQLState())) {
             // This is acceptable - DB already booted - Another instance of Derby may have already booted the database
          } else {
            throw e
          }
        }
        
        try {
          // Shutdown db
          DriverManager.getConnection("jdbc:derby:${path};shutdown=true;")
          // DB Shutdown should have thrown an exception!
          throw new RuntimeException('Derby shutdown failed!')
        } catch (SQLException e)  {
          if ('08006'.equals(e.getSQLState()) || 'XJ015'.equals(e.getSQLState())) {
            println "Derby shutdown succeeded. SQLState=${e.getSQLState()}"
          } else {
            throw e
          }
        }
        
        try {
          // Shutdown Derby
          DriverManager.getConnection("jdbc:derby:;shutdown=true;")
          // DB Shutdown should have thrown an exception!
          throw new RuntimeException('Derby shutdown failed!')
        } catch (SQLException e)  {
          if ('08006'.equals(e.getSQLState()) || 'XJ015'.equals(e.getSQLState())) {
            println "Derby shutdown succeeded. SQLState=${e.getSQLState()}"
          } else {
            throw e
          }
        }
        
        break
      default:
        throw new IllegalArgumentException("Unknown value provided for db.type = '${type}'!")
    }
    
    return result
  }
}
