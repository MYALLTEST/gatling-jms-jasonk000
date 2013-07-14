package com.bluedevel.gatling.jms

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.ProtocolConfigurationRegistry
import akka.actor._
import scala.collection.immutable.ListMap
import javax.jms.Message

/**
 * JmsAttributes carries around the JMS settings
 */
case class JmsAttributes(
  requestName: String,
  queueName: String,
  textMessage: String,
  messageProperties: Map[String, Object],
  checks: List[JmsCheck])

/**
 * Builds a request reply JMS
 */
object JmsReqReplyBuilder {
  def apply(requestName: String) = new JmsReqReplyBuilder(JmsAttributes(
      requestName = requestName,
      queueName = "?", 
      textMessage = "?",
      messageProperties = new ListMap[String, Object],
      checks = Nil))
}

/**
 * Builds a JMS request reply 
 */
class JmsReqReplyBuilder(val attributes: JmsAttributes) extends ActionBuilder {
  val system = akka.actor.ActorSystem("system")

  def queue(q: String) = new JmsReqReplyBuilder(attributes.copy(queueName = q))
  def textMessage(text: String) = new JmsReqReplyBuilder(attributes.copy(textMessage = text))
  def addProperty(key: String, value: Object) = 
    new JmsReqReplyBuilder(attributes.copy(messageProperties = attributes.messageProperties + ((key, value))))
  def addCheck(checks: JmsCheck*) = new JmsReqReplyBuilder(attributes.copy(checks = attributes.checks ::: checks.toList))

  /**
   * Builds an action instance
   */
  def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) = {
    val jmsProtocol = protocolConfigurationRegistry.getProtocolConfiguration(JmsProtocol.default)
    val tracker = system.actorOf(Props[JmsRequestTrackerActor])
    system.actorOf(Props(new JmsReqReplyAction(next, attributes, jmsProtocol, tracker)))
  }
}
