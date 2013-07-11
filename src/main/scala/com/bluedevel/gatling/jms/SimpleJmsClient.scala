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
 * Trivial JMS client, allows sending messages and use of a MessageListener
 */
class SimpleJmsClient(val qcfName: String, val queueName: String, val url: String,
    val username: Option[String], val password: Option[String], val contextFactory: String,
    val responseHandler: MessageListener) {

  // create InitialContext
  val properties = new JHashtable[String, String]
  properties.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory)
  properties.put(Context.PROVIDER_URL, url)
  username match {
    case None => None
    case Some(s) =>  properties.put(Context.SECURITY_PRINCIPAL, s)
  }
  password match {
    case None => None
    case Some(s) => properties.put(Context.SECURITY_CREDENTIALS, s)
  }

  val ctx = new InitialContext(properties)
  println("Got InitialContext " + ctx.toString())

  // create QueueConnectionFactory
  val qcf = (ctx.lookup(qcfName)).asInstanceOf[ConnectionFactory]
  println("Got ConnectionFactory " + qcf.toString())

  // create QueueConnection
  val conn = qcf.createConnection()
  conn.start()
  val session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
  println("Got Connection " + conn.toString())

  val replyQ = session.createTemporaryQueue()

  val destination = session.createQueue(queueName)

  val producer = session.createProducer(destination)
  producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT)

  // set up the reply queue listener
  val replyConsumer = session.createConsumer(replyQ)
  replyConsumer.setMessageListener(responseHandler)


  /**
   * Wrapper to send a TextMessage, returns the message ID of the sent message
   */
  def sendTextMessage(messageText : String): String = {
    val message = session.createTextMessage(messageText)
    props.foreach {
      case (key: String, value: Object) => message.setObjectProperty(key, value)
    }
    sendMessage(message)
  }

  /**
   * Sends a JMS message, returns the message ID of the sent message
   */
  def sendMessage(message: Message): String = {
    try {

      message.setJMSReplyTo(replyQ)
      producer.send(message)
      // return the message id
      message.getJMSMessageID

    } catch {

      case e : Exception =>
        println("Got other/unexpected exception")
        e.printStackTrace(System.err)
        System.exit(0)
        "<< never get here, system exit will solve it >>"

    }
  }

}

