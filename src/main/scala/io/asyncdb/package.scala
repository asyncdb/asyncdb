package io

import cats.data._
import cats.effect._

package object asyncdb {

  class Int1(val value: Byte) extends AnyVal
  class Int3(val value: Int)  extends AnyVal

  type Reads[A]  = ReaderT[IO, Socket, A]
  type Writes[A] = WriterT[IO, Socket, A]

  private[asyncdb] type Buf = java.nio.ByteBuffer
}
