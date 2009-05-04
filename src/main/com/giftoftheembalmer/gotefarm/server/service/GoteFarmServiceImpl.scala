package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.server.dao.{
  Boss,
  Chr,
  ChrClass,
  GoteFarmDaoT,
  Guild,
  Instance,
  Race,
  Region,
  ScalaTransactionTemplate
}

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  JSAccount,
  JSBoss,
  JSCharacter,
  JSEventSchedule,
  JSEventSignups,
  JSEventTemplate,
  JSInstance,
  JSGuild,
  JSRegion,
  NotFoundError
}

import com.google.appengine.api.datastore.{
  Key,
  KeyFactory
}
import com.google.appengine.api.users.User

import org.springframework.transaction.annotation.{
  Propagation,
  Transactional
}

import org.apache.commons.logging.LogFactory

import java.net.{
  URL,
  URLEncoder
}

import java.io.Serializable

import java.util.{
  Calendar,
  Collections,
  Date,
  GregorianCalendar,
  TimeZone
}

import javax.cache.{
  Cache,
  CacheManager
}

import scala.collection.jcl.Conversions._

object GoteFarmServiceImpl {
  implicit def key2String(key: Key): String = {
    if (key eq null) {
      null
    }
    else {
      KeyFactory.keyToString(key)
    }
  }

  implicit def string2Key(key: String): Key = {
    if (key eq null) {
      null
    }
    else {
      KeyFactory.stringToKey(key)
    }
  }
}

@Transactional{val propagation = Propagation.NEVER,
               val rollbackFor = Array(classOf[Throwable])}
class GoteFarmServiceImpl extends GoteFarmServiceT {
  import GoteFarmServiceImpl._

  private val logger = LogFactory.getLog(this.getClass)

  @scala.reflect.BeanProperty
  private var goteFarmDao: GoteFarmDaoT = null
  @scala.reflect.BeanProperty
  private var transactionTemplate: ScalaTransactionTemplate = null

  private val cache: java.util.Map[AnyRef, AnyRef] = (
    CacheManager.getInstance.getCacheFactory.createCache(
      Collections.emptyMap[Any,Any]
    ).asInstanceOf[java.util.Map[AnyRef, AnyRef]]
  )

  def cached[K <: Serializable, R <: Serializable](key: K, f: K => R): R = {
    val r = cache.get(key).asInstanceOf[R]
    if (r ne null) {
      r
    }
    else {
      val r = f(key)
      cache.put(key, r)
      r
    }
  }

  def key2Race(key: Key): Race = {
    getRace(key)
  }

  def key2RaceName(key: Key): String = {
    cached(key, { key: Key =>
      transactionTemplate.execute {
        key2Race(key)
      }.getName
    })
  }

  def key2ChrClass(key: Key): ChrClass = {
    getChrClass(key)
  }

  def key2ChrClassName(key: Key): String = {
    cached(key, { key: Key =>
      transactionTemplate.execute {
        key2ChrClass(key)
      }.getName
    })
  }

  implicit def region2JSRegion(region: Region): JSRegion = {
    val r = new JSRegion
    r.key = region.getKey
    r.code = region.getCode
    r
  }

  implicit def chr2JSCharacter(chr: Chr): JSCharacter = {
    val r = new JSCharacter
    r.name = chr.getName
    r.race = key2RaceName(chr.getRace)
    r.clazz = key2ChrClassName(chr.getChrClass)
    r.level = chr.getLevel.shortValue
    r.characterxml = chr.getChrXml
    r.created = chr.getCreated
    r.roles = Array()
    r.badges = Array()
    r
  }

  private def populateJSGuild(jsg: JSGuild, g: Guild): Unit = {
    jsg.key = g.getKey
    jsg.region = g.getRegion
    jsg.realm = g.getRealm
    jsg.name = g.getName
    jsg.owner = g.getOwner
    jsg.officers = new java.util.HashSet[String]
    val officers = g.getOfficers
    if (officers ne null) {
      jsg.officers ++= officers.map(key2String)
    }
  }

  implicit def guild2JSGuild(g: Guild): JSGuild = {
    val r = new JSGuild
    populateJSGuild(r, g)
    r
  }

  implicit def instance2JSInstance(instance: Instance): JSInstance = {
    val r = new JSInstance
    r.key = instance.getKey
    r.name = instance.getName
    r
  }

