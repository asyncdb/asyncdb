package io.asyncdb
package nio

import cats.syntax.all._
import cats.effect._
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.TimeUnit

import io.asyncdb.util.Hex

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
    println("-------")
    println(Hex.fromBytes(buf.array()))
//    val t =
//      "5500000108a20a00ffffff00530000000000000000000000000000000000000000000000746573740014acf9e19c917a181a945cc051823c7de45d806ded74657374006d7973716c5f6e61746976655f70617373776f726400"
//    val b  = Hex.toBytes(t)
//    val bu = ByteBuffer.wrap(b)
    val bu = ByteBuffer.wrap(buf.array())
    F.async[Unit] { k =>
      channel.write(
        bu,
        timeout,
        TimeUnit.MILLISECONDS, {},
        Handler[Integer, Unit](_ => k(Right({})))(e => k(Left(e)))
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
