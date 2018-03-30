package io.asyncdb.util

object Hex {
  def toBytes(hex: String): Array[Byte] = {
    hex.grouped(2).toArray.map {
      case sub =>
        Integer.parseInt(sub, 16).toByte
    }
  }

  def fromBytes(ba: Array[Byte]) = {
    ba.foldLeft("") {
      case (r, b) =>
        r + String.format(f"${b}%02x")
    }
  }
}
