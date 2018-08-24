package io.asyncdb
package nio

import cats.effect.Resource
import java.nio.ByteBuffer

trait BufferPool[F[_]] {
  def acquire(size: Long): Resource[F, ByteBuffer]
}
