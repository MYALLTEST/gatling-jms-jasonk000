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
  bytesMessage: Array[Byte],
  mapMessage: Map[String, Object],
  objectMessage: java.io.Serializable,
  messageProperties: Map[String, Object],
  messageType: JmsMessageClass.JmsMessageClass,
  checks: List[JmsCheck])

object JmsMessageClass extends Enumeration {
  type JmsMessageClass = Value
  val BytesJmsMessage, MapJmsMessage, ObjectJmsMessage, TextJmsMessage = Value
}

/**
 * Builds a request reply JMS
 */
object JmsReqReplyBuilder {
  def apply(requestName: String) = new JmsReqReplyBuilder(JmsAttributes(
      requestName = requestName,
      queueName = "", 
      textMessage = "",
      bytesMessage = new Array[Byte](0),
      objectMessage = null,
      mapMessage = new ListMap[String, Object],
      messageType = JmsMessageClass.TextJmsMessage,
      messageProperties = new ListMap[String, Object],
      checks = Nil))
}

/**
 * Builds a JMS request reply 
 */
class JmsReqReplyBuilder(val attributes: JmsAttributes) extends ActionBuilder {
  val system = akka.actor.ActorSystem("system")

  import JmsMessageClass._

  // set queue name
  def queue(q: String) = new JmsReqReplyBuilder(attributes.copy(queueName = q))

  // various supported message types
  // note that StreamMessage is not presently supported; would need a bit of work to make a simple API
  def textMessage(text: String) = new JmsReqReplyBuilder(attributes.copy(textMessage = text, messageType = TextJmsMessage))
  def bytesMessage(bytes: Array[Byte]) = new JmsReqReplyBuilder(attributes.copy(bytesMessage = bytes, messageType = BytesJmsMessage))
  def mapMessage(map: Map[String, Object]) = new JmsReqReplyBuilder(attributes.copy(mapMessage = map, messageType = MapJmsMessage))
  def objectMessage(o: java.io.Serializable) = new JmsReqReplyBuilder(attributes.copy(objectMessage = o, messageType = ObjectJmsMessage))

  // add jms message properties
  def addProperty(key: String, value: Object) = 
    new JmsReqReplyBuilder(attributes.copy(messageProperties = attributes.messageProperties + ((key, value))))

  // checks that will be run on the response events
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
