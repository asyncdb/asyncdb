package io.asyncdb.netty.mysql.protocol.server

case class ColumnDefinition(
  val catalog: String,
  val schema: String,
  val table: String,
  val originalTable: String,
  val name: String,
  val originalName: String,
  val characterSet: Int,
  val columnLength: Long,
  val columnType: Int,
  val flags: Short,
  val decimals: Byte
)
