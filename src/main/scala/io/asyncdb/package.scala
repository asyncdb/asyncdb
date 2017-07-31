package io

import cats.data.Kleisli
import cats.effect.IO

package object asyncdb {
  type Decoder[A] = Kleisli[IO, Socket, A]
}
