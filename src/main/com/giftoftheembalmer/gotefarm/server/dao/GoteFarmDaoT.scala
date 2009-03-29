package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  JSBadge,
  JSEvent,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSCharacter,
  JSRole
}

import java.util.{
  Date,
  List
}

trait GoteFarmDaoT {
  def generateTables(): Unit

  def validateUser(username: String, password: String): Long
  def createUser(username: String, email: String, password: String): Long
  def createCharacter(uid: Long, realm: String, character: String): Long

  def getCharacters(uid: Long): List[JSCharacter]
  def getCharacter(cid: Long): JSCharacter

  def getRoles: List[JSRole]
  def getRole(roleid: Long): JSRole
  def addRole(name: String, restricted: Boolean): Long
  def updateCharacterRole(cid: Long, roleid: Long, adding: Boolean): Unit

  def getBadges: List[JSBadge]
  def addBadge(name: String, score: Int): Long
  def updateCharacterBadge(cid: Long, badgeid: Long, adding: Boolean): Unit

  def addInstance(name: String): Long
  def addBoss(instance: String, boss: String): Long

  def getInstances: List[String]
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
}
