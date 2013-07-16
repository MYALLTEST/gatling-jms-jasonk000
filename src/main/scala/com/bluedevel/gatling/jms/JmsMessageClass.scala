package com.bluedevel.gatling.jms

/**
 * Provides the enumeration of JMSMessage types that the implementation supports
 */
object JmsMessageClass extends Enumeration {
  type JmsMessageClass = Value
  val BytesJmsMessage, MapJmsMessage, ObjectJmsMessage, TextJmsMessage = Value
}


