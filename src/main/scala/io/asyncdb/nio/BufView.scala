package io.asyncdb
package nio

trait BufView {
  def hasRemaining(): Boolean
  def get(ba: Array[Byte]): Unit
  def get(): Byte
  def takeThrough(f: Byte => Boolean): Array[Byte]
}

object BufView {
  def apply(buf: Buf): BufView           = ???
  def apply(bytes: Array[Byte]): BufView = ???
  def composite(bufs: BufView*): BufView = ???
}
