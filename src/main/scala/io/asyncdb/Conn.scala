package io.asyncdb

import cats.effect.IO

trait Conn[I, O] {
  def execute(cmd: I): IO[O]
}

object Conn {
}
