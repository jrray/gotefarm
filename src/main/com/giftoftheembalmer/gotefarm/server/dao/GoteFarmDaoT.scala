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
  def getAccount(key: Key): Option[Account]

  /*
  def validateUser(username: String, password: String): Long
  def createUser(username: String, email: String, password: String): Long
  */

  def getBoss(key: Key): Option[Boss]

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
  def getCharacter(key: Key): Option[Chr]

  def getChrGroup(key: Key): Option[ChrGroup]

  def getRole(key: Key): Option[Role]
  def getRoles(guild: Key): Collection[Role]

  /*
  def getRole(roleid: Long): JSRole
  */

  def addRole(guild: Key, name: String, restricted: Boolean): Role

  def getBadge(key: Key): Option[Badge]
  def getBadges(guild: Key): Collection[Badge]

  def addBadge(guild: Key, name: String, score: Int): Badge

  def addInstance(guild: Key, name: String): Instance
  def getInstances(guild: Key): Collection[Instance]

  def getEventTemplate(key: Key): Option[EventTemplate]
  def getEventTemplates(guild: Key): Collection[EventTemplate]

  def addEventTemplate(guild: Key, name: String, size: Int, min_level: Int,
                       instance: String, instance_key: Key): EventTemplate

  /*
  def getActiveEventSchedules: List[JSEventSchedule]
  */

  def getEventSchedule(key: Key): Option[EventSchedule]
  def getEventSchedules(event_template: Key): Collection[EventSchedule]
  def getGuildEventSchedules(guild: Key): Collection[EventSchedule]

  def saveEventSchedule(guild: Key, es: JSEventSchedule): EventSchedule
  /*
  def publishEvent(es: JSEventSchedule): Unit
  */

  def getEvents(guild: Key): Collection[Event]
  def getEvent(key: Key): Option[Event]
  /*
  def getEventSignup(eventsignupid: Long): JSEventSignup
  def changeEventSignup(eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit
  def removeEventSignup(eventsignupid: Long): Unit
  */
}
