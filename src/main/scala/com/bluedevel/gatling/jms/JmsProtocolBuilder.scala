package com.bluedevel.gatling.jms

object JmsProtocolBuilder {
  val default = new JmsProtocolBuilder(JmsProtocol.default)
}

case class JmsProtocolBuilder(protocol: JmsProtocol) {

  def connectionFactoryName(cf: String) = copy(protocol = protocol.copy(connectionFactoryName = cf))
  def url(theUrl: String) = copy(protocol = protocol.copy(jmsUrl = theUrl))
  def credentials(user: String, pass: String) = copy(protocol = protocol.copy(username = Some(user), password = Some(pass)))
  def contextFactory(factory: String) = copy(protocol = protocol.copy(contextFactory = factory))

  def build = {
    protocol
  }

}
