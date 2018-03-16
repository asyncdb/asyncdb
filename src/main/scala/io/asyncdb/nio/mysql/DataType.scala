package io.asyncdb
package nio
package mysql

class Int1(val value: Byte)             extends AnyVal
class Int2(val value: Int)              extends AnyVal
class Int3(val value: Int)              extends AnyVal
class LenencInt(val value: Int)         extends AnyVal
class NullBytes(val value: Array[Byte]) extends AnyVal

object Codecs {

  implicit object int1Reader extends Reader[Int1] {
    def read(buf: Buf): Either[Throwable, Int1] = {
      try {
        Right(new Int1(buf.get()))
      } catch {
        case e: Throwable =>
          Left(e)
      }
    }
  }

  implicit object int2Reader extends Reader[Int2] {
    def read(buf: Buf): Either[Throwable, Int2] = {
      val ba = new Array[Byte](2)
      try {
        buf.get(ba)
        val r = b2i(ba(0)) | (b2i(ba(1)) << 8)
        Right(new Int2(r))
      } catch {
        case e: Throwable =>
          Left(e)
      }
    }
  }

  implicit object int3Reader extends Reader[Int3] {
    def read(buf: Buf): Either[Throwable, Int3] = {
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

  private def b2i(b: Byte): Int = {
    b.toInt & 0xff
  }
}
