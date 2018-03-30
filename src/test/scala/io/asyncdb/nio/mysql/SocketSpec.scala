package io.asyncdb
package nio
package mysql

import cats.effect.IO
import java.nio.ByteBuffer
import java.nio.channels._
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.scalatest._

abstract class SocketSpec extends AsyncFreeSpec with Matchers {
  val group = AsynchronousChannelGroup.withFixedThreadPool(
    4,
    Executors.defaultThreadFactory())
  val ctx = new MySQLSocketContext[IO](
    address = new InetSocketAddress("127.0.0.1", 3306),
    channel = AsynchronousSocketChannel.open(group),
    headerBuf = ByteBuffer.allocate(4),
    payloadBuf =
      BufferRef.withFactory(128 * 1024, s => IO.pure(ByteBuffer.allocate(s)))
  )

  def socket = new MySQLSocket(ctx)
}
