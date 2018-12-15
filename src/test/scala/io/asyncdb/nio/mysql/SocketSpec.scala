package io.asyncdb
package nio
package mysql

import cats.effect._
import java.nio.ByteBuffer
import java.nio.channels._
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.scalatest._

abstract class SocketSpec extends AsyncFreeSpec with Matchers {
  val group = AsynchronousChannelGroup.withFixedThreadPool(
    4,
    Executors.defaultThreadFactory()
  )
  val ctx = new NioSocket.Context[IO](
    address = new InetSocketAddress("127.0.0.1", 3306),
    channel = AsynchronousSocketChannel.open(group),
    allocator = Allocator.unpooled[IO](ByteBuffer.allocate)
  )

  private def connect = new MySQLSocket(ctx).connect

  protected def withSocket[A](f: MySQLSocket[IO] => IO[A]): IO[A] = {
    Resource.make(connect)(_.disconnect).use(f)
  }
}
