gatling-jms
===========
A simple JMS Gatling test library. Gatling provides a simple and relatively pretty performance test framework. http://gatling-tool.org/

Example
===========
Short example, assuming FFMQ on localhost, using a reqreply query, to the queue named 'jmstestq'.

```scala
package com.bluedevel.gatling.jms

import net.timewalker.ffmq3.FFMQConstants
import io.gatling.core.Predef._
import com.bluedevel.gatling.jms.Predef._
import scala.concurrent.duration._
import bootstrap._
import javax.jms.{ Message, TextMessage }

class TestJmsDsl extends Simulation {

  val jmsConfig = JmsProtocolBuilder.default
    .connectionFactoryName(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME)
    .url("tcp://localhost:10002")
    .credentials("user", "secret")
    .contextFactory(FFMQConstants.JNDI_CONTEXT_FACTORY)
    .listenerCount(1)
    .deliveryMode(javax.jms.DeliveryMode.PERSISTENT)

  val scn = scenario("JMS DSL test").repeat(1) {
    exec(jms("req reply testing").reqreply
      .queue("jmstestq")
// -- four message types are supported; only StreamMessage is not currently supported
      .textMessage("hello from gatling jms dsl")
//      .bytesMessage(new Array[Byte](1))
//      .mapMessage(new ListMap[String, Object])
//      .objectMessage("hello!")
      .addProperty("test_header", "test_value")
      .addCheck(checkBodyTextCorrect)
    )
  }

  setUp(scn.inject(
       rampRate(10 usersPerSec) to (1000 usersPerSec) during (2 minutes)
    )).protocols(jmsConfig)

  /**
   * Checks if a body text is correct.
   * <p>
   * Note the contract on the checks is Message => Boolean, so you can perform
   * any processing you like on the message (check headers, check type, check body,
   * complex checks, etc).
   */
  def checkBodyTextCorrect(m: Message) = {
    // this assumes that the service just does an "uppercase" transform on the text
    val BODY_SHOULD_BE = "HELLO FROM GATLING JMS DSL"
    m match {
      case tm: TextMessage => (tm.getText.toString == BODY_SHOULD_BE)
      case _ => false
    }
  }

}

```

Using Gatling-JMS
===========
You can download a copy from build/libs/gatling-jms.jar
Place this into gatling/lib
You'll also need: jms-1.1.jar, and your JMS driver jar files and dependencies. Place these into gatling/lib too.

Now, you can use the DSL in your simulations. See above example snippet.


Building from source
===========
You may need to download some JARs from here if the gradle config isn't working: http://repository.excilys.com/content/groups/public/

```
$ gradle clean jar
$ cp build/libs/gatling-jms.jar ~/gatling/libs
```

TODO
===========
Most of the obvious core API is built. Need to add some unit test coverage and possibly some additional checks to make the check DSL more fluent.

Now available for public testing & use. Please raise any issues - https://github.com/jasonk000/gatling-jms/issues

License
===========
Copyright jasonk000.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

