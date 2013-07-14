package com.bluedevel.gatling.jms


/**
 * JmsProtocolBuilder
 */
object JmsProtocolBuilder {
  val default = new JmsProtocolBuilder(JmsProtocol.default)
}

/**
 * JmsProtocolBuilder allows building of the JMS protocol
 * This allows multiple scenarios or jms methods to refer to a single protocol configuration
 */
case class JmsProtocolBuilder(protocol: JmsProtocol) {

  def connectionFactoryName(cf: String) = copy(protocol = protocol.copy(connectionFactoryName = cf))
  def url(theUrl: String) = copy(protocol = protocol.copy(jmsUrl = theUrl))
  def credentials(user: String, pass: String) = copy(protocol = protocol.copy(username = Some(user), password = Some(pass)))
  def contextFactory(factory: String) = copy(protocol = protocol.copy(contextFactory = factory))
  def listenerCount(count: Int) = copy(protocol = protocol.copy(listenerCount = count))

  def build = {
    protocol
  }

}
