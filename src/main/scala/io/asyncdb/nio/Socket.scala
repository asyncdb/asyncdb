package io.asyncdb
package nio

import cats.effect.Async
import java.nio._
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit

trait SocketContext {
  val channel: ASC
}

private[nio] abstract class Socket[F[_], I, O](ctx: SocketContext)(
  implicit F: Async[F]) {

  def write(buf: Buf, timeout: Long): F[Unit] = F.async { cb =>
    ctx.channel.write(
      buf,
      timeout,
      TimeUnit.MILLISECONDS,
      null,
      new CompletionHandler[Integer, Any] {
        def completed(n: Integer, x: Any) = {
          cb(Right({}))
        }

        def failed(t: Throwable, x: Any) = {
          cb(Left(t))
        }
      })
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
            new CompletionHandler[Integer, Any] {

              def completed(len: Integer, x: Any) = {
                if (buf.remaining() >= n) {
                  cb(Right(buf.slice()))
                } else if (len == -1) {
                  cb(Left(EOF))
                } else {
                  val took = System.currentTimeMillis - start
                  doRead(t - took)
                }
              }
              def failed(t: Throwable, a: Any) = {
                cb(Left(t))
              }
            })
      }
      doRead(timeout)
    }
  }
}
