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

  /**
   * Create a client to refer to
   */
  val client = new SimpleJmsClient(protocol.connectionFactoryName,
    attributes.queueName,
    protocol.jmsUrl,
    protocol.username, 
    protocol.password, 
    protocol.contextFactory)

  // set up a consumer from the queue
  println("starting " + protocol.listenerCount + " listeners.")
  for(i <- 1 to protocol.listenerCount) {

    val thread = new Thread(new Runnable { def run = {
      val replyConsumer = client.createReplyConsumer
      while(true) {
        val m = replyConsumer.receive()
        m match {
          case msg: Message => tracker ! MessageReceived(msg.getJMSCorrelationID, nowMillis, msg)
          case _ => println("oops")
        }
      }
    }})
    thread.start

  }
  

  /**
   * Framework calls the execute() method to send a single request
   */
  def execute(session: io.gatling.core.Predef.Session) {
    import JmsMessageClass._

    // send the message
    val start = nowMillis
    val msgid = attributes.messageType match {
      case BytesJmsMessage => client.sendBytesMessage(attributes.bytesMessage, attributes.messageProperties)
      case MapJmsMessage =>  client.sendMapMessage(attributes.mapMessage, attributes.messageProperties)
      case ObjectJmsMessage => client.sendObjectMessage(attributes.objectMessage, attributes.messageProperties)
      case TextJmsMessage => client.sendTextMessage(attributes.textMessage, attributes.messageProperties)
    }
    val end = nowMillis
    // notify the tracker that a message was sent
    tracker ! MessageSent(msgid, start, end, session.scenarioName, session.userId, attributes.checks, session, next)
  }
}
