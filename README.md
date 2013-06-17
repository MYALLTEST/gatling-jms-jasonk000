gatling-jms
===========
A simple JMS Gatling test library. Gatling provides a simple and relatively pretty performance test framework. http://gatling-tool.org/

Currently this library runs "synchronously" in the Gatling query threads.

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
* ADD A BUILD SCRIPT
* ADD SOME TESTS
* Enable checks on the response message (was it sent with the correct text)
* Enable a null value to flag response->not ok
* Use a protocol configuration registry instead of using the builder
* Enable more input types [over and above TextMessage]
* Enable setting headers on JMS messages (addHeader())
* Add a build script
* Convert to an async handler
  * Take advantage of JMS MessageListener spec, and use JMS message correlation IDs to correlate request and reply

License
===========
Copyright jasonk000.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

