package io.asyncdb
package netty
package mysql

import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import io.asyncdb.netty.mysql.protocol.server._
import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec._
import io.netty.util.AttributeKey
import java.nio.charset.Charset
import protocol.client._
import protocol.server._

class FrameEncoder(charset: Short) extends ChannelOutboundHandlerAdapter {
  val jcharset = CharsetMap.of(charset)
  override def write(ctx: ChannelHandlerContext, msg: AnyRef, p: ChannelPromise) = {
    msg match {
      case m: HandshakeResponse =>
        val buf = ctx.alloc().buffer(1024)
        val bufs = PacketsEncoder.encode(m, buf, jcharset)
        val wrapped = Unpooled.wrappedBuffer(bufs: _*)
        ctx.write(wrapped, p)
    }
    ctx.flush()
  }
}

class FrameDecoder[F[_]: ConcurrentEffect](ref: MsgRef[F])
    extends ByteToMessageDecoder {

  private val initStateKey: AttributeKey[InitState] =
    AttributeKey.valueOf("InitState")

  private val charsetKey: AttributeKey[Short] =
    AttributeKey.valueOf("Charset")

  override def decode(
    ctx: ChannelHandlerContext,
    in: ByteBuf,
    out: java.util.List[AnyRef]
  ) = {
    ctx.channel().attr(initStateKey).get match {
      case InitState.WaitHandshakeInit =>
        val pd = PacketDecoder[HandshakeInit]
        if (pd.isReady(in)) {
          val m = pd.decode(in, Charset.defaultCharset())
          ref.put(m).toIO.unsafeRunSync()
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

  private def charset[A](ctx: ChannelHandlerContext, k: AttributeKey[A]) = {
    ctx.channel().attr(charsetKey).get
  }
}