  implicit def boss2JSBoss(boss: Boss): JSBoss = {
    val r = new JSBoss
    r.key = boss.getKey
    r.name = boss.getName
    r
  }

  private def mkList[A](col: java.util.Collection[A]): java.util.List[A] = {
    val r = new java.util.ArrayList[A]
    if (col ne null) {
      val i = col.iterator
      while (i.hasNext) {
        r.add(i.next)
      }
    }
    r
  }

  private def mkList[A,B](col: java.util.Collection[A], f: A => B)
    : java.util.List[B] = {
    val r = new java.util.ArrayList[B]
    if (col ne null) {
      val i = col.iterator
      while (i.hasNext) {
        r.add(f(i.next))
      }
    }
    r
  }

  private
  def setAdd[I <: AnyRef, S <: java.util.Set[I]](
                                            element: I, existing_set: S,
                                            set: java.util.HashSet[I] => Unit)
    : Unit = {
    if (existing_set eq null) {
      val new_set = new java.util.HashSet[I]
      new_set.add(element)
      set(new_set)
    }
    else {
      existing_set.add(element)
    }
  }

  private
  def listAdd[I <: AnyRef, L <: java.util.List[I]](
                                          element: I, existing_list: L,
                                          set: java.util.ArrayList[I] => Unit)
    : Unit = {
    if (existing_list eq null) {
      val new_list = new java.util.ArrayList[I]
      new_list.add(element)
      set(new_list)
    }
    else {
      existing_list.add(element)
    }
  }

  def initialize() = {
    // Populate Regions
    for (code <- List("us", "eu", "kr", "cn", "tw")) {
      transactionTemplate.execute {
        goteFarmDao.getRegion(code)
      }
    }
  }

  override
  def getAccount(user: User): JSAccount = {
    val r = transactionTemplate.execute {
      val account = goteFarmDao.getAccount(user)

      val r = new JSAccount
      r.key = account.getKey
      r.email = user.getEmail

      r.guilds = new java.util.ArrayList[JSGuild]

      val guilds = account.getGuilds
      if (guilds ne null) {
        for (g <- guilds) {
          val jsg = new JSGuild
          jsg.key = g
          r.guilds.add(jsg)
        }
      }

      val ag = account.getActiveGuild
      if (ag ne null) {
        val key: String = ag
        // point active_guild at one of the existing JSGuild instances
        r.active_guild = r.guilds.find(_.key == key).getOrElse(null)
      }

      r
    }

    // populate the JSGuild objects
    for (jsg <- r.guilds) {
      transactionTemplate.execute {
        goteFarmDao.getGuild(jsg.key) match {
          case Some(g) =>
            populateJSGuild(jsg, g)

          case None =>
            jsg.name = "Guild no longer exists"
        }
      }
    }

    r
  }

  private val regionre = """(?i)(\w+)\.wowarmory\.com""".r

  def getGuildFromArmoryURL(user: User, url: String): JSGuild = {
    // Parse url to detect region code
    val region = regionre.findFirstMatchIn(url) match {
      case Some(m) =>
        m.group(1).toLowerCase match {
          case "www" => "us"
          case x => x
        }

      case None =>
        throw new IllegalArgumentException("Invalid armory URL")
    }

    // Fetch character XML from armory
    val url2 = {
      if (url.startsWith("http://")) {
        url
      }
      else {
        "http://" + url
      }
    }
    val charxml = try {
      fetchCharacterFromArmory(url2)
    }
    catch {
      case _: java.io.IOException =>
        throw new IllegalArgumentException("Invalid URL or other error, try again.")
    }

    val charInfo = charxml \ "characterInfo"
    if (charInfo.isEmpty) {
      throw new IllegalArgumentException(
        "URL did not appear to return armory character information"
      )
    }

    // check if the character was found
    if (!(charInfo \ "@errCode").isEmpty) {
      throw new NotFoundError("Character not found on armory.")
    }

    val character = charInfo \ "character"
    if (character.isEmpty) {
      throw new IllegalArgumentException("Unrecognized armory format")
    }

    // Parse guild name
    val guild = character \ "@guildName"
    if (guild.isEmpty || guild.toString == "") {
      throw new NotFoundError("Character is guild-less")
    }

    // Parse realm name
    val realm = character \ "@realm"
    if (realm.isEmpty || realm.toString == "") {
      throw new IllegalArgumentException("Unrecognized armory format")
    }

    // Get/create region
    val region_code = transactionTemplate.execute {
      goteFarmDao.getRegion(region).getCode
    }

    // Get/create realm
    val realm_name = transactionTemplate.execute {
      goteFarmDao.getRealm(region_code, realm.toString).getName
    }

    // Get account key
    val account_key = transactionTemplate.execute {
      val a = goteFarmDao.getAccount(user)
      a.getKey
    }

    // Get/create guild
    val (r, guild_key) = transactionTemplate.execute {
      val g = goteFarmDao.getGuild(region_code, realm_name, guild.toString,
                                   account_key)

      (guild2JSGuild(g), g.getKey)
    }

    // Associate guild with account
    transactionTemplate.execute {
      val acct = goteFarmDao.getAccount(user)
      setAdd(guild_key, acct.getGuilds, acct.setGuilds)
      acct.setActiveGuild(guild_key)
    }

    // Register new character (ignore error if it already exists)
    try {
      createCharacter(user, guild_key, charxml)
    }
    catch {
      case _: AlreadyExistsError =>
    }

    r
  }

