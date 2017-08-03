package io

import cats.data.Kleisli
import cats.effect.IO

package object asyncdb {
  type Read[A] = Socket => IO[A]
  type Write[A] = A => Socket => IO[Unit]
}
