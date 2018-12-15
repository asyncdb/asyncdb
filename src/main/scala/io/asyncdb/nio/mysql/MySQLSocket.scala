package io.asyncdb
package nio
package mysql

import cats.data.NonEmptyList
import cats.instances.vector._
import cats.syntax.all._
import cats.effect.{Async, Bracket}
import java.net.SocketAddress
import java.nio.ByteBuffer

class MySQLSocket[F[_]](ctx: NioSocket.Context[F])(
  implicit F: Async[F],
  B: Bracket[F, Throwable]
) extends NioSocket(ctx) {

  val headerBuf = ByteBuffer.allocate(4)

  private def readPacket(timeout: Long): F[Packet] = {
    val start = System.currentTimeMillis
    F.flatMap(readN(4, timeout)) { header =>
      val remain = System.currentTimeMillis - start
      val bs     = header.array
      val len    = Packet.decodeLength(bs)
      val seq    = Packet.decodeSeq(bs)
      allocator.acquire(len.value).use { buf =>
        F.map(readN(len.value, remain)) { payload =>
          Packet(len, seq, payload)
        }
      }
    }
  }

  private def readPackets(timeout: Long): F[NonEmptyList[Packet]] = {

    def readUntilEnd(to: Long): F[NonEmptyList[Packet]] = {
      val start = System.currentTimeMillis
      F.flatMap(readPacket(timeout)) { p =>
        val end = System.currentTimeMillis
        if (p.len.value < MaxPacketSize)
          F.pure(NonEmptyList.one(p))
        else
          F.map(readUntilEnd(to - (end - start))) { pn =>
            p :: pn
          }
      }
    }
    readUntilEnd(timeout)
  }

  def read[A: Reader](timeout: Long) = F.map(readPackets(timeout)) { packet =>
    Codec.read[A](packet)
  }

  def write[A: Writer](a: A, timeout: Long) = {
    def writeP(p: Packet) = {
      super.write(Packet.toBuf(p), timeout)
    }
    Codec.write(a).traverse(writeP(_))
  }
}
