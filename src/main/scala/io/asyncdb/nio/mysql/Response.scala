package io.asyncdb
package nio
package mysql

/**
 * HandshakeInit message sent from server
 */
case class HandshakeInit(
  protocol: Int1,
  version: Array[Byte],
  connectionId: Long,
  salt: Array[Byte],
  serverCap: Int,
  charset: Short,
  status: Short)

object HandshakeInit {
}
