package com.giftoftheembalmer.gotefarm.server.service

import org.springframework.transaction.annotation.Transactional

import com.giftoftheembalmer.gotefarm.server.dao.GoteFarmDaoT

import com.giftoftheembalmer.gotefarm.client.{
  JSEventSchedule,
  JSEventTemplate
}

import org.apache.commons.logging.LogFactory

@Transactional{val readOnly = true}
class GoteFarmServiceImpl extends GoteFarmServiceT {
  private val logger = LogFactory.getLog(this.getClass)

  @scala.reflect.BeanProperty
  private var goteFarmDao: GoteFarmDaoT = null

  @Transactional{val readOnly = false}
  def generateTables() = goteFarmDao.generateTables()

  def login(username: String, password: String) =
    goteFarmDao.validateUser(username, password)

  @Transactional{val readOnly = false}
  def newUser(username: String, email: String, password: String) =
    goteFarmDao.createUser(username, email, password)
  @Transactional{val readOnly = false}
  def newCharacter(uid: Long, realm: String, character: String) =
    goteFarmDao.createCharacter(uid, realm, character)

  def getCharacters(uid: Long) = goteFarmDao.getCharacters(uid)
  def getCharacter(cid: Long) = goteFarmDao.getCharacter(cid)

  def getRoles = goteFarmDao.getRoles
  @Transactional{val readOnly = false}
  def addRole(name: String, restricted: Boolean) =
    goteFarmDao.addRole(name, restricted)

  def getBadges = goteFarmDao.getBadges
  @Transactional{val readOnly = false}
  def addBadge(name: String, score: Int) =
    goteFarmDao.addBadge(name, score)

  @Transactional{val readOnly = false}
  def addInstance(name: String) = goteFarmDao.addInstance(name)
  @Transactional{val readOnly = false}
  def addBoss(instance: String, boss: String) =
    goteFarmDao.addBoss(instance, boss)

  def getInstances() = goteFarmDao.getInstances
  def getInstanceBosses(instance: String) =
    goteFarmDao.getInstanceBosses(instance)

  def getEventTemplate(name: String) = goteFarmDao.getEventTemplate(name)
  def getEventTemplates = goteFarmDao.getEventTemplates
  @Transactional{val readOnly = false}
  def saveEventTemplate(et: JSEventTemplate) =
    goteFarmDao.saveEventTemplate(et)

  def getEventSchedules(name: String) =
    goteFarmDao.getEventSchedules(name)
  @Transactional{val readOnly = false}
  def saveEventSchedule(es: JSEventSchedule) = {
    if (es.repeat_size < 0 || es.repeat_size > 3) {
      throw new IllegalArgumentException("Illegal repeat size")
    }
    if (es.repeat_size > 0 && es.repeat_freq < 1) {
      throw new IllegalArgumentException("Illegal repeat freq")
    }
    if (es.repeat_size == 2 && (es.day_mask & 0x7F) == 0) {
      throw new IllegalArgumentException("Illegal day mask")
    }
    if (es.repeat_size == 3 && (es.repeat_by < 0 || es.repeat_by > 1)) {
      throw new IllegalArgumentException("Illegal repeat by")
    }

    goteFarmDao.saveEventSchedule(es)
  }
}
