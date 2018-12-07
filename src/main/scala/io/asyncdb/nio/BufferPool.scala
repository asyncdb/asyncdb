package io.asyncdb
package nio

import cats.effect._
import java.nio.ByteBuffer

trait BufferPool[F[_]] {
  def acquire(size: Int): Resource[F, ByteBuffer]
}

object BufferPool {
  def unpooled[F[_]](allocator: Int => ByteBuffer = ByteBuffer.allocate _)(
    implicit F: Sync[F]): BufferPool[F] =
    new BufferPool[F] {
      def acquire(size: Int): Resource[F, ByteBuffer] = {
        Resource.make(F.delay(allocator(size)))(_ => F.unit)
      }
    }
}
