package io.asyncdb
package nio
package mysql

/**
 * https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeV10
 */
case class ExtraHandshakeData(
  characterSet: Int1,
  statusFlags: Int2,
  capabilityFlagUpper: Int2,
)

case class HandshakeInitData(
  protocol: Int1,
  version: NullDelimitedBytes,
  connectionId: Int,
  authPluginDataPart1: Array[Byte],
  filter: Int1,
  capabilityFlagLower: Int2,
  authPluginDataLen: Int1,
  extra: Option[ExtraHandshakeData]
)

case class HandshakeInit(
  protocol: Int1,
  version: String,
  connectionId: Int,
  authPluginData: Array[Byte],
  charset: Int1,
  status: Int2
)

object HandshakeInit {}
