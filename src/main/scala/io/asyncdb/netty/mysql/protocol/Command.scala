package io.asyncdb.netty.mysql.protocol

object Command {

  object Client {
    final val ClientProtocolVersion         = 0x09 // COM_STATISTICS
    final val Quit                          = 0x01 // COM_QUIT
    final val Query                         = 0x03 // COM_QUERY
    final val PreparedStatementPrepare      = 0x16 // COM_STMT_PREPARE
    final val PreparedStatementExecute      = 0x17 // COM_STMT_EXECUTE
    final val PreparedStatementSendLongData = 0x18 // COM_STMT_SEND_LONG_DATA
    final val AuthSwitchResponse            = 0xfe // AuthSwitchRequest
  }

  object Server {
    final val OK  = 0
    final val ERR = -1
    final val EOF = -2
  }
}
