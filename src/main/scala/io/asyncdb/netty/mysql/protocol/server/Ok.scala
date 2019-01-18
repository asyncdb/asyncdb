package io.asyncdb
package netty
package mysql
package protocol
package server

case class Ok(
  header: Byte,
  affectedRows: Long,
  lastInsertId: Long,
  statusFlag: Int,
  warnings: Int,
  info: String
) extends Message

object Ok {
  import Decoder._
  implicit val okDecoder: Decoder[Ok] = {
    (int1 :: lenencInt :: lenencInt :: intL2 :: intL2 :: strEOF).as[Ok]
  }
}
