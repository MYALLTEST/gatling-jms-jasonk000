package com.bluedevel.gatling.jms

import akka.actor.{ ActorRef, Actor, Props }
import scala.collection.mutable.HashMap
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.{RequestMessage, OK, KO}
import javax.jms.Message
import io.gatling.core.Predef.Session

case class MessageSent(correlationId: String, startSend: Long, endSend: Long, 
  scenarioName: String, userId: Int, checks: List[JmsCheck], session: Session, next: ActorRef)
case class MessageReceived(correlationId: String, received: Long, message: Message)

/**
 * Bookkeeping actor to correlate request and response JMS messages
 * Once a message is correlated, it publishes to the Gatling core DataWriter
 */
class JmsRequestTrackerActor extends Actor {
  
  // messages to be tracked through this HashMap - note it is a mutable hashmap
  val messages = new HashMap[String, (Long, Long, String, Int, List[JmsCheck], Session, ActorRef)]()

  // Actor receive loop
  def receive = {

    // message was sent; add the timestamps to the map
    case MessageSent(corrId, startSend, endSend, scenarioName, userId, checks, session, next) => {
      messages += corrId -> (startSend, endSend, scenarioName, userId, checks, session, next)
    }

    // message was received; publish to the datawriter and remove from the hashmap
    case MessageReceived(corrId, received, message) => {
      messages.get(corrId) match {
        case Some((startSend, endSend, scenarioName, userId, checks, session, next)) => {
          val checksPassed = checks.forall((check: JmsCheck) => check(message))
          val gatling_response = if (checksPassed) OK else KO
          DataWriter.tell(RequestMessage(scenarioName, userId, Nil, "test", 
            startSend, received, endSend, received, gatling_response, None, Nil))
          next ! session
        }
        case None => println("failed to find message; early receive? or bad correlation id? => " + corrId)
      }
      messages -= corrId
    }
  }
}
