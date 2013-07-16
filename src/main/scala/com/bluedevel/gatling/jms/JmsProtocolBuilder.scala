package com.bluedevel.gatling.jms


/**
 * JmsProtocolBuilder
 */
object JmsProtocolBuilder {
  val default = new JmsProtocolBuilder(JmsProtocol.default)
}

/**
 * JmsProtocolBuilder allows building of the JMS protocol
 * <p>
 * This allows multiple scenarios or jms methods to refer to a single protocol configuration.
 * <p>
 * See your JMS provider documentation for information on the values to set here.
 */
case class JmsProtocolBuilder(protocol: JmsProtocol) {

  /**
   * Configures the JMS Connection factory name - see your JMS provider docs
   */
  def connectionFactoryName(cf: String) = copy(protocol = protocol.copy(connectionFactoryName = Some(cf)))

  /**
   * Configures the JMS URL - see your JMS provider docs
   */
  def url(theUrl: String) = copy(protocol = protocol.copy(jmsUrl = Some(theUrl)))

  /**
   * Configures the JMS connection credentials - see your JMS provider docs
   */
  def credentials(user: String, pass: String) = copy(protocol = protocol.copy(username = Some(user), password = Some(pass)))

  /**
   * Configures the context factory name - see your JMS provider docs
   */
  def contextFactory(factory: String) = copy(protocol = protocol.copy(contextFactory = Some(factory)))

  /**
   * A number (default=1) of listener threads will be set up to wait for JMS response messages.
   * <p>
   * In extremely high volume testing you may need to set up multiple listener threads.
   */
  def listenerCount(count: Int) = copy(protocol = protocol.copy(listenerCount = count))

  /**
   * Builds the required protocol. Generally only used by Gatling.
   */
  def build = {
    require(! protocol.connectionFactoryName.isEmpty, "Connection factory must be set")
    require(! protocol.jmsUrl.isEmpty, "JMS URL must be set")
    require(! protocol.contextFactory.isEmpty, "Context Factory must be set")
    require(protocol.listenerCount >= 1, "JMS response listener count must be at least 1")
    require(protocol.username.isEmpty ^ protocol.password.isEmpty, "Username or password should both be set or neither")
    protocol
  }

}
