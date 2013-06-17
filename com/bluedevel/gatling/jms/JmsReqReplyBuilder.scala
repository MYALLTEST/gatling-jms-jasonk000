package com.bluedevel.gatling.jms

import io.gatling.core.action.builder.ActionBuilder
import akka.actor._
import io.gatling.core.config.ProtocolConfigurationRegistry

/**
 * JmsAttributes carries around the JMS settings
 */
case class JmsAttributes(
  requestName: String,
  connFactoryName: String,
  queueName: String,
  jmsUrl: String,
  username: Option[String],
  password: Option[String],
  textMessage: String,
  contextFactory: String)

/**
 * Builds a request reply JMS
 */
object JmsReqReplyBuilder {
  def apply(requestName: String) = new JmsReqReplyBuilder(JmsAttributes(
      requestName = requestName,
      connFactoryName = "?",
      queueName = "?", 
      jmsUrl = "?", 
      username = None, 
      password = None, 
      textMessage = "?",
      contextFactory = "?"))
}

/**
 * Builds a JMS request reply 
 */
class JmsReqReplyBuilder(val attributes: JmsAttributes) extends ActionBuilder {
  val system = akka.actor.ActorSystem("system")

  def connectionFactoryName(cf: String) = new JmsReqReplyBuilder(attributes.copy(connFactoryName = cf))
  def queue(q: String) = new JmsReqReplyBuilder(attributes.copy(queueName = q))
  def url(theUrl: String) = new JmsReqReplyBuilder(attributes.copy(jmsUrl = theUrl))
  def credentials(user: String, pass: String) = new JmsReqReplyBuilder(attributes.copy(username = Some(user), password = Some(pass)))
  def textMessage(text: String) = new JmsReqReplyBuilder(attributes.copy(textMessage = text))
  def contextFactory(factory: String) = new JmsReqReplyBuilder(attributes.copy(contextFactory = factory))

  /**
   * Builds an action instance
   */
  def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) =
    system.actorOf(Props(new JmsReqReplyAction(next, attributes)))
}


