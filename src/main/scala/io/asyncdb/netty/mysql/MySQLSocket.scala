package io.asyncdb
package netty
package mysql

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent._
import cats.data.NonEmptyList
import io.netty.bootstrap.Bootstrap

class MySQLSocket[F[_]](
  config: NettySocketConfig[F],
  ref:  MsgRef[F]
)(implicit F: Concurrent[F])
    extends NettySocket[F, Message](config) {

  def read() = ref.take.rethrow

  def write(n: Message) = {
    channel.flatMap(_.write(n).to[F]).void
  }
}


object MySQLSocket {
  def apply[F[_]: ConcurrentEffect](bootstrap: Bootstrap) = {
    val msgRef = MVar[F].empty[Either[Throwable, Message]]
    for {
      ref <- msgRef
      codec = new FrameCodec[F](ref)
      _ = bootstrap.handler(codec)
      config <- NettySocket.newConfig(bootstrap)
    } yield new MySQLSocket[F](config, ref)
  }
}
