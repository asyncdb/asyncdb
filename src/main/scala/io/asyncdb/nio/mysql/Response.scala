package io.asyncdb
package nio
package mysql

/**
 * HandshakeInit message sent from server
 */
case class HandshakeInit(
  protocol: Byte,
  version: Array[Byte],
  threadId: Long,
  salt: Array[Byte],
  serverCap: Int,
  charset: Short,
  status: Short)

object HandshakeInit {}
