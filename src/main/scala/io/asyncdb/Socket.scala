package io.asyncdb

/**
 * Binary oriented socket interface
 */
private[asyncdb] trait Socket[F[_], Bin] {
  def write(data: Bin, timeout: Long): F[Unit]
  def readN(n: Int, timeout: Long): F[Bin]
  def connect(): F[this.type]
  def disconnect(): F[Unit]
}
