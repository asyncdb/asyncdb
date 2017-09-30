package io.asyncdb
package nio
package mysql

import cats.effect._

trait MySQLSocket extends NioSocket {
  def readInt1(buf: Buf, timeout: Long): IO[Int @@ Int1]
  def readInt3(buf: Buf, timeout: Long): IO[Int @@ Int3]
}

trait MySQLCodec {}
