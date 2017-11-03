package io.asyncdb
package nio
package mysql

private[mysql] sealed trait PayloadWriter[A] {
  def write(buf: Buf, payload: A): Unit
}

private[mysql] sealed trait PayloadReader[A] {
  def read(buf: Buf): Either[ClientError, A]
}
