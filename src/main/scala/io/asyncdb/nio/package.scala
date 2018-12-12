package io.asyncdb

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

package object nio {
  private[nio] type Buf = ByteBuffer
  private[nio] type ASC = AsynchronousSocketChannel
}
