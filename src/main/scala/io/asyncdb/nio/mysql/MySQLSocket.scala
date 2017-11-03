package io.asyncdb
package nio
package mysql

import cats.effect._

class MySQLSocket(val config: SocketConfig, val ctx: SocketContext)
    extends NioSocket[Request, Result] {
  protected def readInt1(buf: Buf, timeout: Long): IO[Int3] = ???
  protected def readInt3(buf: Buf, timeout: Long): IO[Int3] = ???
  protected def readPacket(buf: Buf, timeout: Long): IO[Packet] = {
    val buf     = ctx.bufRef.get()
    val timeout = config.readTimeout
    buf.flatMap { _buf =>
      val startTime = System.currentTimeMillis
      for {
        head <- readInt1(_buf, timeout)
        headReadTime = System.currentTimeMillis
        len <- readInt3(_buf, timeout - (headReadTime - startTime))
        lenReadTime = System.currentTimeMillis
        payload <- readN(len.value, buf, (timeout - (lenReadTime - startTime)))
      } yield Packet(head, len, payload)
    }
  }
  private val payloadRead: PayloadReader[Result]    = ???
  private val payloadWriter: PayloadWriter[Request] = ???

  def read[O] = {}
}
