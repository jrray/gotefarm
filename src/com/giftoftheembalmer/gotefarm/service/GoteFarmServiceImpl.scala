package com.giftoftheembalmer.gotefarm.service

import com.giftoftheembalmer.gotefarm.dao.GoteFarmDaoT

class GoteFarmServiceImpl extends GoteFarmServiceT {
  @scala.reflect.BeanProperty
  private var goteFarmDao: GoteFarmDaoT = null

  def generateTables() = goteFarmDao.generateTables()
  def test() = throw new UnsupportedOperationException()
}
