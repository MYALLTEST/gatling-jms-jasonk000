package com.bluedevel.gatling.jms

import akka.actor.{ ActorRef, Actor, Props }
import scala.collection.mutable.HashMap
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.{RequestMessage, OK}

class JmsRequestTrackerActor extends Actor {
  
  val messages = new HashMap[String, (Long, Long, String, Int)]()

  def receive = {
    case MessageSent(corrId, startSend, endSend, scenarioName, userId) => {
      messages += corrId -> (startSend, endSend, scenarioName, userId)
    }
    case MessageReceived(corrId, received) => {
      messages.get(corrId) match {
        case Some((startSend, endSend, scenarioName, userId)) => {
          DataWriter.tell(RequestMessage(scenarioName, userId, Nil, "test", 
            startSend, received, endSend, received, OK, None, Nil))
        }
        case None => println("failed to find message")
      }
      messages -= corrId
    }
  }
}

case class MessageSent(correlationId: String, startSend: Long, endSend: Long, scenarioName: String, userId: Int)
case class MessageReceived(correlationId: String, received: Long)
