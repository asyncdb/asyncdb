package io.asyncdb
package netty
package mysql

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent._
import cats.data.NonEmptyList

class MySQLSocket[F[_]](config: NettySocketConfig[F], state: Ref[F, Deferred[F, Either[Throwable, Message]]])
  (implicit F: Concurrent[F]) extends NettySocket[F, Message](config) {

  def read() = state.get.flatMap(_.get.rethrow)

  def write(n: Message) = {
    channel.flatMap(_.write(n).to[F]).void
  }

}
