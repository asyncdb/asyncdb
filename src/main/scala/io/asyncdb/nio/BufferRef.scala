package io.asyncdb
package nio

import java.nio.ByteBuffer
import cats.effect.IO

trait BufferRef {
  def get(): IO[ByteBuffer]
  def resize(size: Int): IO[ByteBuffer]
}
