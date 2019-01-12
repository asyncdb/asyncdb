package io.asyncdb
package netty
package mysql

import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import io.asyncdb.netty.mysql.protocol.server._
import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec.ByteToMessageCodec
import io.netty.util.AttributeKey
import java.nio.charset.Charset

class FrameCodec[F[_]: ConcurrentEffect](ref: MsgRef[F])
    extends ByteToMessageCodec[Message] {

  private val initStateKey: AttributeKey[InitState] =
    AttributeKey.valueOf("InitState")

  override def encode(
    ctx: ChannelHandlerContext,
    msg: Message,
    out: ByteBuf
  ) = {}

  override def decode(
    ctx: ChannelHandlerContext,
    in: ByteBuf,
    out: java.util.List[AnyRef]
  ) = {
    ctx.channel().attr(initStateKey).get match {
      case InitState.WaitHandshakeInit =>
        val pd = PacketDecoder[HandshakeInit]
        if (pd.isReady(in)) {
          ref.put {
            pd.decode(in, Charset.defaultCharset())
          }.toIO.unsafeRunSync()
        }
      case InitState.ReceivedHandshakeInit =>
        ???
      case InitState.ReceivedLoginResponse =>
        ???
    }
  }

  override def handlerAdded(ctx: ChannelHandlerContext) = {
    ctx.channel().attr(initStateKey).set(InitState.WaitHandshakeInit)
  }
}
