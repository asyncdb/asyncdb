package io

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

package object asyncdb {
  private[asyncdb] type Buf = ByteBuffer
  private[asyncdb] type ASC = AsynchronousSocketChannel
}
