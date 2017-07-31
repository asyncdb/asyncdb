package io.asyncdb

import cats.effect.IO

trait Socket {
  def readInt: IO[Int]
  def readLong: IO[Long]
  def readBytes(n: Int): IO[Array[Byte]]
}
