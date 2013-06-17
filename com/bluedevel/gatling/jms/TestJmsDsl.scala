package com.bluedevel.gatling.jms

import net.timewalker.ffmq3.FFMQConstants
import io.gatling.core.Predef._ 
import com.bluedevel.gatling.jms.Predef._
import scala.concurrent.duration._
import bootstrap._

class TestJmsDsl extends Simulation { 
  val scn = scenario("JMS DSL test").repeat(1) {
    exec(jms("req reply testing").reqreply
      .queue("jmstestq")
      .contextFactory(FFMQConstants.JNDI_CONTEXT_FACTORY)
      .connectionFactoryName(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME)
      .url("tcp://localhost:10002")
      .credentials("x", "x")
      .textMessage("hello from gatling jms dsl"))
  }

  setUp(scn.inject(
       ramp(20 users) over (3 seconds),
       constantRate(30 usersPerSec) during (120 seconds))
    )
}


