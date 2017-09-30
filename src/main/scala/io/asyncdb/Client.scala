package io.asyncdb

import cats.effect.IO

trait Client {
  def execute[I, O](cmd: I): IO[O]
}
