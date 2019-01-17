package io.asyncdb
package netty
package mysql

import cats.effect._
import io.netty.bootstrap._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio._
import java.net.InetSocketAddress
import scala.concurrent.{Future, ExecutionContext}
import scala.language.implicitConversions


abstract class SocketSpec extends Spec {

  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  implicit def effectAsFuture[A](f: IO[A]) = f.unsafeToFuture

  val config =  {
    val host: String = "127.0.0.1"
    val port: Int = 3306
    val address = new InetSocketAddress(host, port)
    val b = (new Bootstrap)
      .remoteAddress(address)
      .group(new NioEventLoopGroup())
      .channel(classOf[NioSocketChannel])
    MySQLSocketConfig(
      bootstrap = b,
      charset = CharsetMap.Utf8_general_ci,
      database = "test",
      username = "root",
      password = ""
    )
  }


  def withSocket[A](f: MySQLSocket[IO] => IO[A]): IO[A] = {


    Resource.make(MySQLSocket[IO](config).flatMap(_.connect))(_.disconnect).use(f)
  }

}
