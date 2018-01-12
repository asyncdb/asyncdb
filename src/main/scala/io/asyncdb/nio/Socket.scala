package io.asyncdb
package nio

import cats.Eval
import cats.effect.Async
import java.net.SocketAddress
import java.nio._
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit

trait SocketContext {
  val channel: ASC
  val address: SocketAddress
}

private[nio] abstract class Socket[F[_], I, O](ctx: SocketContext)(
  implicit F: Async[F]) {

  def connect = F.async[ASC] { cb =>
    ctx.channel.connect(
      ctx.address,
      null,
      Handler(cb(Right(ctx.channel)))(t => cb(Left(t))))
  }

  def write(buf: Buf, timeout: Long): F[Unit] = F.async { cb =>
    ctx.channel.write(
      buf,
      timeout,
      TimeUnit.MILLISECONDS,
      null,
      Handler(cb(Right({})))(t => cb(Left(t))))
  }

  def readN(n: Int, buf: Buf, timeout: Long): F[Buf] = {

    F.async[Buf] { cb =>
      def doRead(t: Long): Unit = t match {
        case t if t <= 0 =>
          cb(Left(Timeout(s"Cannot read ${n} after ${timeout}ms")))
        case _ =>
          val start = System.currentTimeMillis
          ctx.channel.read(
            buf,
            timeout,
            TimeUnit.MILLISECONDS,
            null,
            Handler[Integer] { len: Integer =>
              if (buf.remaining() >= n) {
                cb(Right(buf))
              } else if (len == -1) {
                cb(Left(EOF))
              } else {
                val took = System.currentTimeMillis - start
                doRead(t - took)
              }
            } { err =>
              cb(Left(err))
            }
          )
      }
      doRead(timeout)
    }
  }
}
