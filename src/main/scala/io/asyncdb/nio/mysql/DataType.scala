package io.asyncdb
package nio
package mysql

import java.nio.{ByteBuffer, ByteOrder}
import scala.util.control.NonFatal

case class Int1(val value: Byte)                      extends AnyVal
case class Int2(val value: Int)                       extends AnyVal
case class Int3(val value: Int)                       extends AnyVal
case class IntLE(val value: Int)                      extends AnyVal
case class LenencInt(val value: Int)                  extends AnyVal
case class NullDelimitedBytes(val value: Array[Byte]) extends AnyVal
case class NullDelimitedUTF8String(val value: String) extends AnyVal

trait BasicCodecs {

  private[mysql] def readNBytes(buf: BufView, n: Int) = tryExec {
    val out = Array.ofDim[Byte](n)
    buf.get(out)
    out
  }

  private[mysql] def tryExec[A](a: => A): Either[Throwable, A] = {
    try (Right(a))
    catch {
      case NonFatal(e) => Left(e)
    }
  }

  implicit object intReader extends Reader[IntLE] {
    def read(buf: BufView): Either[Throwable, IntLE] = tryExec {
      val ba = Array.ofDim[Byte](4)
      buf.get(ba)
      val i = ByteBuffer.wrap(ba).order(ByteOrder.LITTLE_ENDIAN).getInt()
      new IntLE(i)
    }
  }

  implicit object int1Reader extends Reader[Int1] {
    def read(buf: BufView): Either[Throwable, Int1] = tryExec {
      new Int1(buf.get())
    }
  }

  implicit object int2Reader extends Reader[Int2] {
    def read(buf: BufView): Either[Throwable, Int2] = tryExec {
      val ba = new Array[Byte](2)
      buf.get(ba)
      val r = b2i(ba(0)) | (b2i(ba(1)) << 8)
      new Int2(r)
    }
  }

  implicit object int3Reader extends Reader[Int3] {
    def read(buf: BufView): Either[Throwable, Int3] = {
      val ba = new Array[Byte](3)
      try {
        buf.get(ba)
        val r = b2i(ba(0)) | (b2i(ba(1)) << 8) | (b2i(ba(2)) << 16)
        Right(new Int3(r))
      } catch {
        case e: Throwable =>
          Left(e)
      }
    }
  }

  implicit object nullBytesReader extends Reader[NullDelimitedBytes] {
    def read(buf: BufView): Either[Throwable, NullDelimitedBytes] = {
      try {
        Right(new NullDelimitedBytes(buf.takeThrough(_ != 0x00).init))
      } catch {
        case e: Throwable =>
          Left(e)
      }
    }
  }

  private def b2i(b: Byte): Int = {
    b.toInt & 0xff
  }
}

private[mysql] object BasicCodecs extends BasicCodecs
