package com.giftoftheembalmer.gotefarm.server.dao

import org.springframework.transaction.support.{
  TransactionCallback,
  TransactionTemplate
}
import org.springframework.transaction.TransactionStatus

final private class RepackagedCheckedException(val e: Throwable)
  extends RuntimeException(e) {
  // don't bother filling in the stack trace here, only care about the
  // repackaged exception
  override def fillInStackTrace(): Throwable = this
}

class ScalaTransactionTemplate extends TransactionTemplate {
  private def isChecked(e: Throwable): Boolean = {
    !(e.isInstanceOf[RuntimeException] || e.isInstanceOf[Error])
  }

  def execute[T <: AnyRef](f: => T): T = {
    try {
      super.execute(new TransactionCallback {
        def doInTransaction(status: TransactionStatus) = {
          try {
            f
          }
          catch {
            case e: Throwable =>
              status.setRollbackOnly()

              // May only throw unchecked exceptions here, or else the
              // transaction will not be rolled back, or cleaned up at all.
              // Repackage the exception as an unchecked exception if
              // necessary.
              if (isChecked(e)) {
                throw new RepackagedCheckedException(e)
              }
              else {
                throw e
              }
          }
        }
      }).asInstanceOf[T]
    }
    catch {
      case e: RepackagedCheckedException =>
        throw e.e
    }
  }

  def execute(f: => Unit): Unit = {
    try {
      super.execute(new TransactionCallback {
        def doInTransaction(status: TransactionStatus) = {
          try {
            f
          }
          catch {
            case e: Throwable =>
              status.setRollbackOnly()

              // May only throw unchecked exceptions here, or else the
              // transaction will not be rolled back, or cleaned up at all.
              // Repackage the exception as an unchecked exception if
              // necessary.
              if (isChecked(e)) {
                throw new RepackagedCheckedException(e)
              }
              else {
                throw e
              }
          }
        }
      })
    }
    catch {
      case e: RepackagedCheckedException =>
        throw e.e
    }
  }
}
