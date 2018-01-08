package io.asyncdb

trait Client[F[_]] {
  def execute[I, O](cmd: I): F[O]
}
