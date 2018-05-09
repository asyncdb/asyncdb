package io.asyncdb
package nio

trait ByteVector {
  def size: Long
  def take(n: Int): Array[Byte]
  def takeWhile(p: Byte => Boolean): Array[Byte]
  def ++(r: ByteVector): ByteVector = new ByteVector.Append(this, r)
  def array: Array[Byte]
  def bufs: Vector[Buf]
  protected def takeWhile0(p: Byte => Boolean): (Array[Byte], Boolean)
}

object ByteVector {

  private class Append(left: ByteVector, right: ByteVector) extends ByteVector {
    def array = left.array ++ right.array
    def size  = left.size + right.size
    def take(n: Int): Array[Byte] = {
      val leftTaked = left.take(n)
      if (leftTaked.size < n) {
        leftTaked ++ right.take(n - leftTaked.size)
      } else leftTaked
    }
    def bufs                           = left.bufs ++ right.bufs
    def takeWhile(p: Byte => Boolean)  = ???
    def takeWhile0(p: Byte => Boolean) = ???
  }

  def apply(bb: Buf): ByteVector            = ???
  def apply(bytes: Array[Byte]): ByteVector = ???
}
