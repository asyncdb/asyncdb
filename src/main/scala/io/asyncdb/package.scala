package io

import cats.data._
import cats.effect._

package object asyncdb {

  /**
   * stolen from miles' idea
   */
  private[asyncdb] type Tagged[U] = { type Tag = U }
  private[asyncdb] type @@[T, U]  = T with Tagged[U]

  type Reads[A]  = ReaderT[IO, Socket, A]
  type Writes[A] = WriterT[IO, Socket, A]

  private[asyncdb] type Buf = java.nio.ByteBuffer
}
