package io.asyncdb
package netty
package mysql

import cats.MonadError
import cats.data.StateT
import cats.effect.concurrent._
import cats.syntax.all._
import cats.instances.either._
import io.netty.buffer._
import io.netty.channel._
import java.nio.charset.Charset
import protocol.client._
import protocol.server._

sealed trait ChannelState

object ChannelState {

  sealed trait Handshake extends ChannelState

  object Handshake {
    object WaitHandshakeInit extends Handshake
    object WaitAuthResult    extends Handshake
  }

  object ReadForCommand extends ChannelState

  case class Result(
    outgoing: Option[Message],
    emit: Option[Message]
  )

}

case class ChannelContext[F[_]](
  state: ChannelState,
  serverCharset: Deferred[F, Charset]
)

class StateMachine[F[_]](config: MySQLSocketConfig)(
  implicit F: MonadError[F, Throwable]
) {

  type Transition =
    Function[ByteBuf, StateT[F, ChannelContext[F], ChannelState.Result]]

  def transition: Transition = { buf =>
    StateT { ctx =>
      ctx.state match {

        case ChannelState.Handshake.WaitHandshakeInit =>
          PacketDecoder[HandshakeInit]
            .decode(buf, Charset.defaultCharset())
            .liftTo[F]
            .flatMap { init =>
              val out = HandshakeResponse(init, config)
              val r   = ChannelState.Result(Some(out), None)
              val nc  = ctx.copy(state = ChannelState.Handshake.WaitAuthResult)
              nc.serverCharset.complete(init.charset).as(nc -> r)
            }

        case ChannelState.Handshake.WaitAuthResult =>
          for {
            cs <- ctx.serverCharset.get
            m  <- PacketDecoder[OrErr[Ok]].decode(buf, cs).liftTo[F]
          } yield {
            println(m)
            val nc = ctx.copy(
              state = ChannelState.ReadForCommand
            )
            val outgoing = None
            val emit     = Some(m)
            (nc, ChannelState.Result(outgoing, emit))
          }
      }

    }
  }
}
