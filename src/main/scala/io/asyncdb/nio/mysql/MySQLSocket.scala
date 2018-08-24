package io.asyncdb
package nio
package mysql

import cats.instances.vector._
import cats.syntax.all._
import cats.effect.{Async, Bracket}
import java.net.SocketAddress
import java.nio.ByteBuffer

case class MySQLSocketContext[F[_]](
  address: SocketAddress,
  channel: ASC,
  headerBuf: Buf,
  payloadBuf: BufferPool[F]
) extends SocketContext

class MySQLSocket[F[_]](ctx: MySQLSocketContext[F])(
  implicit F: Async[F],
  B: Bracket[F, Throwable])
    extends Socket(ctx) {

  private def readPacket0(timeout: Long): F[Packet] = {
    val start = System.currentTimeMillis
    ctx.headerBuf.clear()
    F.flatMap(readN(4, ctx.headerBuf, timeout)) { header =>
      val remain = System.currentTimeMillis - start
      val bs     = header.array
      val len    = Packet.decodeLength(bs)
      val seq    = Packet.decodeSeq(bs)
      ctx.payloadBuf.acquire(len.value).use { buf =>
        F.map(readN(len.value, buf, remain)) { payload =>
          Packet(len, seq, payload)
        }
      }
    }
  }

  private def readPayload(timeout: Long): F[Vector[Packet]] = {

    def readUntilEnd(to: Long): F[Vector[Packet]] = {
      val start = System.currentTimeMillis
      F.flatMap(readPacket0(timeout)) { p =>
        val end = System.currentTimeMillis
        if (p.len.value < MaxPacketSize)
          F.pure(Vector(p))
        else
          F.map(readUntilEnd(to - (end - start))) { pn =>
            p +: pn
          }
      }
    }
    readUntilEnd(timeout)
  }

  def read[A: Reader](timeout: Long) = F.map(readPayload(timeout)) { packet =>
    Codec.read[A](packet)
  }

  def write[A: Writer](a: A, timeout: Long) = {}
}
