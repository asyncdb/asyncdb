package io.asyncdb
package nio

import cats.effect.IO
import java.nio._
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit

sealed trait NioError extends Exception
case object EOF       extends NioError
case object Timeout   extends NioError

class NioSocket(config: SocketConfig, ctx: SocketContext) extends Socket {

  private def withBuf[A](f: ByteBuffer => IO[A]) = {
    ctx.bufRef.get().flatMap(f)
  }

  def readBytes(n: Int, timeout: Long): IO[Array[Byte]] = withBuf { buf =>
    val start = System.currentTimeMillis
    IO.async[Array[Byte]] { cb =>
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
                  val arr = new Array[Byte](n)
                  buf.get(arr)
                  cb(Right(arr))
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