  /*
  def login(username: String, password: String) =
    goteFarmDao.validateUser(username, password)

  @Transactional{val readOnly = false}
  def newUser(username: String, email: String, password: String) =
    goteFarmDao.createUser(username, email, password)
  */

  def getRegions: java.util.List[JSRegion] = {
    transactionTemplate.execute {
      mkList(goteFarmDao.getRegions, region2JSRegion)
    }
  }

  def getChrClass(key: Key): ChrClass = goteFarmDao.getChrClass(key).getOrElse(
    throw new NotFoundError
  )

  def getRace(key: Key): Race = goteFarmDao.getRace(key).getOrElse(
    throw new NotFoundError
  )

  def getGuild(key: Key): Guild = goteFarmDao.getGuild(key).getOrElse(
    throw new NotFoundError
  )

  def getRegion(key: Key): Region = goteFarmDao.getRegion(key).getOrElse(
    throw new NotFoundError
  )

  private def fetchCharacterFromArmory(url: String): scala.xml.Elem = {
    val conn = new URL(url).openConnection

    conn.setRequestProperty(
      "User-Agent",
      "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9) Gecko/20008061004 Firefox/3.0"
    )
    conn.setRequestProperty(
      "Accept-Language",
      "en-us,en;q=0.5"
    )
    conn.setRequestProperty(
      "Accept-Charset",
      "ISO-8859-1,utf-8;q=0.7,*;q=0.7"
    )

    conn.connect()

    try {
      logger.debug("Loading character via URL: " + url)
      scala.xml.XML.load(conn.getInputStream())
    }
    catch {
      case e: org.xml.sax.SAXParseException =>
        logger.debug("Failure parsing armory XML", e)
        throw new IllegalArgumentException("Invalid armory URL or other error, try again later.")
    }
  }

  private def createCharacter(user: User, guild: Key, charxml: scala.xml.Elem): JSCharacter = {
    val charInfo = charxml \ "characterInfo"

    // check if the character was found
    if (!(charInfo \ "@errCode").isEmpty) {
      throw new NotFoundError("Character not found.")
    }

    val char = charInfo \ "character"

    val name = char \ "@name"
    val realm = char \ "@realm"
    val race = char \ "@race"
    val clazz = char \ "@class"
    val level = (char \ "@level").toString.toInt

    // Character already in use?
    transactionTemplate.execute {
      val chr = goteFarmDao.getCharacter(guild, name.toString)
      if (chr.isDefined) {
        throw new AlreadyExistsError("Character '" + name.toString + "' already exists.")
      }
    }

    // get (or create) the race object
    val race_obj = transactionTemplate.execute {
      goteFarmDao.getRace(race.toString)
    }

    // get (or create) the class object
    val class_obj = transactionTemplate.execute {
      goteFarmDao.getChrClass(clazz.toString)
    }

    val chr = transactionTemplate.execute {
      goteFarmDao.createCharacter(user, guild, name.toString,
                                  race_obj, class_obj, level, charxml.toString)
    }

    // XXX: Duplicates chr2JSCharacter but don't need to re-query
    // race and class here
    val r = new JSCharacter
    r.name = chr.getName
    r.race = race_obj.getName
    r.clazz = class_obj.getName
    r.level = chr.getLevel.shortValue
    r.characterxml = chr.getChrXml
    r.created = chr.getCreated
    r.roles = Array()
    r.badges = Array()
    r
  }

