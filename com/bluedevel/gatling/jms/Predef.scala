package com.bluedevel.gatling.jms

object Predef {

  /**
   * DSL text to start the jms builder
   * 
   * @param requestName human readable name of request
   * @return a PingBuilder instance which can be used to build up a ping
   */
  def jms(requestName: String) = JmsBuilder.jms(requestName)

}


