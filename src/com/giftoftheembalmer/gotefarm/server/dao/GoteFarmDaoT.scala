package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  JSEventSchedule,
  JSEventTemplate,
  JSCharacter,
  JSRole
}

import java.util.List

trait GoteFarmDaoT {
  def generateTables(): Unit

  def validateUser(username: String, password: String): Long
  def createUser(username: String, email: String, password: String): Long
  def createCharacter(uid: Long, realm: String, character: String): Long

  def getCharacters(uid: Long): List[JSCharacter]
  def getCharacter(cid: Long): JSCharacter

  def getRoles: List[JSRole]
  def addRole(name: String, restricted: Boolean): Long

  def getBadges: List[String]
  def addBadge(name: String, score: Int): Long

  def addInstance(name: String): Long
  def addBoss(instance: String, boss: String): Long

  def getInstances: List[String]
  def getInstanceBosses(instance: String): List[String]

  def getEventTemplate(name: String): JSEventTemplate
  def getEventTemplates: List[String]
  def saveEventTemplate(et: JSEventTemplate): Long

  def getEventSchedules(name: String): List[JSEventSchedule]
}
