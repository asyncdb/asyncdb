package io.asyncdb
package netty

import io.netty.buffer._

object HexDump {
  def dump(buf: ByteBuf) = ByteBufUtil.hexDump(buf)
  def decode(hex: String) = ByteBufUtil.decodeHexDump(hex)
}
