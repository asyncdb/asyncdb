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

  def connect = F.async[this.type] { cb =>
    ctx.channel
      .connect(ctx.address, null, Handler(cb(Right(this)))(t => cb(Left(t))))
  }

  def write(buf: Buf, timeout: Long): F[Unit] = F.async { cb =>
    ctx.channel.write(
      buf,
      timeout,
      TimeUnit.MILLISECONDS,
      null,
      Handler(cb(Right({})))(t => cb(Left(t))))
  }

  def close(): F[Unit] = F.delay {
    ctx.channel.close()
  }

  def readN(n: Int, buf: Buf, timeout: Long): F[Buf] = {
    F.async[Buf] { cb =>
      def doRead(t: Long): Unit = t match {
        case t if t <= 0 =>
          cb(Left(Timeout(s"Cannot read ${n} after ${timeout}ms")))
        case _ =>
          val start = System.currentTimeMillis
          ctx.channel
            .read(buf, t, TimeUnit.MILLISECONDS, null, Handler[Integer, Any] {
              len: Integer =>
                val took = System.currentTimeMillis - start
                if (len == 0) {
                  doRead(t - took)
                } else if (buf.position() >= n) {
                  buf.flip()
                  cb(Right(buf))
                } else if (len == -1) {
                  cb(Left(EOF))
                } else {
                  doRead(t - took)
                }
            } { err =>
              cb(Left(err))
            })
      }
      doRead(timeout)
    }
  }
}
