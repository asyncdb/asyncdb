package io.asyncdb
package nio
package mysql

import cats._
import cats.data.NonEmptyList
import cats.syntax.all._
import scala.util._

trait Reader[A] {
  def read(buf: NonEmptyList[Packet]): Either[Throwable, A]
}

trait Writer[A] {
  def write(a: A): NonEmptyList[Packet]
}

object Reader {

  implicit val readerMonadError: MonadError[Reader, Throwable] =
    new MonadError[Reader, Throwable] with StackSafeMonad[Reader] {
      def pure[A](a: A) = Codec.reader(buf => a)
      def raiseError[A](e: Throwable) = new Reader[A] {
        def read(buf: NonEmptyList[Packet]) = Left(e)
      }
      def handleErrorWith[A](fa: Reader[A])(f: Throwable => Reader[A]) =
        new Reader[A] {
          def read(buf: NonEmptyList[Packet]) = {
            fa.read(buf).recoverWith {
              case e: Throwable => f(e).read(buf)
            }
          }
        }

      def flatMap[A, B](fa: Reader[A])(f: A => Reader[B]) = new Reader[B] {
        def read(buf: NonEmptyList[Packet]) = {
          for {
            a <- fa.read(buf)
            b <- f(a).read(buf)
          } yield b
        }
      }
    }

  //Those methods are not wrapped with either, to reduce memory allocation probablly,
  //Calls while be wrapped with either inside Codec.reader
  object Unsafe {

    def readUInt1(buf: BufferReader) = {
      UInt1((buf.get & 0xff).toShort)
    }

    def readInt1(buf: BufferReader) = Int1(buf.get)

    def readInt2(buf: BufferReader) = {
      val bytes = buf.take(2)
      val v     = bytes(0).toInt & 0xff | (bytes(1).toInt & 0xff << 8)
      Int2(v)
    }

    def readInt3(buf: BufferReader) = {
      val bytes = buf.take(3)
      val v = (bytes(0).toInt & 0xff) | ((bytes(1).toInt & 0xff) << 8) | ((bytes(
        2).toInt & 0xff) << 16)
      Int3(v)
    }

    def readIntLE(buf: BufferReader) = {
      val bytes = buf.take(4)
      val v = (bytes(0).toInt & 0xff) | ((bytes(1).toInt & 0xff) << 8) | ((bytes(
        2).toInt & 0xff) << 16) | (bytes(3).toInt & 0xff) << 24
      IntLE(v)
    }

    def readN(buf: BufferReader, n: Int) = buf.take(n)

    def readNullEnded(buf: BufferReader) = {
      val v = buf.takeWhile(_ != '\u0000')
      buf.get
      v
    }
  }
}

object Codec {

  def reader[A](f: BufferReader => A): Reader[A] =
    new Reader[A] {
      def read(packets: NonEmptyList[Packet]) = {
        val buf: BufferReader = packets.map { p =>
          BufferReader(p.payload)
        }.reduceLeft(_ ++ _)
        Either.catchNonFatal(f(buf))
      }
    }

  def read[A](buf: NonEmptyList[Packet])(
    implicit reader: Reader[A]): Either[Throwable, A] = reader.read(buf)

  def write[A](a: A)(implicit writer: Writer[A]) =
    writer.write(a)

}
