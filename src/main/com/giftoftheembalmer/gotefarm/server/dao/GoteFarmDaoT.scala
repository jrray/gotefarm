package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  JSBadge,
  JSCharacter,
  JSEvent,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSRole
}

import com.google.appengine.api.datastore.Key
import com.google.appengine.api.users.User

import java.util.{
  Collection,
  Date,
  List,
  Set
}

trait GoteFarmDaoT {
  def getAccount(user: User): Account

  /*
  def validateUser(username: String, password: String): Long
  def createUser(username: String, email: String, password: String): Long
  */

  def getRegions: Collection[Region]

  def getChrClass(key: Key): Option[ChrClass]
  def getChrClass(clazz: String): ChrClass

  def getInstance(key: Key): Option[Instance]

  def getRace(key: Key): Option[Race]
  def getRace(race: String): Race

  def getRegion(key: Key): Option[Region]
  def getRegion(code: String): Region

  def getRealm(key: Key): Option[Realm]
  def getRealm(region: String, name: String): Realm

  def getGuild(key: Key): Option[Guild]
  def getGuild(region: String, realm: String, name: String, account: Key)
    : Guild

  def getCharacter(guild: Key, name: String): Option[Chr]
  def createCharacter(user: User, guild: Key, character: String, race: Race,
                      clazz: ChrClass, level: Int, chrxml: String): Chr

  def getCharacters(user: User, guild: Key): Collection[Chr]
  /*
  def getCharacter(cid: Long): JSCharacter

  def getRoles: List[JSRole]
  def getRole(roleid: Long): JSRole
  def addRole(name: String, restricted: Boolean): Long
  def updateCharacterRole(cid: Long, roleid: Long, adding: Boolean): Unit

  def getBadges: List[JSBadge]
  def addBadge(name: String, score: Int): Long
  def updateCharacterBadge(cid: Long, badgeid: Long, adding: Boolean): Unit
  */

  def addInstance(guild: Key, name: String): Instance

  /*
  def addBoss(instance: String, boss: String): Long
  */

  def getInstances(guild: Key): Collection[Instance]

  /*
  def getInstanceBosses(instance: String): List[String]

  def getEventTemplate(name: String): JSEventTemplate
  def getEventTemplates: List[JSEventTemplate]
  def saveEventTemplate(et: JSEventTemplate): Long

  def getActiveEventSchedules: List[JSEventSchedule]
  def getEventSchedules(name: String): List[JSEventSchedule]
  def saveEventSchedule(es: JSEventSchedule): Long
  def publishEvent(es: JSEventSchedule): Unit

  def getEvents: List[JSEvent]
  def getEvent(eventid: Long): JSEvent
  def getEventSignups(eventid: Long,
                      if_changed_since: Date): Option[JSEventSignups]
  def getEventSignup(eventsignupid: Long): JSEventSignup
  def signupForEvent(eventid: Long, cid: Long, roleid: Long,
                     signup_type: Int): Unit
  def changeEventSignup(eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit
  def removeEventSignup(eventsignupid: Long): Unit
  */
}
