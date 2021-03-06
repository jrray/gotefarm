package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSBadge,
  JSCharacter,
  JSChrBadge,
  JSChrRole,
  JSEvent,
  JSEventBadge,
  JSEventRole,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSRole,
  NotFoundError
}

import com.google.appengine.api.datastore.Key
import com.google.appengine.api.users.User

import java.util.Date

import scala.collection.jcl.Conversions._

import scala.Predef.{
  intWrapper => _,
  longWrapper => _,
  _
}

class GoteFarmJdoDao extends ScalaJdoDaoSupport
  with GoteFarmDaoT {

  implicit def convertCollection[T](col: java.util.Collection[T])
    : scala.collection.jcl.CollectionWrapper[T] = {
    new scala.collection.jcl.CollectionWrapper[T] {
      override val underlying = col
      override def transform(f: (T) => T): Boolean = {
        throw new UnsupportedOperationException(
          "In-place modification not possible"
        )
      }
    }
  }

  override
  def getAccount(user: User): Account = {
    val r = find(classOf[Account], "user == userParam",
                 "com.google.appengine.api.users.User userParam")(user)
    if (r.isEmpty) {
      // add it
      val nr = new Account(user)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      val a = r.iterator.next
      a.setLastUpdate(new Date)
      a
    }
  }

  override
  def getAccount(key: Key): Option[Account] = {
    getObjectById(classOf[Account], key)
  }

  override
  def getBoss(key: Key): Option[Boss] = {
    getObjectById(classOf[Boss], key)
  }

  override
  def getRegions: java.util.Collection[Region] = {
    find(classOf[Region])
  }

  override
  def getChrClass(key: Key): Option[ChrClass] = {
    getObjectById(classOf[ChrClass], key)
  }

  override
  def getInstance(key: Key): Option[Instance] = {
    getObjectById(classOf[Instance], key)
  }

  override
  def getRace(key: Key): Option[Race] = {
    getObjectById(classOf[Race], key)
  }

  override
  def getRace(race: String): Race = {
    val r = find(classOf[Race], "name == nameParam",
                 "java.lang.String nameParam")(race)
    if (r.isEmpty) {
      // add it
      val nr = new Race(race)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getRegion(key: Key): Option[Region] = {
    getObjectById(classOf[Region], key)
  }

  override
  def getRegion(code: String): Region = {
    val r = find(classOf[Region], "code == codeParam",
                 "java.lang.String codeParam")(code)
    if (r.isEmpty) {
      // add it
      val nr = new Region(code)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getRealm(key: Key): Option[Realm] = {
    getObjectById(classOf[Realm], key)
  }

  override
  def getRealm(region: String, name: String): Realm = {
    val r = find(
      classOf[Realm], "region == regionParam && name == nameParam",
      "java.lang.String regionParam, java.lang.String nameParam"
    )(region, name)
    if (r.isEmpty) {
      // add it
      val nr = new Realm(region, name, null)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getGuild(key: Key): Option[Guild] = {
    getObjectById(classOf[Guild], key)
  }

  override
  def getGuild(region: String, realm: String, name: String, account: Key)
    : Guild = {
    val r = find(
      classOf[Guild],
      "region == regionParam && realm == realmParam && name == nameParam",
        "java.lang.String regionParam, java.lang.String realmParam,"
      + "java.lang.String nameParam"
    )(region, realm, name)
    if (r.isEmpty) {
      // add it
      val nr = new Guild(region, realm, name, account, null)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getChrClass(clazz: String): ChrClass = {
    val r = find(classOf[ChrClass], "name == nameParam",
                 "java.lang.String nameParam")(clazz)
    if (r.isEmpty) {
      // add it
      val nr = new ChrClass(clazz)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getCharacter(guild: Key, name: String): Option[Chr] = {
    val r = find(classOf[Chr], "guild == guildParam && name == nameParam",
                   "com.google.appengine.api.datastore.Key guildParam, "
                 + "java.lang.String nameParam")(guild, name)
    if (r.isEmpty) {
      None
    }
    else {
      Some(r.iterator.next)
    }
  }

  override
  def getCharacter(key: Key): Option[Chr] = {
    getObjectById(classOf[Chr], key)
  }

  override
  def getChrGroup(key: Key): Option[ChrGroup] = {
    getObjectById(classOf[ChrGroup], key)
  }

  override
  def getRole(key: Key): Option[Role] = {
    getObjectById(classOf[Role], key)
  }

  override
  def getRoles(guild: Key): java.util.Collection[Role] = {
    find(classOf[Role], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def addRole(guild: Key, name: String, restricted: Boolean): Role = {
    val nr = new Role(guild, name, restricted)
    getJdoTemplate.makePersistent(nr)
    nr
  }

  override
  def getBadge(key: Key): Option[Badge] = {
    getObjectById(classOf[Badge], key)
  }

  override
  def getBadges(guild: Key): java.util.Collection[Badge] = {
    find(classOf[Badge], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def addBadge(guild: Key, name: String, score: Int): Badge = {
    val nb = new Badge(guild, name, score)
    getJdoTemplate.makePersistent(nb)
    nb
  }

  override
  def addInstance(guild: Key, name: String): Instance = {
    val ni = new Instance(guild, name)
    getJdoTemplate.makePersistent(ni)
    ni
  }

  override
  def getInstances(guild: Key): java.util.Collection[Instance] = {
    find(classOf[Instance], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def addEventTemplate(guild: Key, name: String, size: Int, min_level: Int,
                       instance: String, instance_key: Key): EventTemplate = {
    val ne = new EventTemplate(guild, name, size, min_level, instance,
                               instance_key)
    getJdoTemplate.makePersistent(ne)
    ne
  }

  override
  def getEventTemplate(key: Key): Option[EventTemplate] = {
    getObjectById(classOf[EventTemplate], key)
  }

  override
  def getEventTemplates(guild: Key): java.util.Collection[EventTemplate] = {
    find(classOf[EventTemplate], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def getActiveEventSchedule(window: Long)
    : java.util.Collection[EventSchedule] = {
    // limit results to two: one to act upon and another to know if there is
    // more work to do
    executeFind { pm =>
      val query = pm.newQuery(classOf[EventSchedule],
                              "active == true && displayStartDate <= now")
      getJdoTemplate.prepareQuery(query)
      query.declareParameters("java.util.Date now")
      query.setOrdering("displayStartDate asc")
      query.setRange(0, 1)
      query.executeWithArray(new Date(System.currentTimeMillis + window))
    }
  }

  override
  def getEventSchedule(key: Key): Option[EventSchedule] = {
    getObjectById(classOf[EventSchedule], key)
  }

  override
  def getEventSchedules(event_template: Key)
    : java.util.Collection[EventSchedule] = {
    find(classOf[EventSchedule], "eventTemplate == etParam",
         "com.google.appengine.api.datastore.Key etParam")(event_template)
  }

  override
  def getGuildEventSchedules(guild: Key)
    : java.util.Collection[EventSchedule] = {
    find(classOf[EventSchedule], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def saveEventSchedule(guild: Key, es: JSEventSchedule): EventSchedule = {
    import com.giftoftheembalmer.gotefarm.server.service.GoteFarmServiceImpl.{
      key2String,
      string2Key
    }

    if (es.event_schedule_key eq null) {
      // new schedule
      val nes = new EventSchedule(
        guild, es.event_template_key, es.start_time, es.time_zone, es.duration,
        es.display_start, es.display_end, es.signups_start, es.signups_end,
        es.repeat_size, es.repeat_freq, es.day_mask, es.repeat_by, es.active
      )
      getJdoTemplate.makePersistent(nes)
      es.event_schedule_key = nes.getKey
      nes
    }
    else {
      // update existing
      val nes = getEventSchedule(es.event_schedule_key).getOrElse(
        throw new NotFoundError("Event schedule not found")
      )

      nes.setStartTime(es.start_time)
      nes.setTimeZone(es.time_zone)
      nes.setDuration(es.duration)
      nes.setDisplayStart(es.display_start)
      nes.setDisplayEnd(es.display_end)
      nes.setSignupsStart(es.signups_start)
      nes.setSignupsEnd(es.signups_end)
      nes.setRepeatSize(es.repeat_size)
      nes.setRepeatFreq(es.repeat_freq)
      nes.setDayMask(es.day_mask)
      nes.setRepeatBy(es.repeat_by)
      nes.setActive(es.active)
      nes
    }
  }

  override
  def publishEvent(es: EventSchedule, et: EventTemplate): Unit = {
    // create event
    val e = new Event(es.getEventTemplate, es.getGuild, et.getName, et.getSize,
                      et.getMinimumLevel, et.getInstance, et.getInstanceKey,
                      es.getStartTime, es.getDuration, es.getDisplayStartDate,
                      es.getDisplayEndDate, es.getSignupsStartDate,
                      es.getSignupsEndDate)

    e.setEventBadges(et.getBadges)
    e.setEventBosses(et.getBosses)
    e.setEventRoles(et.getRoles)

    getJdoTemplate.makePersistent(e)
  }

  override
  def getEvents(guild: Key): java.util.Collection[Event] = {
    find(classOf[Event], "guild == guildParam && displayEnd >= now",
           "com.google.appengine.api.datastore.Key guildParam, "
         + "java.util.Date now")(
         guild, new Date)
  }

  override
  def getEventKeys(event_template: Key): Iterable[Key] = {
    find(
      classOf[Event], "eventTemplate == etParam",
      "com.google.appengine.api.datastore.Key etParam"
    )(event_template).map(_.getKey)
  }

  override
  def getEvent(key: Key): Option[Event] = {
    getObjectById(classOf[Event], key)
  }

  override
  def getEventSignup(key: Key): Option[Signup] = {
    getObjectById(classOf[Signup], key)
  }

  def detachCopy[T <: AnyRef](entity: T): T = {
    getJdoTemplate.detachCopy(entity).asInstanceOf[T]
  }
}
