package com.bluedevel.gatling.jms

import io.gatling.core.Predef._
import io.gatling.core.action.Chainable
import akka.actor.{ ActorRef, Actor, Props }
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.util.TimeHelper.nowMillis
import io.gatling.core.result.message.{RequestMessage, OK}
import java.util.{Hashtable => JHashtable}
import javax.naming._
import javax.jms._

/**
 * Core JMS Action to handle Request-Reply semantics
 */
class JmsReqReplyAction(val next : ActorRef, val attributes: JmsAttributes, 
    val protocol: JmsProtocol, val tracker: ActorRef) extends Chainable {

  // define a single response handler; to be used by SimpleJmsClient
  val responseHandler = new MessageListener { 
    def onMessage(m: Message) = m match {
      case tm : TextMessage => tracker ! MessageReceived(tm.getJMSCorrelationID, nowMillis)
      case _ => println("received something else ??")
    }
  }

  /**
   * Create a client to refer to
   */
  val client = new SimpleJmsClient(protocol.connectionFactoryName,
    attributes.queueName,
    protocol.jmsUrl,
    protocol.username, 
    protocol.password, 
    protocol.contextFactory,
    responseHandler)

  /**
   * Framework calls the execute() method to send a single request
   */
  def execute(session: io.gatling.core.Predef.Session) {
    // send the message
    val start = nowMillis
    val msgid = client.sendTextMessage(attributes.textMessage)
    val end = nowMillis
    // notify the tracker that a message was sent
    tracker ! MessageSent(msgid, start, end, session.scenarioName, session.userId)
    // go to next in chain
    next ! session
  }
}
