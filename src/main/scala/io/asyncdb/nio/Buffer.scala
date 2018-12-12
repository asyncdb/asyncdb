package io.asyncdb
package nio

import cats.data.NonEmptyList
import scala.collection.mutable.ArrayBuffer

/**
 * BufferReader for bytes reading, allow composite two or more [[ByteBuffers]]
 */
trait BufferReader {
  def size: Long
  def get: Byte
  def take(n: Int): Array[Byte]
  def takeWhile(p: Byte => Boolean): Array[Byte]
  def ++(r: BufferReader): BufferReader = new BufferReader.Append(this, r)
  def array: Array[Byte]
  def hasRemaining: Boolean

  protected def takeWhile0(p: Byte => Boolean): (Array[Byte], Boolean)
}

object BufferReader {

  def apply(bytes: Array[Byte]): BufferReader =
    apply(java.nio.ByteBuffer.wrap(bytes))

  def apply(buf: Buf): BufferReader = new BufferReader {

    val init = buf.duplicate()

    def get = buf.get

    def array = init.array()

    def size = init.remaining()

    def take(n: Int) = {
      val arr = Array.ofDim[Byte](n)
      buf.get(arr)
      arr
    }

    def takeWhile(p: Byte => Boolean) = takeWhile0(p)._1

    def hasRemaining = buf.hasRemaining()

    def takeWhile0(p: Byte => Boolean) = {

      @scala.annotation.tailrec
      def loop(
        matches: Boolean,
        rs: ArrayBuffer[Byte]
      ): (Boolean, ArrayBuffer[Byte]) = {
        if (buf.hasRemaining()) {
          val b = get
          if (!p(b)) {
            buf.position(buf.position() - 1)
            (true, rs += b)
          } else {
            rs += b
            loop(false, rs)
          }
        } else {
          (matches, rs)
        }
      }

      val abf          = new ArrayBuffer[Byte]
      val (matches, _) = loop(false, abf)
      (abf.toArray, matches)
    }
  }

  private class Append(left: BufferReader, right: BufferReader)
      extends BufferReader {
    def array = left.array ++ right.array
    def size  = left.size + right.size
    def take(n: Int): Array[Byte] = {
      val leftTaked = left.take(n)
      if (leftTaked.size < n) {
        leftTaked ++ right.take(n - leftTaked.size)
      } else leftTaked
    }

    def get = if (left.hasRemaining) left.get else right.get

    def hasRemaining = left.hasRemaining || right.hasRemaining

    def takeWhile(p: Byte => Boolean) = {
      val (bytes, isMatched) = left.takeWhile0(p)
      if (!isMatched) bytes ++ right.takeWhile(p) else bytes
    }
    def takeWhile0(p: Byte => Boolean) = {
      val (bytes, isMatched) = left.takeWhile0(p)
      if (isMatched) {
        (bytes, isMatched)
      } else {
        val (rBytes, isMatched) = right.takeWhile0(p)
        (bytes ++ rBytes, isMatched)
      }
    }
  }
}