  def newCharacter(user: User, guild_key: Key, character: String) = {
    // Need region code and realm name for this guild
    val (region, realm) = transactionTemplate.execute {
      val r = goteFarmDao.getGuild(guild_key).getOrElse(
        throw new NotFoundError("Guild not found")
      )
      (r.getRegion, r.getRealm)
    }

    val charxml = fetchCharacterFromArmory(
      "http://" + region + ".wowarmory.com/character-sheet.xml?r=" +
      URLEncoder.encode(realm, "UTF-8") + "&n=" +
      URLEncoder.encode(character, "UTF-8"))

    createCharacter(user, guild_key, charxml)
  }

  def getCharacters(user: User, guild: Key) = {
    val chrs = transactionTemplate.execute {
      mkList(goteFarmDao.getCharacters(user, guild))
    }
    mkList(chrs, chr2JSCharacter)
  }
  /*
  def getCharacter(cid: Long) = goteFarmDao.getCharacter(cid)

  def getRoles = goteFarmDao.getRoles
  @Transactional{val readOnly = false}
  def addRole(name: String, restricted: Boolean) =
    goteFarmDao.addRole(name, restricted)

  @Transactional{val readOnly = false}
  def updateCharacterRole(uid: Long, cid: Long, roleid: Long,
                          adding: Boolean): Unit = {
    // character must belong to user
    val chr = goteFarmDao.getCharacter(cid)
    if (chr.accountid != uid) {
      throw new IllegalArgumentException("Character does not belong to you.")
    }
    goteFarmDao.updateCharacterRole(cid, roleid, adding)
  }

  def getBadges = goteFarmDao.getBadges
  @Transactional{val readOnly = false}
  def addBadge(name: String, score: Int) =
    goteFarmDao.addBadge(name, score)

  @Transactional{val readOnly = false}
  def updateCharacterBadge(uid: Long, cid: Long, badgeid: Long,
                           adding: Boolean): Unit = {
    // character must belong to user
    val chr = goteFarmDao.getCharacter(cid)
    if (chr.accountid != uid) {
      throw new IllegalArgumentException("Character does not belong to you.")
    }
    goteFarmDao.updateCharacterBadge(cid, badgeid, adding)
  }
  */

  @Transactional{val propagation = Propagation.REQUIRED}
  def addInstance(user: User, guild: Key, name: String): JSInstance = {
    // TODO: make sure user is an officer of the guild
    goteFarmDao.addInstance(guild, name)
  }

  /*
  @Transactional{val readOnly = false}
  def addBoss(instance: String, boss: String) =
    goteFarmDao.addBoss(instance, boss)
  */

