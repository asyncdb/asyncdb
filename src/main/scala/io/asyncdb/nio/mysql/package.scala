package io.asyncdb
package nio

package object mysql {
  private[mysql] def decodeBuf[A](buf: BufView)(implicit reader: Reader[A]) = {
    reader.read(buf)
  }

  val MaxPacketSize = 16L * 1024L * 1024L
}
