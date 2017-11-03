package io.asyncdb
package nio

import java.nio.channels.AsynchronousSocketChannel

case class SocketConfig(
  host: String,
  port: Int,
  readTimeout: Int
)

case class SocketContext(
  bufRef: BufferRef,
  channel: AsynchronousSocketChannel
)
