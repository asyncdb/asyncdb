package io.asyncdb
package nio
package mysql

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

  def readPacket0(timeout: Long): F[Packet] = {
    val start = System.currentTimeMillis
    ctx.headerBuf.clear()
    F.flatMap(readN(4, ctx.headerBuf, timeout)) { header =>
      val remain = System.currentTimeMillis - start
      val bs     = header.array()
      val len    = Packet.decodeLength(bs)
      val seq    = Packet.decodeSeq(bs)

      B.bracket(ctx.payloadBuf.take(len.value))(buf => {
        F.map(readN(len.value, buf, remain)) { payload =>
          Packet(len, seq, payload)
        }
      })(buf => ctx.payloadBuf.offer(buf))
    }
  }

  def readPayload(timeout: Long): F[BufView] = {

    def readUntilEnd(to: Long): F[BufView] = {
      val start = System.currentTimeMillis
      F.flatMap(readPacket0(timeout)) { p =>
        val end = System.currentTimeMillis
        if (p.payload.remaining() < MaxPacketSize)
          F.pure(BufView(p.payload))
        else
          F.map(readUntilEnd(to - (end - start))) { pn =>
            BufView.composite(BufView(p.payload), pn)
          }
      }
    }
    readUntilEnd(timeout)
  }

  def read[A: Reader](timeout: Long) = F.map(readPayload(timeout)) { packet =>
    decodeBuf[A](packet)
  }
}
