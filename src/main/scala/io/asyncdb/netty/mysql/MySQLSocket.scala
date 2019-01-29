package io.asyncdb
package netty
package mysql

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent._
import io.netty.bootstrap.Bootstrap
import io.netty.channel.{Channel, ChannelInitializer}
import java.nio.charset.Charset
import protocol.server._

case class MySQLSocketConfig(
  bootstrap: Bootstrap,
  username: String,
  password: Option[String],
  database: Option[String],
  charset: Short,
  authMethod: Option[String]
) extends NettySocketConfig

class MySQLSocket[F[_]](
  config: MySQLSocketConfig,
  channelHolder: Deferred[F, Either[Throwable, Channel]],
  ref: MsgRef[F]
)(implicit F: Concurrent[F])
    extends NettySocket[F, Message](config, channelHolder) {

  def connect = {
    open.flatMap(_.read).as(this)
  }

  def disconnect = {
    close.void
  }

  def write(n: Message) = {
    channel.flatMap(_.write(n).to[F]).void
  }

  def read = ref.take.flatMap {
    case OrErr(value) =>
      F.fromEither(value)
    case v => F.pure(v)
  }
}

object MySQLSocket {
  def apply[F[_]: ConcurrentEffect](config: MySQLSocketConfig) = {
    for {
      msgRef   <- MVar[F].empty[Message]
      clientCS <- Deferred[F, Charset]
      initCtx: ChannelContext = ChannelContext.WaitInit
      ctxRef <- Ref[F].of(initCtx)
      decoder = new FrameDecoder[F](config, ctxRef, msgRef)
      encoder = new FrameEncoder(config, ctxRef)
      initHandler = new ChannelInitializer[Channel] {
        override def initChannel(channel: Channel): Unit = {
          channel
            .pipeline()
            .addLast("MySQLFrameDecoder", decoder)
            .addLast("MySQLFrameEncoder", encoder)
        }
      }
      _ = config.bootstrap.handler(initHandler)
      channel <- Deferred[F, Either[Throwable, Channel]]
    } yield new MySQLSocket[F](config, channel, msgRef)
  }
}
