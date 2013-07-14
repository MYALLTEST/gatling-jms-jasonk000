package com.bluedevel.gatling.jms

import io.gatling.core.config.ProtocolConfiguration

/**
 * Wraps a JMS protocol configuration
 */
object JmsProtocol {
  val default = JmsProtocol(
    contextFactory = "?",
    connectionFactoryName = "?",
    jmsUrl = "?",
    username = None,
    password = None,
    listenerCount = 1)
}

case class JmsProtocol (
  contextFactory: String,
  connectionFactoryName: String,
  jmsUrl: String,
  username: Option[String],
  password: Option[String],
  listenerCount: Int
) extends ProtocolConfiguration 

