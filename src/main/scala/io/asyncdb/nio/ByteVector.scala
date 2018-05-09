package io.asyncdb
package nio

trait ByteVector {
  def size: Long
  def take(n: Int): Array[Byte]
  def ++(r: ByteVector): ByteVector
}

object ByteVector {
  def apply(bb: Buf): ByteVector            = ???
  def apply(bytes: Array[Byte]): ByteVector = ???
}
