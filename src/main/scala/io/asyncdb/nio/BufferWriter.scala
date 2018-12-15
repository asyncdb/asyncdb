package io.asyncdb.nio

import java.nio.ByteBuffer

trait BufferWriter {
  def writeInt(value: Int)
  def writeByte(value: Int)
  def writeBytes(value: Array[Byte])
  def array: Array[Byte]
  def value: ByteBuffer
}

object BufferWriter {
  def apply(size: Int): BufferWriter = apply(ByteBuffer.allocate(size))
  def apply(buf: Buf) = new BufferWriter {
    val init = {
      buf.duplicate()
      buf.putInt(0) //是否预留4个字节用于长度和序号？还是拼装一个新的buffer
    }

    def writeInt(value: Int) = {
      buf.putInt(value)
    }
    def writeByte(value: Int) = {
      buf.put(value.toByte)
    }
    def writeBytes(value: Array[Byte]) = {
      buf.put(value)
    }
    def array() = init.array()

    def value = buf
  }
}
