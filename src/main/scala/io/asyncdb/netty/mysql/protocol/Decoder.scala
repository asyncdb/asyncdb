package io.asyncdb
package netty
package mysql

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * Internal api, not fully safe, should carefully dealed at call site.
 * The buffer passed to this decoder is raw data buffer, mysql seq and len head is striped
 */
private[mysql] trait MessageDecoder[V] {
  def decode(b: ByteBuf): V
}

private[mysql] final class PacketDecoder[V](md: MessageDecoder[V]){
  def isReady(buf: ByteBuf): Boolean = {
    @annotation.tailrec
    def isLastPacketReady(fromOffset: Int): Boolean = {
      val headReady = buf.readableBytes() < fromOffset + 4
      headReady && {
        val packetLen = buf.getUnsignedMedium(fromOffset + 1)
        val packetReady = (buf.readableBytes() - fromOffset) >= packetLen + 4
        val isLast = packetLen < Packet.MaxSize
        (isLast && packetReady) || isLastPacketReady(fromOffset + packetLen + 4)
      }
    }
    isLastPacketReady(buf.readerIndex())
  }

  @annotation.tailrec
  private def dataBuffer(buf: ByteBuf, from: Int, composite: Vector[ByteBuf]): Vector[ByteBuf] = {
    val packetLen = buf.getUnsignedMedium(from + 1)
    val dataStart = from + 4
    val dataEnd = dataStart + packetLen
    val thisAdded = composite :+ buf.slice(dataStart, dataEnd)
    if(packetLen >= Packet.MaxSize) {
      dataBuffer(buf, dataEnd, thisAdded)
    } else {
      thisAdded
    }
  }

  def decode(buf: ByteBuf) = {
    Unpooled.wrappedBuffer(dataBuffer(buf, buf.readerIndex(), Vector.empty).toArray: _*)
  }
}
