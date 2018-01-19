package io.asyncdb
package nio
package mysql

class Int1(val value: Byte)             extends AnyVal
class Int2(val value: Short)            extends AnyVal
class Int3(val value: Int)              extends AnyVal
class LenencInt(val value: Int)         extends AnyVal
class NullBytes(val value: Array[Byte]) extends AnyVal

trait DataTypeCodecs {}
