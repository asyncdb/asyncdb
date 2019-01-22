package io.asyncdb
package netty
package mysql

object Packet {
  final val MaxSize       = 0x00ffffff
  final val PacketLength  = 4
  final val CommandLength = 1
}
