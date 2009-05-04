package com.giftoftheembalmer.gotefarm.server.dao

import org.springframework.transaction.support.{
  TransactionCallback,
  TransactionTemplate
}
import org.springframework.transaction.TransactionStatus

class ScalaTransactionTemplate extends TransactionTemplate {
  def execute[T <: AnyRef](f: => T): T = {
    super.execute(new TransactionCallback {
      def doInTransaction(status: TransactionStatus) = {
        f
      }
    }).asInstanceOf[T]
  }

  def execute(f: => Unit): Unit = {
    super.execute(new TransactionCallback {
      def doInTransaction(status: TransactionStatus) = {
        f
      }
    })
  }
}
