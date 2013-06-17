gatling-jms
===========
A simple JMS Gatling test library.

Currently this library runs "synchronously" in the Gatling query threads.

Example
===========
```scala
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
```

Caveats
===========
Currently this library runs synchronously in the Gatling query threads. This makes it susceptible to 'coordinated omission' issues in the results.

TODO
===========
Enable checks on the response message (was it sent with the correct text)
Enable a null value to flag response->not ok
Use a protocol configuration registry instead of using the builder
Enable more input types [over and above TextMessage]
Enable setting headers on JMS messages (addHeader())
Add a build script
Create this as an async handler


