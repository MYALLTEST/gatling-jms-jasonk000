package com.bluedevel.gatling.jms

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.ProtocolConfigurationRegistry
import akka.actor._

/**
 * JmsAttributes carries around the JMS settings
 */
case class JmsAttributes(
  requestName: String,
  queueName: String,
  textMessage: String)

/**
 * Builds a request reply JMS
 */
object JmsReqReplyBuilder {
  def apply(requestName: String) = new JmsReqReplyBuilder(JmsAttributes(
      requestName = requestName,
      queueName = "?", 
      textMessage = "?"))
}

/**
 * Builds a JMS request reply 
 */
class JmsReqReplyBuilder(val attributes: JmsAttributes) extends ActionBuilder {
  val system = akka.actor.ActorSystem("system")

  def queue(q: String) = new JmsReqReplyBuilder(attributes.copy(queueName = q))
  def textMessage(text: String) = new JmsReqReplyBuilder(attributes.copy(textMessage = text))

  /**
   * Builds an action instance
   */
  def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) = {
    val jmsProtocol = protocolConfigurationRegistry.getProtocolConfiguration(JmsProtocol.default)
    system.actorOf(Props(new JmsReqReplyAction(next, attributes, jmsProtocol)))
  }
}


