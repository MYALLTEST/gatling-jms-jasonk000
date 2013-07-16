package com.bluedevel.gatling.jms

import akka.actor.{ ActorRef, Actor, Props }
import scala.collection.mutable.HashMap
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.{RequestMessage, OK, KO}
import javax.jms.Message
import io.gatling.core.Predef.Session


/**
 * Advise actor a message was sent to JMS provider
 */
case class MessageSent(correlationId: String, startSend: Long, endSend: Long, 
  checks: List[JmsCheck], session: Session, next: ActorRef)

/**
 * Advise actor a response message was received from JMS provider
 */
case class MessageReceived(correlationId: String, received: Long, message: Message)

/**
 * Bookkeeping actor to correlate request and response JMS messages
 * Once a message is correlated, it publishes to the Gatling core DataWriter
 */
class JmsRequestTrackerActor extends Actor {
  
  // messages to be tracked through this HashMap - note it is a mutable hashmap
  val sentMessages = new HashMap[String, (Long, Long, List[JmsCheck], Session, ActorRef)]()
  val receivedMessages = new HashMap[String, (Long, Message)]()

  // Actor receive loop
  def receive = {

    // message was sent; add the timestamps to the map
    case MessageSent(corrId, startSend, endSend, checks, session, next) => {
      receivedMessages.get(corrId) match { 
        case Some((received, message)) => {
          // message was received out of order, lets just deal with it
          processMessage(session, startSend, received, endSend, checks, message, next)
          receivedMessages -= corrId
        }
        case None => {
          // normal path
          sentMessages += corrId -> (startSend, endSend, checks, session, next)
        }
      }
    }

    // message was received; publish to the datawriter and remove from the hashmap
    case MessageReceived(corrId, received, message) => {
      sentMessages.get(corrId) match {
        case Some((startSend, endSend, checks, session, next)) => {
          processMessage(session, startSend, received, endSend, checks, message, next)
          sentMessages -= corrId
        }
        case None => {
          // failed to find message; early receive? or bad return correlation id?
          // let's add it to the received messages buffer just in case
          receivedMessages += corrId -> (received, message)
        }
      }
    }
  }

  /**
   * Processes a matched message
   */
  def processMessage(session: Session, startSend: Long, received: Long, endSend: Long,
      checks: List[JmsCheck], message: Message, next: ActorRef) = {

    // run all of the checks
    val checksPassed = checks.forall((check: JmsCheck) => check(message))
    val gatling_response = if (checksPassed) OK else KO

    // advise the Gatling API that it is complete and move to next
    DataWriter.tell(RequestMessage(session.scenarioName, session.userId, Nil, "x",
      startSend, received, endSend, received, gatling_response, None, Nil))
    next ! session
  }

}
