package io.asyncdb
package nio
package mysql

sealed trait Packet
case class HandshakeRequest(flag: Int) extends Packet
case class HandshakeResponse() extends Packet
