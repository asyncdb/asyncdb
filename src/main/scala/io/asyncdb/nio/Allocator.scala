package io.asyncdb
package nio

import cats.effect._
import java.nio.ByteBuffer

trait Allocator[F[_]] {
  def acquire(size: Int): Resource[F, ByteBuffer]
}

object Allocator {
  def unpooled[F[_]](
    allocator: Int => ByteBuffer = ByteBuffer.allocate _
  )(implicit F: Sync[F]): Allocator[F] =
    new Allocator[F] {
      def acquire(size: Int): Resource[F, ByteBuffer] = {
        Resource.make(F.delay(allocator(size)))(_ => F.unit)
      }
    }
}
