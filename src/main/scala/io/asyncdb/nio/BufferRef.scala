package io.asyncdb
package nio

import cats.effect._

abstract class BufferRef[F[_]: Sync] {
  def ensureSize(size: Int): F[Buf]
}
