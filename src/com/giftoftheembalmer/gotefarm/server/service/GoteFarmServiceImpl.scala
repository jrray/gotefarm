package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.server.dao.GoteFarmDaoT

import com.giftoftheembalmer.gotefarm.client.JSEventTemplate

class GoteFarmServiceImpl extends GoteFarmServiceT {
  @scala.reflect.BeanProperty
  private var goteFarmDao: GoteFarmDaoT = null

  def generateTables() = goteFarmDao.generateTables()

  def login(username: String, password: String) =
    goteFarmDao.validateUser(username, password)
  def newUser(username: String, email: String, password: String) =
    goteFarmDao.createUser(username, email, password)
  def newCharacter(uid: Int, realm: String, character: String) =
    goteFarmDao.createCharacter(uid, realm, character)

  def getCharacters(uid: Int) = goteFarmDao.getCharacters(uid)
  def getCharacter(cid: Int) = goteFarmDao.getCharacter(cid)

  def getRoles = goteFarmDao.getRoles
  def addRole(name: String, restricted: Boolean) =
    goteFarmDao.addRole(name, restricted)

  def getBadges = goteFarmDao.getBadges
  def addBadge(name: String, score: Int) =
    goteFarmDao.addBadge(name, score)

  def addInstance(name: String) = goteFarmDao.addInstance(name)
  def addBoss(instance: String, boss: String) =
    goteFarmDao.addBoss(instance, boss)

  def getInstances() = goteFarmDao.getInstances
  def getInstanceBosses(instance: String) =
    goteFarmDao.getInstanceBosses(instance)

  def getEventTemplate(name: String) = goteFarmDao.getEventTemplate(name)
  def getEventTemplates = goteFarmDao.getEventTemplates
  def saveEventTemplate(et: JSEventTemplate) =
    goteFarmDao.saveEventTemplate(et)

  def getEventSchedules(name: String) =
    goteFarmDao.getEventSchedules(name)

}
