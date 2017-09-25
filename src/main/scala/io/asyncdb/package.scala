package io

import cats.data._
import cats.effect._

package object asyncdb {
  type Reads[A] = ReaderT[IO, Socket, A]
  type Writes[A] = WriterT[IO, Socket, A]
}
