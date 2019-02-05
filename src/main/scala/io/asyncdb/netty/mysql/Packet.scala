package io.asyncdb
package netty
package mysql

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset

object Packet {
  final val MaxSize       = 0x00ffffff
  final val PacketLength  = 4
  final val CommandLength = 1

  def encode[V: Encoder](v: V, buf: ByteBuf, cs: Charset, seq: Int = 0) = {
    PacketEncoder.encode(v, buf, cs, seq)
  }

  def decode[V: Decoder](
    buf: ByteBuf,
    cs: Charset,
    state: ChannelState = ChannelState.Handshake.WaitHandshakeInit
  ) =
    PacketDecoder[V].decode(buf, cs, state)
}
