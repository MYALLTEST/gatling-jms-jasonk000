package com.bluedevel.gatling.jms

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.ProtocolRegistry
import akka.actor._
import scala.collection.immutable.ListMap
import javax.jms.Message

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
 * <p>
 * Note that StreamMessage is not presently supported - it would need a bit of cleverness to build a nice API.
 * Happy to take suggestions and pull requests.
 */
class JmsReqReplyBuilder(val attributes: JmsAttributes) extends ActionBuilder {
  val system = akka.actor.ActorSystem("system")

  import JmsMessageClass._

  /**
   * Set the queue name
   */
  def queue(q: String) = new JmsReqReplyBuilder(attributes.copy(queueName = q))

  /**
   * Send a TextMessage
   */
  def textMessage(text: String) = new JmsReqReplyBuilder(attributes.copy(textMessage = text, messageType = TextJmsMessage))

  /**
   * Send a BytesMessage
   */
  def bytesMessage(bytes: Array[Byte]) = new JmsReqReplyBuilder(attributes.copy(bytesMessage = bytes, messageType = BytesJmsMessage))
  
  /**
   * Send a MapMessage
   */
  def mapMessage(map: Map[String, Object]) = new JmsReqReplyBuilder(attributes.copy(mapMessage = map, messageType = MapJmsMessage))

  /**
   * Send an ObjectMessage
   */
  def objectMessage(o: java.io.Serializable) = new JmsReqReplyBuilder(attributes.copy(objectMessage = o, messageType = ObjectJmsMessage))

  /**
   * Add JMS message properties (aka headers) to the outbound message
   */
  def addProperty(key: String, value: Object) = 
    new JmsReqReplyBuilder(attributes.copy(messageProperties = attributes.messageProperties + ((key, value))))

  /**
   * Add a check that will be perfomed on each received JMS response message before giving Gatling on OK/KO response
   */
  def addCheck(checks: JmsCheck*) = new JmsReqReplyBuilder(attributes.copy(checks = attributes.checks ::: checks.toList))

  /**
   * Builds an action instance
   */
  def build(next: ActorRef) = {
    val jmsProtocol = ProtocolRegistry.registry.getProtocol(JmsProtocol.default)
    val tracker = system.actorOf(Props[JmsRequestTrackerActor])
    system.actorOf(Props(new JmsReqReplyAction(next, attributes, jmsProtocol, tracker)))
  }
}
