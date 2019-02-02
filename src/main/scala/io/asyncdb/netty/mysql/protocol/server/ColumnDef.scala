package io.asyncdb
package netty
package mysql
package protocol
package server

case class ColumnDef(
  catalog: String,
  schema: String,
  table: String,
  originalTable: String,
  name: String,
  originalName: String,
  characterSet: Byte,
  columnLength: Int,
  columnType: Byte,
  flags: Int,
  decimals: Byte
) extends ServerMessage

object ColumnDef {
  import Decoder._
  implicit val columnDefDecoder: Decoder[ColumnDef] = {
    (strLen :: strLen :: strLen :: strLen :: strLen :: strLen :: int1 :: intL4 :: int1 :: intL2 :: int1)
      .as[ColumnDef]
  }
}
