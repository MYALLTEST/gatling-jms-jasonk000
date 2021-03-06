package com.bluedevel.gatling.jms

import io.gatling.core.config.Protocol

/**
 * Wraps a JMS protocol configuration
 */
object JmsProtocol {
  val default = JmsProtocol(
    contextFactory = None,
    connectionFactoryName = None,
    jmsUrl = None,
    username = None,
    password = None,
    listenerCount = 1,
    deliveryMode = javax.jms.DeliveryMode.NON_PERSISTENT)
}

/**
 * Wraps a JMS protocol configuration
 */ 
case class JmsProtocol (
  contextFactory: Option[String],
  connectionFactoryName: Option[String],
  jmsUrl: Option[String],
  username: Option[String],
  password: Option[String],
  listenerCount: Int,
  deliveryMode: Int
) extends Protocol

