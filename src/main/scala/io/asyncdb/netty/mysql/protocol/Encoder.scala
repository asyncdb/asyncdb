package io.asyncdb
package netty
package mysql

import io.netty.buffer._

trait Encoder[V] {
  def encode(v: V, buf: ByteBuf): Unit
}

class PacketsEncoder[V] {

  @inline def packet(seq: Int, len: Int, payload: ByteBuf) = {
    val headByts = Array.ofDim[Byte](4)
    val headerBuf = Unpooled.wrappedBuffer(headByts)
    headerBuf
      .writeByte(seq)
      .writeMediumLE(len)
    Unpooled.wrappedBuffer(headerBuf, payload)
  }

  def encodes[V](v: V, buf: ByteBuf)(implicit ve: Encoder[V]) = {
    val fullBuf = ve.encode(v, buf)
    val fullLength = buf.readableBytes()

    @scala.annotation.tailrec
    def splitPackets(from: Int, seq: Int, previous: Vector[ByteBuf]): Vector[ByteBuf] = {
      if(fullLength - from >= Packet.MaxSize) {
        val len = Packet.MaxSize
        val p = packet(seq, len, buf.slice(from, from + len))
        splitPackets(from + len, seq + 1, previous :+ p)
      } else {
        val len = fullLength - from
        val p = packet(seq, len, buf.slice(from, fullLength))
        previous :+ p
      }
    }
  }
}
