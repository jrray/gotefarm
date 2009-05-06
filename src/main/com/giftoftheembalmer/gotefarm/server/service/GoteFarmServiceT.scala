package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSAccount,
  JSBoss,
  JSCharacter,
  JSBadge,
  JSEvent,
  JSEventSchedule,
  JSEventSignups,
  JSEventTemplate,
  JSInstance,
  JSGuild,
  JSRegion,
  JSRole,
  NotFoundError
}
import com.giftoftheembalmer.gotefarm.server.dao.{
  Badge,
  Chr,
  ChrClass,
  Guild,
  Race,
  Region,
  Role
}

import com.google.appengine.api.datastore.Key

import com.google.appengine.api.users.User

import java.util.{
  Date,
  List
}

trait GoteFarmServiceT {
  def getAccount(user: User): JSAccount
  @throws(classOf[NotFoundError])
  def setActiveGuild(user: User, guild: Key): JSGuild

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def getGuildFromArmoryURL(user: User, url: String): JSGuild

  /*
  @throws(classOf[InvalidCredentialsError])
  def login(username: String, password: String): Long

  @throws(classOf[AlreadyExistsError])
  def newUser(username: String, email: String, password: String): Long
  */
  def getRegions: List[JSRegion]

  @throws(classOf[NotFoundError])
  def getBadge(key: Key): Badge
  @throws(classOf[NotFoundError])
  def getChrClass(key: Key): ChrClass
  @throws(classOf[NotFoundError])
  def getRace(key: Key): Race
  @throws(classOf[NotFoundError])
  def getRole(key: Key): Role
  @throws(classOf[NotFoundError])
  def getGuild(key: Key): Guild
  @throws(classOf[NotFoundError])
  def getRegion(key: Key): Region

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  def newCharacter(user: User, guild: Key, character: String): JSCharacter

  def getCharacters(user: User, guild: Key): List[JSCharacter]

  /*
  @throws(classOf[NotFoundError])
  def getCharacter(cid: Long): JSCharacter
  */

  @throws(classOf[NotFoundError])
  def getRoles(guild: Key): List[JSRole]
  @throws(classOf[NotFoundError])
  def addRole(user: User, guild: Key, name: String, restricted: Boolean)
    : JSRole

  /*
  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def updateCharacterRole(uid: Long, cid: Long, roleid: Long, adding: Boolean): Unit
  */

  def getBadges(guild: Key): List[JSBadge]
  @throws(classOf[NotFoundError])
  def addBadge(user: User, guild: Key, name: String, score: Int): JSBadge

  /*
  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def updateCharacterBadge(uid: Long, cid: Long, badgeid: Long, adding: Boolean): Unit
  */

  @throws(classOf[NotFoundError])
  def addInstance(user: User, guild: Key, name: String): JSInstance

  @throws(classOf[NotFoundError])
  def addBoss(user: User, instance: Key, name: String): JSBoss

  @throws(classOf[NotFoundError])
  def getInstances(guild: Key): List[JSInstance]

  @throws(classOf[NotFoundError])
  def getInstanceBosses(instance: Key): List[JSBoss]

  /*
  @throws(classOf[NotFoundError])
  def getEventTemplate(name: String): JSEventTemplate
  */
  @throws(classOf[NotFoundError])
  def getEventTemplates(user: User, guild: Key): List[JSEventTemplate]

  @throws(classOf[AlreadyExistsError])
  @throws(classOf[NotFoundError])
  @throws(classOf[IllegalArgumentException])
  def saveEventTemplate(user: User, guild: Key, et: JSEventTemplate)
    : JSEventTemplate

  def getEventSchedules(user: User, event_template: Key): List[JSEventSchedule]
  /*
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

  def getTimeZones: Array[String]
}
