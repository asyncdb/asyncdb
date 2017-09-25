package io.asyncdb

import cats.effect.IO

trait Socket {
  def readInt(): IO[Int]
  def readLong(): IO[Long]
  def readBytes(n: Int): IO[Array[Byte]]

  def writeInt(i: Int): IO[Unit]
  def writeLong(l: Long): IO[Unit]
  def writeBytes(bytea: Array[Byte]): IO[Unit]
}
