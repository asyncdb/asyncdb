package io.asyncdb

import cats.effect.IO

trait Client {
  def execute[I: Writes, O: Reads](cmd: I): IO[O]
}
