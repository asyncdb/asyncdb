package io

import cats.data._
import cats.effect.IO

package object asyncdb {
  type Read[Buf, A] = Kleisli[IO, Buf, A]
  type Write[Buf, A] = Kleisli[IO, (Buf, A), Unit]
}
