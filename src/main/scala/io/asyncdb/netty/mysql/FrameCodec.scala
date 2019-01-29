package io.asyncdb
package netty
package mysql

import cats.effect._
import cats.effect.concurrent._
import cats.effect.syntax.all._
import cats.instances.option._
import cats.syntax.all._
import io.netty.buffer._
import io.netty.channel._
import io.netty.handler.codec._
import io.netty.util.AttributeKey
import java.nio.charset.Charset
import protocol.client._
import protocol.server._

class FrameEncoder[F[_]: ConcurrentEffect](
  config: MySQLSocketConfig,
  ctxRef: Ref[F, ChannelContext])
    extends ChannelOutboundHandlerAdapter {

  private val FSM = new StateMachine[F](config)

  override def write(
    ctx: ChannelHandlerContext,
    msg: AnyRef,
    p: ChannelPromise
  ) = {

    def sendPackets(packets: Vector[ByteBuf]) = {
      val wrapped = Unpooled.wrappedBuffer(packets: _*)
      ctx.write(wrapped, p)
      ctx.flush()
    }
    val buf = ctx.alloc().buffer(1024)
    val charset = CharsetMap.of(config.charset)
    msg match {
      case m: Message =>
        ctxRef.modifyState(FSM.send(m, buf)).map(sendPackets).toIO.unsafeRunSync()
    }
  }
}

class FrameDecoder[F[_]](
  config: MySQLSocketConfig,
  ctxRef: Ref[F, ChannelContext],
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
      val msg = ctxRef.get.flatMap {
        case ChannelContext.WaitInit =>
          F.fromEither(Packet.decode[ServerMessage](in, Charset.defaultCharset()))
        case ChannelContext.Inited(cs, _) =>
          F.fromEither(Packet.decode[ServerMessage](in, cs))
      }

      msg.flatMap { m =>
        ctxRef.modifyState(FSM.receive(m)).flatMap {
          case ChannelState.Result(o, e) =>
            val fireOutgoing = o.traverse { om =>
              F.delay {
                ctx.channel().write(om)
              }
            }
            val enqueueEmit = e.traverse(msgRef.put)
            fireOutgoing *> enqueueEmit
        }
      }.toIO.unsafeRunSync()
    }
  }
}
