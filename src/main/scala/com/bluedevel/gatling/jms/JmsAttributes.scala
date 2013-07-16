package com.bluedevel.gatling.jms

/**
 * JmsAttributes carries around the JMS settings.
 * <p>
 * As the JmsReqReplyBuilder is building a request from the DSL, it uses this object
 * to represent the in progress request. Once the request is built it can then be used
 * so that the JmsReqReplyAction knows exactly what message to send.
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

