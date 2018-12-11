package io.asyncdb
package nio
package mysql

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
}
