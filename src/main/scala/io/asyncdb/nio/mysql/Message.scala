package io.asyncdb
package nio
package mysql

trait Int3
trait Int1

case class Packet(
  length: Int @@ Int3,
  sequence: Int @@ Int1,
  payload: Buf
)
