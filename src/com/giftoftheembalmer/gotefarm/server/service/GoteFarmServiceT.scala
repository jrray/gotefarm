package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSBadge,
  JSEvent,
  JSEventSchedule,
  JSEventSignups,
  JSEventTemplate,
  JSCharacter,
  JSRole,
  NotFoundError
}

import java.util.{
  Date,
  List
}

trait GoteFarmServiceT {
  def generateTables(): Unit

  @throws(classOf[InvalidCredentialsError])
  def login(username: String, password: String): Long

  @throws(classOf[AlreadyExistsError])
  def newUser(username: String, email: String, password: String): Long

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def newCharacter(uid: Long, realm: String, character: String): Long

  def getCharacters(uid: Long): List[JSCharacter]

  @throws(classOf[NotFoundError])
  def getCharacter(cid: Long): JSCharacter

  def getRoles: List[JSRole]
  @throws(classOf[AlreadyExistsError])
  def addRole(name: String, restricted: Boolean): Long

  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def updateCharacterRole(uid: Long, cid: Long, roleid: Long, adding: Boolean): Unit

  def getBadges: List[JSBadge]
  @throws(classOf[AlreadyExistsError])
  def addBadge(name: String, score: Int): Long

  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def updateCharacterBadge(uid: Long, cid: Long, badgeid: Long, adding: Boolean): Unit

  @throws(classOf[AlreadyExistsError])
  def addInstance(name: String): Long

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def addBoss(instance: String, name: String): Long

  def getInstances: List[String]

  def getInstanceBosses(instance: String): List[String]

  @throws(classOf[NotFoundError])
  def getEventTemplate(name: String): JSEventTemplate
  def getEventTemplates: List[JSEventTemplate]
  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def saveEventTemplate(et: JSEventTemplate): Long

  def getEventSchedules(name: String): List[JSEventSchedule]
  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def saveEventSchedule(es: JSEventSchedule): Long

  def getEvents: List[JSEvent]
  @throws(classOf[NotFoundError])
  def getEventSignups(eventid: Long,
                      if_changed_since: Date): Option[JSEventSignups]

  def publishEvents(): Unit
}
