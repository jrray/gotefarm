package com.giftoftheembalmer.gotefarm.server.dao

import org.springframework.orm.jdo.JdoCallback
import org.springframework.orm.jdo.support.JdoDaoSupport
import org.springframework.orm.ObjectRetrievalFailureException

import javax.jdo.PersistenceManager

import java.util.Collection

trait ScalaJdoDaoSupport extends JdoDaoSupport {
  def find[T](entityClass: Class[T]) = {
    getJdoTemplate.find(entityClass).asInstanceOf[Collection[T]]
  }

  def find[T](entityClass: Class[T], filter: String, parameters: String)
             (values: AnyRef*): Collection[T] = {
    getJdoTemplate.find(entityClass, filter, parameters,
                        values.toArray).asInstanceOf[Collection[T]]
  }

  def find[T](entityClass: Class[T], filter: String, parameters: String,
              ordering: String)
             (values: AnyRef*): Collection[T] = {
    getJdoTemplate.find(entityClass, filter, parameters,
                        values.toArray, ordering).asInstanceOf[Collection[T]]
  }

  def getObjectById[T](entityClass: Class[T], idValue: AnyRef): Option[T] = {
    try {
      Some(getJdoTemplate.getObjectById(entityClass, idValue).asInstanceOf[T])
    }
    catch {
      case _: ObjectRetrievalFailureException =>
        None
    }
  }

  def executeFind[T](f: PersistenceManager => AnyRef): Collection[T] = {
    getJdoTemplate.executeFind(new JdoCallback {
      override
      def doInJdo(pm: PersistenceManager): AnyRef = {
        f(pm)
      }
    }).asInstanceOf[Collection[T]]
  }
}
