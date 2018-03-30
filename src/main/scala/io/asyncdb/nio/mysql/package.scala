package io.asyncdb
package nio

package object mysql {
  private[mysql] def decodeBuf[A](buf: Buf)(implicit reader: Reader[A]) = {
    reader.read(buf)
  }
}
