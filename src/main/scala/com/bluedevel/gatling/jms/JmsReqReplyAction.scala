package com.bluedevel.gatling.jms

import io.gatling.core.Predef._
import io.gatling.core.action.Chainable
import akka.actor.ActorRef
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.util.TimeHelper.nowMillis
import io.gatling.core.result.message.{RequestMessage, OK}
import java.util.{Hashtable => JHashtable}
import javax.naming._
import javax.jms._

class JmsReqReplyAction(val next : ActorRef, val attributes: JmsAttributes, val protocol: JmsProtocol) extends Chainable {
  val client = new SimpleJmsClient(protocol.connectionFactoryName,
    attributes.queueName,
    protocol.jmsUrl,
    protocol.username, 
    protocol.password, 
    protocol.contextFactory)

  def execute(session: io.gatling.core.Predef.Session) {
    val start = nowMillis
    client.sendTextMessage(attributes.textMessage)
    val end = nowMillis
    DataWriter.tell(RequestMessage(session.scenarioName, session.userId, Nil, "test",
      start, end, start, end, OK, None, Nil))
    next ! session
  }
}

class SimpleJmsClient(val qcfName: String, val queueName: String, val url: String,
    val username: Option[String], val password: Option[String], val contextFactory: String) {

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

  val consumer = session.createConsumer(replyQ)

  def sendTextMessage(messageText : String) {
    val message = session.createTextMessage(messageText)
    sendMessage(message)
  }

  def sendMessage(message : Message) {
    try {

      message.setJMSReplyTo(replyQ)
      producer.send(message)

      val response = consumer.receive()
      response match {
        case tm : TextMessage => /* do nothing println("response: " + tm.getText) */
        case null => /* do nothing */
        case _ => println("received something else ??")
      }

    } catch {
      case ne : NamingException =>
        ne.printStackTrace(System.err)
        System.exit(0)
      case jmse : JMSException =>
        jmse.printStackTrace(System.err)
        System.exit(0)
      case e : Exception =>
        println("Got other/unexpected exception")
        e.printStackTrace(System.err)
        System.exit(0)
    }
  }

}

