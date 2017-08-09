package io.asyncdb

import cats.effect.IO

trait Conn {
  def connect: IO[Conn]
  def execute[I, O](cmd: I): IO[O]
  def close: IO[Unit]
}
