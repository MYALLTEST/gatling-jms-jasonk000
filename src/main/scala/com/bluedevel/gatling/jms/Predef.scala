package com.bluedevel.gatling.jms

/**
 * Imports to be used to simplify the DSL
 * Scenario scripts will import this and generally start interacting with the DSL from the functions exposed here
 */

object Predef {

  /**
   * DSL text to start the jms builder
   * 
   * @param requestName human readable name of request
   * @return a PingBuilder instance which can be used to build up a ping
   */
  def jms(requestName: String) = JmsBuilder.jms(requestName)


  /**
   * Convert a JmsProtocolBuilder to a JmsProtocol
   * Simplifies the API somewhat (you can pass the builder reference to the scenario .protocolConfig() method)
   */
  implicit def jmsProtocolBuilder2jmsProtocol(builder: JmsProtocolBuilder): JmsProtocol = builder.build
  
}


