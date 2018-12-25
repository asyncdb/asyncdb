package io.asyncdb

/**
 * Binary oriented socket interface
 */
private[asyncdb] trait Socket[F[_], M] {
  def write(data: M): F[Unit]
  def read(): F[M]
  def connect(): F[this.type]
  def disconnect(): F[Unit]
}
