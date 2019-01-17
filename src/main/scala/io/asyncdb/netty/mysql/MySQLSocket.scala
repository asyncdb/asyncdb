package io.asyncdb
package netty
package mysql

import cats.syntax.all._
import cats.effect._
import cats.effect.concurrent._
import cats.data.NonEmptyList
import io.netty.bootstrap.Bootstrap
import io.netty.channel.{Channel, ChannelInitializer}
import protocol.client._
import protocol.server._



case class MySQLSocketConfig(
  bootstrap: Bootstrap,
  database: String,
  username: String,
  password: String,
  charset: Short
) extends NettySocketConfig

class MySQLSocket[F[_]](
  config: MySQLSocketConfig,
  channelHolder: Deferred[F, Either[Throwable, Channel]],
  ref: MsgRef[F]
)(implicit F: Concurrent[F])
    extends NettySocket[F, Message](config, channelHolder) {

  def connect = open.flatMap { ch =>
    ch.read().flatMap {
      case init: HandshakeInit =>
        val resp = HandshakeResponse(init, config.charset, config.database, config.username, config.password)
        write(resp).as(this)
      case m => F.raiseError(new IllegalStateException(s"HandshakeInit message expected but got $m"))
    }
  }

  def read() = ref.take.rethrow

  def disconnect = close.void

  def write(n: Message) = {
    channel.flatMap(_.write(n).to[F]).void
  }
}

object MySQLSocket {
  def apply[F[_]: ConcurrentEffect](config: MySQLSocketConfig) = {
    val msgRef = MVar[F].empty[Either[Throwable, Message]]
    for {
      ref <- msgRef
      decoder = new FrameDecoder[F](ref)
      encoder = new FrameEncoder(config.charset)
      init = new ChannelInitializer[Channel] {
        override def initChannel(channel: Channel): Unit = {
          channel
            .pipeline()
            .addLast("MySQLFrameDecoder", decoder)
            .addLast("MySQLFrameEncoder", encoder)
        }
      }
      _     = config.bootstrap.handler(init)
      channel <- Deferred[F, Either[Throwable, Channel]]
    } yield new MySQLSocket[F](config, channel, ref)
  }
}
