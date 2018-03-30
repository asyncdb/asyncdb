package io.asyncdb
package nio

import cats.effect._
import java.util.concurrent.atomic.AtomicReference

/**
 * Buffer allocator
 */
abstract class BufferRef[F[_]: Sync] {

  /**
   * Allocate a buffer that ensures its capacity >= `size`
   */
  def ensureSize(size: Int): F[Buf]
}

object BufferRef {

  /**
   * This buffer allocator cannot be used for multiple threads
   */
  def withFactory[F[_]](initSize: Int, f: Int => F[Buf])(implicit F: Sync[F]) =
    new BufferRef[F] {
      val ref = new AtomicReference(f(initSize))
      def ensureSize(size: Int) = F.flatMap(ref.get()) { previous =>
        if (previous.capacity >= size) {
          F.pure(previous.clear())
        } else {
          val newBuf =
            f(math.max(size, previous.capacity + previous.capacity / 2))
          ref.set(newBuf)
          newBuf
        }
      }
    }
}
