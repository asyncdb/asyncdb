package io.asyncdb
package netty
package mysql

import io.netty.buffer.ByteBuf

case class Packet(len: Int, seq: Int, payload: ByteBuf)
