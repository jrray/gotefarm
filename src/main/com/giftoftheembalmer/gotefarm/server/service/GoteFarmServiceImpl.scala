package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.server.dao.{
  Chr,
  ChrClass,
  GoteFarmDaoT,
  Race,
  Region,
  ScalaTransactionTemplate
}

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  JSCharacter,
  JSEventSchedule,
  JSEventSignups,
  JSEventTemplate,
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

@Transactional{val propagation = Propagation.NEVER}
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
    r.realm = chr.getRealm
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

    logger.debug("Loading character via URL: " + url)
    scala.xml.XML.load(conn.getInputStream())
  }

  private def createCharacter(user: User, charxml: scala.xml.Elem): JSCharacter = {
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
      val chr = goteFarmDao.getCharacter(realm.toString, name.toString)
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
      goteFarmDao.createCharacter(user, realm.toString, name.toString,
                                  race_obj, class_obj, level, charxml.toString)
    }

    // XXX: Duplicates chr2JSCharacter but don't need to re-query
    // race and class here
    val r = new JSCharacter
    r.realm = chr.getRealm
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

  def newCharacter(user: User, realm: String, character: String) = {
    val charxml = fetchCharacterFromArmory(
      "http://www.wowarmory.com/character-sheet.xml?r=" +
      URLEncoder.encode(realm, "UTF-8") + "&n=" +
      URLEncoder.encode(character, "UTF-8"))

    createCharacter(user, charxml)
  }

  def getCharacters(user: User) = {
    val chrs = transactionTemplate.execute {
      mkList(goteFarmDao.getCharacters(user))
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

  @Transactional{val readOnly = false}
  def addInstance(name: String) = goteFarmDao.addInstance(name)
  @Transactional{val readOnly = false}
  def addBoss(instance: String, boss: String) =
    goteFarmDao.addBoss(instance, boss)

  def getInstances() = goteFarmDao.getInstances
  def getInstanceBosses(instance: String) =
    goteFarmDao.getInstanceBosses(instance)

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
