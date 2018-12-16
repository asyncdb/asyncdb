package io.asyncdb
package nio
package mysql

import java.nio.ByteBuffer

/**
 * Data structure
 */
case class Packet(len: Int3, seq: Int1, payload: Buf)

object Packet {

  def int1(b: Byte) = new Int1(b)
  def int3(i: Int)  = new Int3(i)

  def decodeLength(bytes: Array[Byte]) = {
    val len = (bytes(0).toInt & 0xff) | ((bytes(1).toInt & 0xff) << 8) | ((bytes(
      2
    ).toInt & 0xff) << 16)
    int3(len)
  }

  def decodeSeq(bytes: Array[Byte]) = {
    int1(bytes(3))
  }

  final val MaxInt3 = 0x00ffffff

  def toPacket(buf: Buf) = {
    println(s"the length is ${buf.position() - 5}")
    Packet(Int3(buf.position() - 5), Int1(1), buf)
  }

  def toBuf(packet: Packet) = {
    val buf = packet.payload
    val len = packet.len.value
    buf.position(0)
    buf.put((len & 0xff).toByte)
    buf.put((len >>> 8).toByte)
    buf.put((len >>> 16).toByte)
    buf.put(packet.seq.value)
  }
}
