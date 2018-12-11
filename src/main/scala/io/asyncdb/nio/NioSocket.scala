package io.asyncdb
package nio

import cats.syntax.all._
import cats.effect._
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.TimeUnit

private[nio] object NioSocket {
  case class Context[F[_]](
    address: SocketAddress,
    channel: ASC,
    allocator: Allocator[F]
  )
}

private[nio] abstract class NioSocket[F[_]](
  ctx: NioSocket.Context[F]
)(implicit F: Async[F])
    extends Socket[F, Buf] {

  val channel   = ctx.channel
  val allocator = ctx.allocator
  val address   = ctx.address

  def connect = F.async[this.type] { cb =>
    val ch = AsynchronousSocketChannel.open()
    channel
      .connect(address, null, Handler(cb(Right(this)))(t => cb(Left(t))))
  }

  def disconnect(): F[Unit] = F.delay {
    channel.close()
  }

  def write(buf: Buf, timeout: Long) = {
    F.async[Unit] { k =>
      channel.write(
        buf,
        timeout,
        TimeUnit.MILLISECONDS, {},
        Handler[Integer, Unit](_ => {})(_ => {})
      )
    }
  }

  def readN(n: Int, timeout: Long): F[Buf] = allocator.acquire(n).use { buf =>
    F.async[Buf] { cb =>
      def doRead(t: Long): Unit = t match {
        case t if t <= 0 =>
          cb(Left(Timeout(s"Cannot read ${n} after ${timeout}ms")))
        case _ =>
          val start = System.currentTimeMillis
          channel
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
