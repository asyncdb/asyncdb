package io.asyncdb
package nio
package mysql

import cats.effect.Async
import java.net.SocketAddress

case class MySQLSocketContext[F[_]](
  address: SocketAddress,
  channel: ASC,
  headerBuf: Buf,
  payloadBuf: BufferRef[F]
) extends SocketContext

class MySQLSocket[F[_]](ctx: MySQLSocketContext[F])(implicit F: Async[F])
    extends Socket(ctx) {

  def readPacket(timeout: Long): F[Packet] = {
    val start = System.currentTimeMillis
    ctx.headerBuf.clear()
    F.flatMap(readN(4, ctx.headerBuf, timeout)) { header =>
      val remain = System.currentTimeMillis - start
      val bs     = header.array()
      val len    = Packet.decodeLength(bs)
      val seq    = Packet.decodeSeq(bs)

      F.flatMap(ctx.payloadBuf.ensureSize(len.value)) { buf =>
        F.map(readN(len.value, buf, remain)) { payload =>
          Packet(len, seq, payload)
        }
      }
    }
  }

  def read[A: Reader](timeout: Long) = F.map(readPacket(timeout)) { packet =>
    decodeBuf[A](packet.payload)
  }
}
