package io.asyncdb
package nio
package mysql

import cats.effect.Async

case class MySQLSocketContext[F[_]](
  channel: ASC,
  headerBuf: Buf,
  payloadBuf: BufferRef[F]
) extends SocketContext

class MySQLSocket[F[_]](ctx: MySQLSocketContext[F])(implicit F: Async[F])
    extends Socket(ctx) {

  def readPacket(timeout: Long): F[Packet] = {
    val start = System.currentTimeMillis
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
}
