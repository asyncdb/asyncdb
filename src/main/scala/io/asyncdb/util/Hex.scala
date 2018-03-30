package io.asyncdb.util

object Hex {
  def toBytes(hex: String): Array[Byte] = {
    hex.grouped(2).toArray.flatMap {
      case sub =>
        Array(
          Integer.parseInt(sub(0).toString, 16).toByte,
          Integer.parseInt(sub(1).toString, 16).toByte)
    }
  }

  def fromBytes(ba: Array[Byte]) = {
    ba.foldLeft("") {
      case (r, b) =>
        r + String.format(f"${b}%02x")
    }
  }
}
