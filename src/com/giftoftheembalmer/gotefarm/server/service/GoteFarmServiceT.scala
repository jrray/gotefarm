package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSEventSchedule,
  JSEventTemplate,
  JSCharacter,
  JSRole,
  NotFoundError
}

import java.util.List

trait GoteFarmServiceT {
  def generateTables(): Unit

  @throws(classOf[InvalidCredentialsError])
  def login(username: String, password: String): Int

  @throws(classOf[AlreadyExistsError])
  def newUser(username: String, email: String, password: String): Int

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def newCharacter(uid: Int, realm: String, character: String): Int

  def getCharacters(uid: Int): List[JSCharacter]

  @throws(classOf[NotFoundError])
  def getCharacter(cid: Int): JSCharacter

  def getRoles: List[JSRole]
  @throws(classOf[AlreadyExistsError])
  def addRole(name: String, restricted: Boolean): Int

  def getBadges: List[String]
  @throws(classOf[AlreadyExistsError])
  def addBadge(name: String, score: Int): Int

  @throws(classOf[AlreadyExistsError])
  def addInstance(name: String): Int

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def addBoss(instance: String, name: String): Int

  def getInstances: List[String]

  def getInstanceBosses(instance: String): List[String]

  @throws(classOf[NotFoundError])
  def getEventTemplate(name: String): JSEventTemplate
  def getEventTemplates: List[String]
  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def saveEventTemplate(et: JSEventTemplate): Int

  def getEventSchedules(name: String): List[JSEventSchedule]
}
