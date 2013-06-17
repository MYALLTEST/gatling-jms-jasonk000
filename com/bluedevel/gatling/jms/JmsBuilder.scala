package com.bluedevel.gatling.jms

object JmsBuilder {
  /**
   * DSL entry point
   */
  def jms(requestName: String) = new JmsBuilder(requestName)

}

class JmsBuilder(val requestName: String) {

  /**
   * Builds a request reply JMS test
   */
  def reqreply = JmsReqReplyBuilder(requestName)

}