  @Transactional{val propagation = Propagation.REQUIRED}
  def getInstances(guild: Key): java.util.List[JSInstance] = {
    mkList(goteFarmDao.getInstances(guild), instance2JSInstance)
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  def getInstanceBosses(instance: Key): java.util.List[JSBoss] = {
    val g = goteFarmDao.getInstance(instance).getOrElse(
      throw new NotFoundError("Instance not found")
    )
    val b = g.getBosses
    if (b eq null) {
      new java.util.ArrayList[JSBoss]
    }
    else {
      mkList(b, boss2JSBoss)
    }
  }

  /*
  def getEventTemplate(name: String) = goteFarmDao.getEventTemplate(name)
  def getEventTemplates = goteFarmDao.getEventTemplates
  @Transactional{val readOnly = false}
  def saveEventTemplate(et: JSEventTemplate) =
    goteFarmDao.saveEventTemplate(et)

  def getEventSchedules(name: String) =
    goteFarmDao.getEventSchedules(name)
  @Transactional{val readOnly = false}
  def saveEventSchedule(es: JSEventSchedule) = {
    if (es.repeat_size < JSEventSchedule.REPEAT_NEVER || es.repeat_size > JSEventSchedule.REPEAT_MONTHLY) {
      throw new IllegalArgumentException("Illegal repeat size")
    }
    if (es.repeat_size > JSEventSchedule.REPEAT_NEVER && es.repeat_freq < 1) {
      throw new IllegalArgumentException("Illegal repeat freq")
    }
    if (es.repeat_size == JSEventSchedule.REPEAT_WEEKLY && (es.day_mask & 0x7F) == 0) {
      throw new IllegalArgumentException("Illegal day mask")
    }
    if (   es.repeat_size == JSEventSchedule.REPEAT_MONTHLY
        && (   es.repeat_by < JSEventSchedule.REPEAT_BY_DAY_OF_MONTH
            || es.repeat_by > JSEventSchedule.REPEAT_BY_DAY_OF_WEEK)) {
      throw new IllegalArgumentException("Illegal repeat by")
    }

    val r = goteFarmDao.saveEventSchedule(es)

    // publish events right away
    publishEvents()

    r
  }

  private def getEventNextOccurrence(event: JSEventSchedule): Date = {
    val h = event.timezone_offset / 60 * -1
    // FIXME: This ignores daylight saving time
    val m = Math.abs(event.timezone_offset % 60)
    val tz = String.format("GMT%+03d%02d", Array(int2Integer(h), int2Integer(m)): _*)
    val cal = new GregorianCalendar(TimeZone.getTimeZone(tz))
    cal.setFirstDayOfWeek(Calendar.SUNDAY)
    cal.setTime(event.start_time)

    event.repeat_size match {
      case JSEventSchedule.REPEAT_NEVER =>
        throw new IllegalStateException("Event does not repeat")

      case JSEventSchedule.REPEAT_DAILY =>
        cal.add(Calendar.DAY_OF_MONTH, event.repeat_freq)
        cal.getTime

      case JSEventSchedule.REPEAT_WEEKLY =>
        // look for the next day this week, or skip ahead repeat_freq weeks
        if (event.day_mask == 0) {
          throw new IllegalStateException("Event repeats weekly but no days are set")
        }

        def scanForNextDay(cal: Calendar, allow_sunday: Boolean): Option[Date] = {
          val dom = cal.get(Calendar.DAY_OF_WEEK)

          if (!allow_sunday && dom == Calendar.SUNDAY) {
            // rolled forward into next week, no match
            None
          }
          else if ((event.day_mask & (1<<(dom-1))) != 0) {
            // this day's bit is set, a match
            Some(cal.getTime)
          }
          else {
            // try the next day
            cal.add(Calendar.DAY_OF_MONTH, 1)
            scanForNextDay(cal, false)
          }
        }

        // pre-seek one day forward
        cal.add(Calendar.DAY_OF_MONTH, 1)

        scanForNextDay(cal, false).getOrElse({
          // no match for the current week, so seek forward to the next
          // Sunday, skipping repeat_freq weeks
          val start_of_week = new GregorianCalendar(TimeZone.getTimeZone(tz))
          start_of_week.setTime(event.start_time)
          start_of_week.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
          start_of_week.add(Calendar.WEEK_OF_YEAR, event.repeat_freq)
          scanForNextDay(start_of_week, true).getOrElse({
            throw new RuntimeException("Unexpected None in scanForNextDay")
          })
        })

      case JSEventSchedule.REPEAT_MONTHLY =>
        val orig_cal = new GregorianCalendar(TimeZone.getTimeZone(tz))
        orig_cal.setFirstDayOfWeek(Calendar.SUNDAY)
        orig_cal.setTime(event.orig_start_time)

        event.repeat_by match {
          case JSEventSchedule.REPEAT_BY_DAY_OF_MONTH =>
            val dom = orig_cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.MONTH, event.repeat_freq)
            val max_dom = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, if (dom > max_dom) max_dom else dom)
            cal.getTime

          case JSEventSchedule.REPEAT_BY_DAY_OF_WEEK =>
            val dow = orig_cal.get(Calendar.DAY_OF_WEEK)
            val dowim = orig_cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
            cal.add(Calendar.MONTH, event.repeat_freq)
            // go to middle of month...
            cal.set(Calendar.DAY_OF_MONTH, 15)
            // so changing the day won't change the month
            cal.set(Calendar.DAY_OF_WEEK, dow)
            val month = cal.get(Calendar.MONTH)
            // changing the DAY_OF_WEEK_IN_MONTH might change the month
            cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dowim)
            while (cal.get(Calendar.MONTH) != month) {
              // so subtract weeks to get back to the same month
              cal.add(Calendar.WEEK_OF_YEAR, -1)
            }
            cal.getTime
        }

      case _ => // invalid value
        throw new IllegalStateException("Unknown repeat_size value");
    }
  }

  def getEvents = goteFarmDao.getEvents
  def getEventSignups(eventid: Long, if_changed_since: Date)
    : Option[JSEventSignups] =
    goteFarmDao.getEventSignups(eventid, if_changed_since)

  private def validateSignup(uid: Long, eventid: Long, chr: JSCharacter,
                             roleid: Long) = {
    // character must belong to user
    if (chr.accountid != uid) {
      throw new IllegalArgumentException("Signup does not belong to you.")
    }

    // character must have roleid
    if (!chr.hasRole(roleid)) {
      throw new IllegalArgumentException("Character missing role.")
    }

    // signups must not be closed
    val event = goteFarmDao.getEvent(eventid)
    val now = System.currentTimeMillis
    if (now > event.signups_end.getTime) {
      throw new IllegalArgumentException("Signups are closed for this event.")
    }

    // character must be able to signup
    val signups_start = event.signups_start.getTime
    if (now < signups_start) {
      // character needs to qualify for an early signup
      lazy val role = goteFarmDao.getRole(roleid)
      if (!event.badges.exists({ badge =>
           ((now >= signups_start - badge.earlySignup * 3600000L)
        && ((badge.applyToRole eq null) || badge.applyToRole == role.name)
        && chr.hasBadge(badge.badgeid))
      })) {
        throw new IllegalArgumentException("Character cannot sign up for this event yet.")
      }
    }
  }

  private def validateSignup(uid: Long, eventid: Long, cid: Long,
                             roleid: Long): Unit = {
    val chr = goteFarmDao.getCharacter(cid)
    validateSignup(uid, eventid, chr, roleid)
  }

  @Transactional{val readOnly = false}
  def signupForEvent(uid: Long, eventid: Long, cid: Long, roleid: Long,
                     signup_type: Int) = {
    validateSignup(uid, eventid, cid, roleid)
    goteFarmDao.signupForEvent(eventid, cid, roleid, signup_type)
  }

  @Transactional{val readOnly = false}
  def changeEventSignup(uid: Long, eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit = {
    val es = goteFarmDao.getEventSignup(eventsignupid)
    validateSignup(uid, es.eventid, es.chr, new_roleid)
    goteFarmDao.changeEventSignup(eventsignupid, new_roleid, new_signup_type)
  }

  @Transactional{val readOnly = false}
  def removeEventSignup(uid: Long, eventsignupid: Long): Unit = {
    // character must belong to user
    val es = goteFarmDao.getEventSignup(eventsignupid)
    if (es.chr.accountid != uid) {
      throw new IllegalArgumentException("Signup does not belong to you.")
    }
    goteFarmDao.removeEventSignup(eventsignupid)
  }

  @Transactional{val readOnly = false}
  def publishEvents() = synchronized {
    val reftime = System.currentTimeMillis

    def keepPublishing(event: JSEventSchedule): Unit = {
      // publish as soon as the display time is hit
      if (event.start_time.getTime - event.display_start * 1000 <= reftime) {
        logger.debug("publishing " + event.esid)
        logger.debug(event.start_time)
        logger.debug("repeat_size: " + event.repeat_size)
        logger.debug("repeat_freq: " + event.repeat_freq)
        logger.debug("day_mask: " + event.day_mask)
        logger.debug("repeat_by: " + event.repeat_by)

        goteFarmDao.publishEvent(event)

        // update to next time
        if (event.repeat_size != JSEventSchedule.REPEAT_NEVER) {
          event.start_time = getEventNextOccurrence(event)
          logger.debug("Next: " + event.start_time)

          // save new start time
          goteFarmDao.saveEventSchedule(event)

          // try to publish again
          keepPublishing(event)
        }
        else {
          // one-shot event, deactive it
          event.active = false
          goteFarmDao.saveEventSchedule(event)
        }
      }
    }

    for (event <- goteFarmDao.getActiveEventSchedules) {
      keepPublishing(event)
    }
  }
  */
}
