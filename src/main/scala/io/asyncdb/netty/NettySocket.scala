package io.asyncdb
package netty

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent.Deferred
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel

case class NettySocketConfig[F[_]](
  bootstrap: Bootstrap,
  channel: Deferred[F, Either[Throwable, Channel]]
)

abstract class NettySocket[F[_], M](config: NettySocketConfig[F])(
  implicit F: Concurrent[F]
) extends Socket[F, M] {

  def connect = F.delay(config.bootstrap.connect()).flatMap { f =>
    f.to[F]
      .attempt
      .flatMap { e =>
        config.channel.complete(e.map(_.channel()))
      }
      .as(this)
  }

  def disconnect() = channel.flatMap(ch => F.delay(ch.close()))

  protected def channel = config.channel.get.rethrow

}

object NettySocket {

  def newConfig[F[_]: Concurrent](
    bootstrap: Bootstrap
  ): F[NettySocketConfig[F]] = {
    Deferred[F, Either[Throwable, Channel]].map { defer =>
      NettySocketConfig[F](bootstrap, defer)
    }
  }
}
