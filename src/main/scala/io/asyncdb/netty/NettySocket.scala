package io.asyncdb
package netty

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent.Deferred
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel

trait NettySocketConfig {
  val bootstrap: Bootstrap
}

abstract class NettySocket[F[_], M](
  config: NettySocketConfig,
  channelHolder: Deferred[F, Either[Throwable, Channel]])(
  implicit F: Concurrent[F]
) extends Socket[F, M] {

  protected def open = F.delay(config.bootstrap.connect()).flatMap { f =>
    f.to[F]
      .attempt
      .flatMap { e =>
        channelHolder.complete(e.map(_.channel()))
      }
      .as(this)
  }

  protected def close = channel.flatMap(ch => F.delay(ch.close()))

  protected def channel = channelHolder.get.rethrow

}
