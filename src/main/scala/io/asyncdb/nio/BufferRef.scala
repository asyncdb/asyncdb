package io.asyncdb
package nio

import cats.effect._
import java.util.concurrent.atomic.AtomicReference

/**
 * Buffer allocator
 */
abstract class BufferPool[F[_]: Sync] {

  /**
   * Allocate a buffer that ensures its capacity >= `size`
   */
  def take(size: Int): F[Buf]
  def offer(buf: Buf): F[Unit]
}

object BufferRef {

  /**
   * This buffer allocator cannot be used for multiple threads
   */
  def withFactory[F[_]](initSize: Int, f: Int => F[Buf])(implicit F: Sync[F]) =
    new BufferPool[F] {
      val ref             = new AtomicReference(f(initSize))
      def take(size: Int) = ???
      def offer(buf: Buf) = ???
    }
}
