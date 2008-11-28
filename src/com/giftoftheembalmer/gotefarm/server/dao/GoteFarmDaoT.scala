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

  def validateUser(username: String, password: String): Int
  def createUser(username: String, email: String, password: String): Int
  def createCharacter(uid: Int, realm: String, character: String): Int

  def getCharacters(uid: Int): List[JSCharacter]
  def getCharacter(cid: Int): JSCharacter

  def getRoles: List[JSRole]
  def addRole(name: String, restricted: Boolean): Int

  def getBadges: List[String]
  def addBadge(name: String, score: Int): Int

  def addInstance(name: String): Int
  def addBoss(instance: String, boss: String): Int

  def getInstances: List[String]
  def getInstanceBosses(instance: String): List[String]

  def getEventTemplate(name: String): JSEventTemplate
  def getEventTemplates: List[String]
  def saveEventTemplate(et: JSEventTemplate): Int

  def getEventSchedules(name: String): List[JSEventSchedule]
}
