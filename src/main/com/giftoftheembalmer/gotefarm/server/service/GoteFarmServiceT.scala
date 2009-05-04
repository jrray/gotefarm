package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSAccount,
  JSCharacter,
  JSBadge,
  JSEvent,
  JSEventSchedule,
  JSEventSignups,
  JSEventTemplate,
  JSRegion,
  JSRole,
  NotFoundError
}
import com.giftoftheembalmer.gotefarm.server.dao.{
  Chr,
  ChrClass,
  Race
}

import com.google.appengine.api.datastore.Key

import com.google.appengine.api.users.User

import java.util.{
  Date,
  List
}

trait GoteFarmServiceT {
  def getAccount(user: User): JSAccount

  /*
  @throws(classOf[InvalidCredentialsError])
  def login(username: String, password: String): Long

  @throws(classOf[AlreadyExistsError])
  def newUser(username: String, email: String, password: String): Long
  */
  def getRegions: List[JSRegion]

  @throws(classOf[NotFoundError])
  def getChrClass(key: Key): ChrClass
  @throws(classOf[NotFoundError])
  def getRace(key: Key): Race

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def newCharacter(user: User, realm: String, character: String): JSCharacter

  def getCharacters(user: User): List[JSCharacter]

  /*
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
  @throws(classOf[NotFoundError])
  @throws(classOf[AlreadyExistsError])
  @throws(classOf[IllegalArgumentException])
  def signupForEvent(uid: Long, eventid: Long, cid: Long, roleid: Long,
                     signup_type: Int): Unit
  @throws(classOf[NotFoundError])
  @throws(classOf[AlreadyExistsError])
  @throws(classOf[IllegalArgumentException])
  def changeEventSignup(uid: Long, eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit
  @throws(classOf[NotFoundError])
  @throws(classOf[AlreadyExistsError])
  @throws(classOf[IllegalArgumentException])
  def removeEventSignup(uid: Long, eventsignupid: Long): Unit

  def publishEvents(): Unit
  */
}
