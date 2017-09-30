package io.asyncdb
package nio

import cats.effect.IO
import java.nio._
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit

sealed trait NioError extends Exception
case object EOF       extends NioError
case object Timeout   extends NioError

trait NioSocket extends Socket {

  val config: SocketConfig
  val ctx: SocketContext

  def readN(n: Int, buf: Buf, timeout: Long): IO[Buf] = {
    val start = System.currentTimeMillis
    IO.async[Buf] { cb =>
      def doRead(t: Long): Unit = t match {
        case t if t <= 0 =>
          cb(Left(Timeout))
        case _ =>
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

  private def withBuf[A](f: ByteBuffer => IO[A]) = {
    ctx.bufRef.get().flatMap(f)
  }
}
