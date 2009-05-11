package com.giftoftheembalmer.gotefarm.server.servlet

import com.giftoftheembalmer.gotefarm.server.service._

import com.giftoftheembalmer.gotefarm.client.{
  GoteFarmRPC,
  JSAccount,
  JSBadge,
  JSBoss,
  JSCharacter,
  JSEvent,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSInstance,
  JSGuild,
  JSRegion,
  JSRole,
  UserNotLoggedInError
}

import com.google.appengine.api.datastore.Key
import com.google.appengine.api.users.User
import com.google.appengine.api.users.UserServiceFactory

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.gwtwidgets.server.spring.ServletUtils

import org.slf4j.LoggerFactory

import java.util.Date

class GoteFarmRPCImpl extends RemoteServiceServlet
  with GoteFarmRPC {
  import GoteFarmServiceImpl._

  private val logger = LoggerFactory.getLogger(this.getClass)
  logger.debug("Servlet running")

  @scala.reflect.BeanProperty
  private var goteFarmService: GoteFarmServiceT = null

  private val userService = UserServiceFactory.getUserService

  override
  def getAccount: JSAccount = {
    goteFarmService.getAccount(getUser)
  }

  override
  def setActiveGuild(guild_key: String): JSGuild = {
    goteFarmService.setActiveGuild(getUser, guild_key)
  }

  override
  def getGuildFromArmoryURL(url: String): JSGuild = {
    goteFarmService.getGuildFromArmoryURL(getUser, url)
  }

  override
  def getRegions: java.util.List[JSRegion] = goteFarmService.getRegions

  private def getUser: User = {
    val user = userService.getCurrentUser
    if (user eq null) throw new UserNotLoggedInError
    user
  }

  override
  def newCharacter(guild_key: String, character: String) = {
    goteFarmService.newCharacter(getUser, guild_key, character)
  }

  override
  def getCharacters(guild_key: String) = {
    goteFarmService.getCharacters(getUser, guild_key)
  }

  override
  def getCharacter(character_key: String): JSCharacter = {
    goteFarmService.getCharacter(getUser, character_key)
  }

  override
  def setMainCharacter(guild_key: String, character_key: String): Unit = {
    goteFarmService.setMainCharacter(getUser, guild_key, character_key)
  }

  override
  def getRoles(guild_key: String): java.util.List[JSRole] = {
    goteFarmService.getRoles(guild_key)
  }

  override
  def addRole(guild_key: String, name: String, restricted: Boolean): JSRole = {
    goteFarmService.addRole(getUser, guild_key, name, restricted)
  }

  override
  def updateCharacterRole(character_key: String, role_key: String,
                          adding: Boolean): JSCharacter = {
    goteFarmService.updateCharacterRole(getUser, character_key, role_key,
                                        adding)
  }

  override
  def getBadges(guild_key: String): java.util.List[JSBadge] = {
    goteFarmService.getBadges(guild_key)
  }

  override
  def addBadge(guild_key: String, name: String, score: Int): JSBadge = {
    goteFarmService.addBadge(getUser, guild_key, name, score)
  }

  override
  def updateCharacterBadge(character_key: String, badge_key: String,
                           adding: Boolean): JSCharacter = {
    goteFarmService.updateCharacterBadge(getUser, character_key, badge_key,
                                         adding)
  }

  override
  def addInstance(guild_key: String, name: String): JSInstance = {
    goteFarmService.addInstance(getUser, guild_key, name)
  }

  override
  def addBoss(instance_key: String, name: String): JSBoss = {
    goteFarmService.addBoss(getUser, instance_key, name)
  }

  override
  def getInstances(guild_key: String): java.util.List[JSInstance] = {
    goteFarmService.getInstances(guild_key)
  }

  override
  def getInstanceBosses(instance_key: String): java.util.List[JSBoss] = {
    goteFarmService.getInstanceBosses(instance_key)
  }

  override
  def getEventTemplates(guild_key: String) = {
    goteFarmService.getEventTemplates(getUser, guild_key)
  }

  override
  def saveEventTemplate(guild_key: String, et: JSEventTemplate)
    : JSEventTemplate = {
    goteFarmService.saveEventTemplate(getUser, guild_key, et)
  }

  override
  def getEventSchedules(event_template_key: String)
    : java.util.List[JSEventSchedule] = {
    goteFarmService.getEventSchedules(getUser, event_template_key)
  }

  override
  def saveEventSchedule(guild_key: String, es: JSEventSchedule): Unit = {
    goteFarmService.saveEventSchedule(getUser, guild_key, es)
  }

  override
  def getEvents(guild_key: String): java.util.List[JSEvent] = {
    goteFarmService.getEvents(getUser, guild_key)
  }

  override
  def getEventSignups(event_key: String, if_changed_since: Date)
    : JSEventSignups = {
    goteFarmService.getEventSignups(getUser, event_key, if_changed_since)
      .getOrElse(null)
  }

  override
  def signupForEvent(event_key: String, character_key: String,
                     role_key: String, signup_type: Int): JSEventSignups = {
    goteFarmService.signupForEvent(getUser, event_key, character_key, role_key,
                                   signup_type)
  }

  override
  def changeEventSignup(event_signup_key: String, new_role_key: String,
                        new_signup_type: Int): JSEventSignups = {
    goteFarmService.changeEventSignup(getUser, event_signup_key, new_role_key,
                                      new_signup_type)
  }

  override
  def removeEventSignup(event_signup_key: String): JSEventSignups = {
    goteFarmService.removeEventSignup(getUser, event_signup_key)
  }

  override
  def getTimeZones: Array[String] = {
    goteFarmService.getTimeZones
  }

  override
  def setTimeZone(guild_key: String, time_zone: String): JSGuild = {
    goteFarmService.setTimeZone(getUser, guild_key, time_zone)
  }
}
