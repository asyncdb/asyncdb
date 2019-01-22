package io.asyncdb
package netty
package mysql

import cats.effect._
import cats.effect.concurrent._
import cats.effect.syntax.all._
import cats.instances.option._
import cats.syntax.all._
import io.asyncdb.netty.mysql.protocol.server._
import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec._
import io.netty.util.AttributeKey
import java.nio.charset.Charset

import io.asyncdb.netty.mysql.protocol.Command
import protocol.client._
import protocol.server._

class FrameEncoder[F[_]](config: MySQLSocketConfig)
    extends ChannelOutboundHandlerAdapter {

  override def write(
    ctx: ChannelHandlerContext,
    msg: AnyRef,
    p: ChannelPromise
  ) = {

    val charset = CharsetMap.of(config.charset)
    val packets = msg match {
      case m: HandshakeResponse =>
        val buf = ctx.alloc().buffer(1024)
        PacketsEncoder.encode(m, buf, charset, 1)
      case m: Query =>
        val buf = ctx
          .alloc()
          .buffer(
            Packet.PacketLength + Packet.CommandLength + m.query
              .getBytes(charset)
              .size
          )
        PacketsEncoder.encode(m, buf, charset)
    }
    val wrapped = Unpooled.wrappedBuffer(packets: _*)
    ctx.write(wrapped, p)
    ctx.flush()
  }
}

class FrameDecoder[F[_]](
  config: MySQLSocketConfig,
  ctxRef: Ref[F, ChannelContext[F]],
  msgRef: MsgRef[F]
)(implicit F: ConcurrentEffect[F])
    extends ByteToMessageDecoder {

  private val FSM = new StateMachine[F](config)

  private val charsetKey: AttributeKey[Short] =
    AttributeKey.valueOf("Charset")

  override def decode(
    ctx: ChannelHandlerContext,
    in: ByteBuf,
    out: java.util.List[AnyRef]
  ) = {
    if (PacketDecoder.isReady(in)) {
      ctxRef.access.flatMap {
        case (old, updateF) =>
          FSM.transition(in).run(old).flatMap {
            case (nc, ChannelState.Result(o, e)) =>
              val fireOutgoing = o.traverse { om =>
                F.delay {
                  ctx.channel().write(om)
                }
              }
              val enqueueEmit = e.traverse { em =>
                msgRef.put(em)
              }
              fireOutgoing *> enqueueEmit *> updateF(nc)
          }
      }.toIO.unsafeRunSync()
    }
  }
}
