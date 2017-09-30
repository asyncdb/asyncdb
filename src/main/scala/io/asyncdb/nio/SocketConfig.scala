package io.asyncdb
package nio

import java.nio.channels.AsynchronousSocketChannel

case class SocketConfig(
  host: String,
  port: Int,
  timeoutMillis: Int
)

case class SocketContext(
  bufRef: BufferRef,
  channel: AsynchronousSocketChannel
)
