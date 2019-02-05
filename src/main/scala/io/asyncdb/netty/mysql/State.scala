package io.asyncdb
package netty
package mysql

import cats.MonadError
import cats.data.{State, StateT}
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

  object ReadyForCommand extends ChannelState

  sealed trait Handshake extends ChannelState
  object Handshake {
    object WaitHandshakeInit extends Handshake
    object WaitAuthResult    extends Handshake
  }

  sealed trait QueryState
  object Query {
    case class WaitColumnData(
      columnCount: Int = 0,
      defs: Vector[ColumnDef] = Vector.empty,
      rows: Vector[Any] = Vector.empty
    ) extends ChannelState
  }

  /**
   * Result produced by a state transition.
   * @param outgoing message send to server if any.
   * @param emit message emit to client if any(will be used for client read).
   */
  case class Result(
    outgoing: Option[Message],
    emit: Option[Message]
  )

}

sealed trait ChannelContext {
  val state: ChannelState
}

object ChannelContext {
  case object WaitInit extends ChannelContext {
    val state = ChannelState.Handshake.WaitHandshakeInit
  }

  case class Inited(serverCharset: Charset, state: ChannelState)
      extends ChannelContext
}

class StateMachine[F[_]](config: MySQLSocketConfig)(
  implicit F: MonadError[F, Throwable]
) {

  /**
   * Input a [[ByteBuf]], decode and process buf with current state, then produce the next state and [[ChannelState.Result]]
   */
  type ReceiveTransition =
    Function[ServerMessage, State[ChannelContext, ChannelState.Result]]

  type SendTransition =
    Function2[Message, ByteBuf, State[ChannelContext, Vector[ByteBuf]]]

  val send: SendTransition = { (msg, buf) =>
    val cs = CharsetMap.of(config.charset)
    State {
      case ctx: ChannelContext.Inited =>
        (msg, ctx.state) match {
          case (q: HandshakeResponse, ChannelState.Handshake.WaitAuthResult) =>
            ctx -> Packet.encode(q, buf, cs, 1)
          case (q: Query, ChannelState.ReadyForCommand) =>
            val nc =
              ctx.copy(state = ChannelState.Query.WaitColumnData())
            nc -> Packet.encode(q, buf, cs)
          case (m, s) =>
            throw new IllegalStateException(s"IllegalStateException ${m}, ${s}")
        }
      case ctx =>
        throw new IllegalStateException(s"IllegalState ${ctx.state}")
    }
  }

  /**
   * Define the finite state matchine for receiving message
   */
  val receive: ReceiveTransition = { msg =>
    State { state =>
      (state, msg) match {
        case (ChannelContext.WaitInit, m: HandshakeInit) =>
          val out = HandshakeResponse(m, config)
          val r   = ChannelState.Result(Some(out), None)
          val nc = ChannelContext.Inited(
            m.charset,
            ChannelState.Handshake.WaitAuthResult
          )
          nc -> r
        case (
            ctx @ ChannelContext
              .Inited(cs, ChannelState.Handshake.WaitAuthResult),
            m @ (_: Ok | _: Err)
            ) =>
          val nc = ctx.copy(
            state = ChannelState.ReadyForCommand
          )
          val outgoing = None
          val emit     = Some(m)
          (nc, ChannelState.Result(outgoing, emit))
        case (
            ctx @ ChannelContext
              .Inited(
                cs,
                ChannelState.Query.WaitColumnData(columnCount, defs, rows)
              ),
            m @ (_: Ok | _: Err | _: ColumnDef)
            ) =>
          val nc = ctx.copy(
            state = ChannelState.ReadyForCommand
          )
          val outgoing = None
          val emit     = Some(m)
          (nc, ChannelState.Result(outgoing, emit))
        case _ => ???
      }
    }
  }
}
