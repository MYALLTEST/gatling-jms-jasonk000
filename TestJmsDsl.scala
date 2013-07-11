package com.bluedevel.gatling.jms

import net.timewalker.ffmq3.FFMQConstants
import io.gatling.core.Predef._
import com.bluedevel.gatling.jms.Predef._
import scala.concurrent.duration._
import bootstrap._

/**
 * Sample for using the JMS DSL
 */
class TestJmsDsl extends Simulation {

  // configure a connection to JMS
  val jmsConfig = JmsProtocolBuilder.default
    .connectionFactoryName(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME)
    .url("tcp://localhost:10002")
    .credentials("x", "x")
    .contextFactory(FFMQConstants.JNDI_CONTEXT_FACTORY)

  // define a scenario, using the DSL API
  val scn = scenario("JMS DSL test").repeat(1) {
    exec(jms("req reply testing").reqreply
      .queue("jmstestq")
      .textMessage("hello from gatling jms dsl"))
  }

  // and run the test (standard Gatling)
  setUp(scn.protocolConfig(jmsConfig).inject(
       ramp(20 users) over (3 seconds),
       constantRate(30 usersPerSec) during (120 seconds))
    )
}
