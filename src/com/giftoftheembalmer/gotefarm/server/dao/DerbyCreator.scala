package com.giftoftheembalmer.gotefarm.server.dao

class DerbyCreator {
  @scala.reflect.BeanProperty
  private var jdbcUrl: String = _

  def initialize() = {
    // load the derby driver
    val driver = new org.apache.derby.jdbc.EmbeddedDriver

    // open a connection to the datasource, causing derby to create the
    // database if necessary
    val conn = driver.connect(jdbcUrl, null)
    try {
      // suppress warnings
      conn.clearWarnings()
    }
    finally {
      conn.close()
    }
  }
}
