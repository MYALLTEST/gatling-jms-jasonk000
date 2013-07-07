package com.bluedevel.gatling.jms

/**
 * Wraps up the DSL entry point, exposed through the Predef's
 */
object JmsBuilder {
  /**
   * jms() is the entry pointk
   */
  def jms(requestName: String) = new JmsBuilder(requestName)
}

class JmsBuilder(val requestName: String) {

  /**
   * Builds a request reply JMS test
   */
  def reqreply = JmsReqReplyBuilder(requestName)

}

