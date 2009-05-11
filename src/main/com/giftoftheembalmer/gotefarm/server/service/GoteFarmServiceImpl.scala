package com.giftoftheembalmer.gotefarm.server.service

import com.giftoftheembalmer.gotefarm.server.dao.{
  Badge,
  Boss,
  Chr,
  ChrBadge,
  ChrClass,
  ChrGroup,
  ChrRole,
  Event,
  EventBadge,
  EventBoss,
  EventRole,
  EventSchedule,
  EventTemplate,
  GoteFarmDaoT,
  Guild,
  Instance,
  Race,
  Region,
  Role,
  ScalaTransactionTemplate,
  Signup
}

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  JSAccount,
  JSBadge,
  JSBoss,
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
  JSInstance,
  JSGuild,
  JSRegion,
  JSRole,
  NotAuthorizedException,
  NotFoundError
}

import com.google.appengine.api.datastore.{
  Key,
  KeyFactory
}
import com.google.appengine.api.users.User

import org.slf4j.LoggerFactory

import org.springframework.transaction.annotation.{
  Propagation,
  Transactional
}

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
import scala.util.Sorting._

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

  implicit def function2ToComparator[T](f: (T, T) => Int)
    : java.util.Comparator[T] = {
    new java.util.Comparator[T] {
      override
      def compare(o1: T, o2: T): Int = {
        f(o1, o2)
      }
    }
  }
}

@Transactional{val propagation = Propagation.NEVER,
               val rollbackFor = Array(classOf[Throwable])}
class GoteFarmServiceImpl extends GoteFarmServiceT {
  import GoteFarmServiceImpl._

  private val logger = LoggerFactory.getLogger(this.getClass)

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

  def key2BadgeName(key: Key): String = {
    cached(key, { key: Key =>
      transactionTemplate.execute {
        goteFarmDao.getBadge(key).getOrElse(
          throw new NotFoundError("Badge not found")
        )
      }.getName
    })
  }

  def key2BossName(key: Key): String = {
    cached(key, { key: Key =>
      transactionTemplate.execute {
        goteFarmDao.getBoss(key).getOrElse(
          throw new NotFoundError("Boss not found")
        )
      }.getName
    })
  }

  def key2InstanceName(key: Key): String = {
    cached(key, { key: Key =>
      transactionTemplate.execute {
        goteFarmDao.getInstance(key).getOrElse(
          throw new NotFoundError("Instance not found")
        )
      }.getName
    })
  }

  def key2RoleName(key: Key): String = {
    cached(key, { key: Key =>
      transactionTemplate.execute {
        goteFarmDao.getRole(key).getOrElse(
          throw new NotFoundError("Role not found")
        )
      }.getName
    })
  }

  implicit def eventSchedule2JSEventSchedule(es: EventSchedule)
    : JSEventSchedule = {
    val r = new JSEventSchedule
    r.event_schedule_key = es.getKey
    r.event_template_key = es.getEventTemplate
    r.start_time = es.getStartTime
    r.orig_start_time = es.getOrigStartTime
    r.time_zone = es.getTimeZone
    r.duration = es.getDuration
    r.display_start = es.getDisplayStart
    r.display_end = es.getDisplayEnd
    r.signups_start = es.getSignupsStart
    r.signups_end = es.getSignupsEnd
    r.repeat_size = es.getRepeatSize
    r.repeat_freq = es.getRepeatFreq
    r.day_mask = es.getDayMask
    r.repeat_by = es.getRepeatBy
    r.active = es.isActive
    r
  }

  implicit def region2JSRegion(region: Region): JSRegion = {
    val r = new JSRegion
    r.key = region.getKey
    r.code = region.getCode
    r
  }

  implicit def chrrole2JSChrRole(chrrole: ChrRole): JSChrRole = {
    val r = new JSChrRole
    r.key = chrrole.getRoleKey
    r.name = chrrole.getRole
    // FIXME: not setting restricted, is it needed?
    r.waiting = chrrole.isWaiting
    r.approved = chrrole.isApproved
    r.message = chrrole.getMessage
    r
  }

  implicit def chrbadge2JSChrBadge(chrbadge: ChrBadge): JSChrBadge = {
    val r = new JSChrBadge
    r.key = chrbadge.getBadgeKey
    r.name = chrbadge.getBadge
    // FIXME: not setting restricted, is it needed?
    r.waiting = chrbadge.isWaiting
    r.approved = chrbadge.isApproved
    r.message = chrbadge.getMessage
    r
  }

  implicit def chr2JSCharacter(chr: Chr): JSCharacter = {
    val r = new JSCharacter
    r.key = chr.getKey
    r.account_key = chr.getAccountKey
    r.name = chr.getName
    r.main = chr.getMain
    r.race = chr.getRace
    r.clazz = chr.getChrClass
    r.level = chr.getLevel.shortValue
    r.characterxml = chr.getChrXml
    r.created = chr.getCreated
    // FIXME: appengine null collection bug
    val chr_roles = chr.getRoles
    r.roles = if (chr_roles ne null) {
      chr_roles.map(chrrole2JSChrRole).toArray
    }
    else {
      Array()
    }
    // FIXME: appengine null collection bug
    val chr_badges = chr.getBadges
    r.badges = if (chr_badges ne null) {
      chr_badges.map(chrbadge2JSChrBadge).toArray
    }
    else {
      Array()
    }
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
    jsg.time_zone = g.getTimeZone
  }

  implicit def guild2JSGuild(g: Guild): JSGuild = {
    val r = new JSGuild
    populateJSGuild(r, g)
    r
  }

  implicit def eventRole2JSEventRole(eventRole: EventRole): JSEventRole = {
    val r = new JSEventRole
    r.role_key = eventRole.getRoleKey
    r.name = eventRole.getRole
    r.min = eventRole.getMin
    r.max = eventRole.getMax
    r
  }

  implicit def eventBadge2JSEventBadge(eventBadge: EventBadge)
    : JSEventBadge = {
    val r = new JSEventBadge
    r.badge_key = eventBadge.getBadgeKey
    r.name = eventBadge.getBadge
    r.requireForSignup = eventBadge.getRequireForSignup
    r.applyToRole = eventBadge.getApplyToRole
    r.numSlots = eventBadge.getNumSlots
    r.earlySignup = eventBadge.getEarlySignup
    r
  }

  implicit def eventTemplate2JSEventTemplate(eventTemplate: EventTemplate)
    : JSEventTemplate = {
    val r = new JSEventTemplate
    r.key = eventTemplate.getKey
    r.name = eventTemplate.getName
    r.size = eventTemplate.getSize
    r.minimumLevel = eventTemplate.getMinimumLevel
    r.instance_key = eventTemplate.getInstanceKey
    r.boss_keys = mkList(eventTemplate.getBosses, (x: EventBoss) => x.getBossKey)
    r.roles = mkList(eventTemplate.getRoles, eventRole2JSEventRole)
    r.badges = mkList(eventTemplate.getBadges, eventBadge2JSEventBadge)
    r.modifyEvents = false
    r
  }

  implicit def instance2JSInstance(instance: Instance): JSInstance = {
    val r = new JSInstance
    r.key = instance.getKey
    r.name = instance.getName
    r
  }

  implicit def role2JSRole(role: Role): JSRole = {
    val r = new JSRole
    r.key = role.getKey
    r.name = role.getName
    r.restricted = role.getRestricted.booleanValue
    r
  }

  implicit def badge2JSBadge(badge: Badge): JSBadge = {
    val r = new JSBadge
    r.key = badge.getKey
    r.name = badge.getName
    r
  }

  implicit def boss2JSBoss(boss: Boss): JSBoss = {
    val r = new JSBoss
    r.key = boss.getKey
    r.name = boss.getName
    r
  }

  implicit def event2JSEvent(event: Event): JSEvent = {
    val r = new JSEvent

    // EventTemplate
    // XXX: JSEventTemplate.key assigned to event.key here, this is confusing
    r.key = event.getKey
    r.name = event.getName
    r.size = event.getSize
    r.minimumLevel = event.getMinimumLevel
    r.instance_key = event.getInstanceKey
    r.boss_keys = mkList(event.getEventBosses, (x: EventBoss) => x.getBossKey)
    r.roles = mkList(event.getEventRoles, eventRole2JSEventRole)
    r.badges = mkList(event.getEventBadges, eventBadge2JSEventBadge)

    // Event
    r.start_time = event.getStartTime
    r.duration = event.getDuration
    r.display_start = event.getDisplayStart
    r.display_end = event.getDisplayEnd
    r.signups_start = event.getSignupsStart
    r.signups_end = event.getSignupsEnd
    r
  }

  // not an implicit, need a way to know the event_key
  def signup2JSEventSignup(event_key: String, signup: Signup)
    : JSEventSignup = {
    val r = new JSEventSignup
    r.key = signup.getKey
    r.event_key = event_key
    r.chr_key = signup.getCharacter
    r.role_key = signup.getRole
    r.signup_type = signup.getSignupType
    r.signup_time = signup.getSignupTime
    r.note = signup.getNote
    r
  }

  implicit def event2JSEventSignups(event: Event): JSEventSignups = {
    val r = new JSEventSignups
    r.key = event.getKey
    r.signups = mkList(event.getSignups, (x: Signup) =>
      signup2JSEventSignup(r.key, x))
    r.asof = event.getLastModification
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

  private def mkList[A,B](col: java.util.Collection[A], filter: A => Boolean,
                          map: A => B)
    : java.util.List[B] = {
    val r = new java.util.ArrayList[B]
    if (col ne null) {
      val i = col.iterator
      while (i.hasNext) {
        val n = i.next
        if (filter(n)) {
          r.add(map(n))
        }
      }
    }
    r
  }

  private def mkList[A](col: Collection[A]): java.util.List[A] = {
    val r = new java.util.ArrayList[A](col.size)
    r.addAll(col)
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

  private
  def getGuildOfficers(guild: Key): java.util.Set[Key] = {
    val key = "officers-" + guild
    cached(key, { key: String =>
      transactionTemplate.execute {
        val g = goteFarmDao.getGuild(guild).getOrElse(
          throw new NotFoundError("Guild not found")
        )
        val officers = new java.util.HashSet[Key]
        officers += g.getOwner
        // FIXME: this null test can be removed once appengine bug is fixed
        val guild_officers = g.getOfficers
        if (guild_officers ne null) {
          officers ++= guild_officers
        }
        officers
      }
    })
  }

  private
  def getUserAccountKey(user: User): Key = {
    val key = "user-accout-key-" + user
    cached(key, { key: String =>
      transactionTemplate.execute {
        goteFarmDao.getAccount(user).getKey
      }
    })
  }

  private
  def isUserOfficer(user: User, guild: Key): Boolean = {
    val officers = getGuildOfficers(guild)
    officers.contains(getUserAccountKey(user))
  }

  private
  def verifyUserIsOfficer(user: User, guild: Key): Unit = {
    // don't cache the result of this test directly, because it is impractical
    // to invalidate the cache if the guild's officer list changes
    if (!isUserOfficer(user, guild)) {
      throw new NotAuthorizedException("You are not an officer of this guild")
    }
  }

  private
  def getUserChrGroupKey(user: User, guild_key: Key): Key = {
    val key = "chr-group-key-" + user + "-" + guild_key
    cached(key, { key: String =>
      val acct = goteFarmDao.getAccount(user)
      val chr_groups = acct.getChrGroups
      if (chr_groups eq null) {
        // don't want to cache a negative result
        throw new NotFoundError("User has no character groups")
      }
      chr_groups.find(_.getGuildKey == guild_key).getOrElse(
        // don't want to cache a negative result
        throw new NotFoundError("User has no character group for guild")
      ).getKey
    })
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

  override
  def setActiveGuild(user: User, guild: Key): JSGuild = {
    val r = transactionTemplate.execute {
      val g = goteFarmDao.getGuild(guild).getOrElse(
        throw new NotFoundError("Guild not found")
      )
      guild2JSGuild(g)
    }

    // set the user's active guild
    transactionTemplate.execute {
      val account = goteFarmDao.getAccount(user)

      // verify guild is in the user's list of guilds
      val guilds = account.getGuilds
      if (!guilds.contains(guild)) {
        throw new NotFoundError("Guild not found in user's guild list")
      }

      account.setActiveGuild(guild)
    }

    r
  }

  private val regionre = """(?i)(\w+)\.wowarmory\.com""".r

  override
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
      val chr_group = new ChrGroup(acct, guild_key)
      listAdd(chr_group, acct.getChrGroups, acct.setChrGroups)
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
  override
  def login(username: String, password: String) =
    goteFarmDao.validateUser(username, password)

  @Transactional{val readOnly = false}
  override
  def newUser(username: String, email: String, password: String) =
    goteFarmDao.createUser(username, email, password)
  */

  override
  def getRegions: java.util.List[JSRegion] = {
    transactionTemplate.execute {
      mkList(goteFarmDao.getRegions, region2JSRegion)
    }
  }

  override
  def getBadge(key: Key): Badge = goteFarmDao.getBadge(key).getOrElse(
    throw new NotFoundError("Badge not found")
  )

  override
  def getChrClass(key: Key): ChrClass = goteFarmDao.getChrClass(key).getOrElse(
    throw new NotFoundError
  )

  override
  def getRace(key: Key): Race = goteFarmDao.getRace(key).getOrElse(
    throw new NotFoundError
  )

  override
  def getRole(key: Key): Role = goteFarmDao.getRole(key).getOrElse(
    throw new NotFoundError("Role not found")
  )

  override
  def getGuild(key: Key): Guild = goteFarmDao.getGuild(key).getOrElse(
    throw new NotFoundError
  )

  override
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
    val race_key = transactionTemplate.execute {
      goteFarmDao.getRace(race.toString).getKey
    }

    // get (or create) the class object
    val class_key = transactionTemplate.execute {
      goteFarmDao.getChrClass(clazz.toString).getKey
    }

    transactionTemplate.execute {
      // find/create the appropriate ChrGroup
      val acct = goteFarmDao.getAccount(user)
      val chr_groups = {
        val chr_groups = acct.getChrGroups
        // FIXME: appengine null collection workaround
        if (chr_groups eq null) {
          val chr_groups = new java.util.ArrayList[ChrGroup]
          acct.setChrGroups(chr_groups)
          chr_groups
        }
        else {
          chr_groups
        }
      }

      val chr_group = chr_groups.find(_.getGuildKey == guild).getOrElse {
        val chr_group = new ChrGroup(acct, guild)
        chr_groups.add(chr_group)
        chr_group
      }

      val no_main = chr_group.getMain eq null

      // create the Chr entity
      val chr = new Chr(acct.getKey, chr_group, guild, name.toString, no_main,
                        race.toString, race_key, clazz.toString, class_key,
                        level, charxml.toString, new Date)

      // put it in the group
      val characters = {
        val characters = chr_group.getCharacters
        // FIXME: appengine null collection workaround
        if (characters eq null) {
          val characters = new java.util.ArrayList[Chr]
          chr_group.setCharacters(characters)
          characters
        }
        else {
          characters
        }
      }

      characters.add(chr)

      // make it the user's main if it is the first character
      if (no_main) {
        chr_group.setMain(chr.getName, chr.getKey)
      }

      chr
    }
  }

  override
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

  override
  def getCharacters(user: User, guild: Key) = {
    val r = new java.util.ArrayList[JSCharacter]

    try {
      val chr_group = goteFarmDao.getChrGroup(
        getUserChrGroupKey(user, guild)
      ).getOrElse(
        throw new NotFoundError("Character group not found")
      )

      val characters = chr_group.getCharacters
      // FIXME: appengine null collection bug
      if (characters ne null) {
        convertList(r).addAll(characters.map(chr2JSCharacter))
      }
    }
    catch {
      case _: NotFoundError =>
    }

    r
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getCharacter(user: User, character: Key): JSCharacter = {
    // XXX: should users only be allowed to see characters in their own
    // guild(s)? That test would be somewhat expensive.
    goteFarmDao.getCharacter(character).getOrElse(
      throw new NotFoundError("Character not found")
    )
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def setMainCharacter(user: User, guild: Key, character: Key): Unit = {
    val chr_group = goteFarmDao.getChrGroup(
      getUserChrGroupKey(user, guild)
    ).getOrElse(
      throw new NotFoundError("Character group not found")
    )

    val old_main_key = chr_group.getMainKey
    if (old_main_key != character) {
      val characters = chr_group.getCharacters
      // FIXME: appengine null collection bug
      if (characters eq null) {
        throw new NotFoundError("Character not found")
      }

      val new_main = characters.find(_.getKey == character).getOrElse(
        throw new NotFoundError("Character not found")
      )

      // need to update the ChrGroup
      chr_group.setMain(new_main.getName, new_main.getKey)

      // and change the old main Chr
      (for (old_main <- characters.find(_.getKey == old_main_key)) yield {
        old_main.setMain(false)
        Some(true)
      }).getOrElse {
        // not an error if it isn't found, it means the old_main_key is
        // invalid, indictive of a coding error somewhere else
        logger.warn(  "Old main character not found changing main for user "
                    + user + " in guild " + guild)
      }

      // and change the new main Chr
      new_main.setMain(true)
    }
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getRoles(guild: Key): java.util.List[JSRole] = {
    mkList(goteFarmDao.getRoles(guild), role2JSRole)
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def addRole(user: User, guild: Key, name: String, restricted: Boolean)
    : JSRole = {
    // TODO: make sure user is an officer of the guild
    goteFarmDao.addRole(guild, name, restricted)
  }

  override
  def updateCharacterRole(user: User, character: Key, role: Key,
                          adding: Boolean): JSCharacter = {
    val (role_name, restricted) = if (adding) {
      transactionTemplate.execute {
        val r = goteFarmDao.getRole(role).getOrElse(
          throw new NotFoundError("Role not found")
        )
        (r.getName, r.getRestricted)
      }
    }
    else {
      // not needed when removing
      (null, false)
    }

    transactionTemplate.execute {
      val chr = goteFarmDao.getCharacter(character).getOrElse(
        throw new NotFoundError("Character not found")
      )

      val acct = goteFarmDao.getAccount(chr.getAccountKey).getOrElse(
        throw new NotFoundError("Account not found")
      )

      // character must belong to user
      // TODO: or if user is an admin ...
      if (acct.getUser != user) {
        throw new IllegalArgumentException("Character does not belong to you.")
      }

      val curr_roles = chr.getRoles

      if (adding) {
        // avoid adding duplicates
        if ((curr_roles eq null) || !curr_roles.exists(_.getRoleKey == role)) {
          // XXX: disabling restriction during develoment
          val new_role = new ChrRole(role_name, role,
                                     false, true) // restricted, !restricted)
          listAdd(new_role, curr_roles, chr.setRoles)
        }
      }
      else {
        if (curr_roles ne null) {
          chr.setRoles(mkList(curr_roles.filter(_.getRoleKey != role)))
        }

        // TODO: Update last_signup_modification for any event that this
        // character is signed up for with the role being deleted.

        // TODO: Delete any signups by this character with this role.
      }

      chr
    }
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getBadges(guild: Key): java.util.List[JSBadge] = {
    mkList(goteFarmDao.getBadges(guild), badge2JSBadge)
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def addBadge(user: User, guild: Key, name: String, score: Int)
    : JSBadge = {
    // TODO: make sure user is an officer of the guild
    goteFarmDao.addBadge(guild, name, score)
  }

  override
  def updateCharacterBadge(user: User, character: Key, badge: Key,
                           adding: Boolean): JSCharacter = {

    val badge_name = key2BadgeName(badge)

    transactionTemplate.execute {
      val chr = goteFarmDao.getCharacter(character).getOrElse(
        throw new NotFoundError("Character not found")
      )

      val acct = goteFarmDao.getAccount(chr.getAccountKey).getOrElse(
        throw new NotFoundError("Account not found")
      )

      // character must belong to user
      // TODO: or if user is an admin ...
      if (acct.getUser != user) {
        throw new IllegalArgumentException("Character does not belong to you.")
      }

      val curr_badges = chr.getBadges

      if (adding) {
        // avoid adding duplicates
        if (   (curr_badges eq null)
            || !curr_badges.exists(_.getBadgeKey == badge)) {
          // XXX: disabling restriction during develoment
          val new_badge = new ChrBadge(badge_name, badge,
                                       false, true) // true, false)
          listAdd(new_badge, curr_badges, chr.setBadges)
        }
      }
      else {
        if (curr_badges ne null) {
          chr.setBadges(mkList(curr_badges.filter(_.getBadgeKey != badge)))
        }

        // TODO: Update last_signup_modification for any event that this
        // character is signed up for that has badge requirements involving
        // the badge being deleted.
      }

      chr
    }
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def addInstance(user: User, guild: Key, name: String): JSInstance = {
    // TODO: make sure user is an officer of the guild
    goteFarmDao.addInstance(guild, name)
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def addBoss(user: User, instance: Key, name: String): JSBoss = {
    // TODO: make sure user is an officer of the guild

    val i = goteFarmDao.getInstance(instance).getOrElse(
      throw new NotFoundError("Instance not found")
    )

    val nb = new Boss(name)
    listAdd(nb, i.getBosses, i.setBosses)
    nb
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getInstances(guild: Key): java.util.List[JSInstance] = {
    mkList(goteFarmDao.getInstances(guild), instance2JSInstance)
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
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
  override
  def getEventTemplate(name: String) = goteFarmDao.getEventTemplate(name)
  */

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getEventTemplates(user: User, guild: Key)
    : java.util.List[JSEventTemplate] = {
    // TODO: make sure user is an officer of the guild
    mkList(goteFarmDao.getEventTemplates(guild), eventTemplate2JSEventTemplate)
  }

  override
  def saveEventTemplate(user: User, guild: Key,
                        jset: JSEventTemplate): JSEventTemplate = {
    // TODO: make sure user is an officer of the guild
    val event_is_new = jset.key eq null

    val instance = key2InstanceName(jset.instance_key)

    // Map the badge, boss and role keys from the client to their current
    // names.
    val badge_map = new scala.collection.mutable.HashMap[String, String]
    for {
      badge <- jset.badges
      key: Key = badge.badge_key
      name = key2BadgeName(key)
    } {
      badge_map.put(badge.badge_key, name)
    }

    val boss_map = new scala.collection.mutable.HashMap[String, String]
    for {
      boss <- jset.boss_keys
      key: Key = boss
      name = key2BossName(key)
    } {
      boss_map.put(boss, name)
    }

    val role_map = new scala.collection.mutable.HashMap[String, String]
    for {
      role <- jset.roles
      key: Key = role.role_key
      name = key2RoleName(key)
    } {
      role_map.put(role.role_key, name)
    }

    transactionTemplate.execute {
      val et = if (event_is_new) {
        val et = goteFarmDao.addEventTemplate(guild, jset.name, jset.size,
                                              jset.minimumLevel, instance,
                                              jset.instance_key)
        jset.key = et.getKey
        et
      }
      else {
        val et = goteFarmDao.getEventTemplate(jset.key).getOrElse(
          throw new NotFoundError("Event not found")
        )
        et.setName(jset.name)
        et.setSize(jset.size)
        et.setMinimumLevel(jset.minimumLevel)
        et.setInstance(instance, jset.instance_key)
        et
      }

      // Update bosses
      val bosses = new java.util.ArrayList[EventBoss]
      bosses.addAll(jset.boss_keys.map(x => {
        val name = boss_map.get(x).get
        new EventBoss(name, x)
      }))
      et.setBosses(bosses)

      // Update roles
      val roles = new java.util.ArrayList[EventRole]
      roles.addAll(jset.roles.map(x => {
        val name = role_map.get(x.role_key).get
        new EventRole(name, x.role_key, x.min, x.max)
      }))
      et.setRoles(roles)

      // Update badges
      val badges = new java.util.ArrayList[EventBadge]
      badges.addAll(jset.badges.map(x => {
        val name = badge_map.get(x.badge_key).get
        new EventBadge(name, x.badge_key, x.requireForSignup, x.applyToRole,
                       x.numSlots, x.earlySignup)
      }))
      et.setBadges(badges)
    }

    if (!event_is_new && jset.modifyEvents) {
      // TODO: rebuild events
    }

    jset
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getEventSchedules(user: User, event_template: Key)
    : java.util.List[JSEventSchedule] = {
    // TODO: user needs to be ... in guild? officer?
    mkList(goteFarmDao.getEventSchedules(event_template),
           eventSchedule2JSEventSchedule)
  }

  override
  def saveEventSchedule(user: User, guild: Key, es: JSEventSchedule): Unit = {
    // TODO: check user is a guild officer
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

    // get the guild's time zone
    transactionTemplate.execute {
      val g = goteFarmDao.getGuild(guild).getOrElse(
        throw new NotFoundError("Guild not found")
      )

      es.time_zone = g.getTimeZone
      if (es.time_zone eq null) {
        throw new IllegalArgumentException("Guild time zone not set")
      }
    }

    transactionTemplate.execute {
      goteFarmDao.saveEventSchedule(guild, es)
    }

    // TODO: publish this schedule's events right away
    // publishEvents()
  }

  private def getEventNextOccurrence(event: EventSchedule): Date = {
    val tz = TimeZone.getTimeZone(event.getTimeZone)
    val cal = new GregorianCalendar(tz)
    cal.setFirstDayOfWeek(Calendar.SUNDAY)
    cal.setTime(event.getStartTime)

    event.getRepeatSize match {
      case JSEventSchedule.REPEAT_NEVER =>
        throw new IllegalStateException("Event does not repeat")

      case JSEventSchedule.REPEAT_DAILY =>
        cal.add(Calendar.DAY_OF_MONTH, event.getRepeatFreq)
        cal.getTime

      case JSEventSchedule.REPEAT_WEEKLY =>
        // look for the next day this week, or skip ahead repeat_freq weeks
        if (event.getDayMask == 0) {
          throw new IllegalStateException("Event repeats weekly but no days are set")
        }

        def scanForNextDay(cal: Calendar, allow_sunday: Boolean): Option[Date] = {
          val dom = cal.get(Calendar.DAY_OF_WEEK)

          if (!allow_sunday && dom == Calendar.SUNDAY) {
            // rolled forward into next week, no match
            None
          }
          else if ((event.getDayMask & (1<<(dom-1))) != 0) {
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
          val start_of_week = new GregorianCalendar(tz)
          start_of_week.setTime(event.getStartTime)
          start_of_week.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
          start_of_week.add(Calendar.WEEK_OF_YEAR, event.getRepeatFreq)
          scanForNextDay(start_of_week, true).getOrElse({
            throw new RuntimeException("Unexpected None in scanForNextDay")
          })
        })

      case JSEventSchedule.REPEAT_MONTHLY =>
        val orig_cal = new GregorianCalendar(tz)
        orig_cal.setFirstDayOfWeek(Calendar.SUNDAY)
        orig_cal.setTime(event.getOrigStartTime)

        event.getRepeatBy match {
          case JSEventSchedule.REPEAT_BY_DAY_OF_MONTH =>
            val dom = orig_cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.MONTH, event.getRepeatFreq)
            val max_dom = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, if (dom > max_dom) max_dom else dom)
            cal.getTime

          case JSEventSchedule.REPEAT_BY_DAY_OF_WEEK =>
            val dow = orig_cal.get(Calendar.DAY_OF_WEEK)
            val dowim = orig_cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
            cal.add(Calendar.MONTH, event.getRepeatFreq)
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

  override
  def getEvents(user: User, guild: Key): java.util.List[JSEvent] = {
    // TODO: user must belong to guild
    val now = System.currentTimeMillis
    // Cannot have more than one inequality filter in the query, so filter
    // the results further here to eliminate ones that should not be shown
    // yet.
    // Also cannot sort on startTime when the query filter is on displayEnd so
    // the events must be sorted here.
    val r = mkList(goteFarmDao.getEvents(guild),
                   (x: Event) => x.getDisplayStart.getTime <= now,
                   event2JSEvent)
    java.util.Collections.sort(r, (x: JSEvent, y: JSEvent) =>
      x.start_time.compareTo(y.start_time)
    )
    r
  }

  @Transactional{val propagation = Propagation.REQUIRED}
  override
  def getEventSignups(user: User, event_key: Key, if_changed_since: Date)
    : Option[JSEventSignups] = {
    // XXX: should users only be allowed to see signups in their own
    // guild(s)? That test would be somewhat expensive.
    val event = goteFarmDao.getEvent(event_key).getOrElse(
      throw new NotFoundError("Event not found")
    )
    if (event.getLastModification.compareTo(if_changed_since) > 0) {
      Some(event2JSEventSignups(event))
    }
    else {
      None
    }
  }

  private def validateSignup(user: User, event: Event, chr: JSCharacter,
                             role: Key): Unit = {
    // character must have roleid
    if (!chr.hasRole(role)) {
      throw new IllegalArgumentException("Character missing role.")
    }

    // signups must not be closed
    val now = System.currentTimeMillis
    if (now > event.getSignupsEnd.getTime) {
      throw new IllegalArgumentException("Signups are closed for this event.")
    }

    // character must be able to signup
    val signups_start = event.getSignupsStart.getTime
    if (now < signups_start) {
      // character needs to qualify for an early signup
      // FIXME: appengine null collection bug
      val event_badges = event.getEventBadges
      if ((event_badges eq null) || !event_badges.exists({ badge =>
           ((now >= signups_start - badge.getEarlySignup * 3600000L)
        && ((badge.getApplyToRole eq null) || badge.getApplyToRole == role)
        && chr.hasBadge(badge.getBadgeKey))
      })) {
        throw new IllegalArgumentException("Character cannot sign up for this event yet.")
      }
    }
  }

  override
  def signupForEvent(user: User, event_key: Key, character: Key, role: Key,
                     signup_type: Int): JSEventSignups = {
    val chr = transactionTemplate.execute {
      val chr = goteFarmDao.getCharacter(character).getOrElse(
        throw new NotFoundError("Character not found")
      )

      val acct = goteFarmDao.getAccount(chr.getAccountKey).getOrElse(
        throw new NotFoundError("Account not found")
      )

      // character must belong to user
      // TODO: or if user is an admin ...
      if (acct.getUser != user) {
        throw new IllegalArgumentException("Character does not belong to you.")
      }

      chr2JSCharacter(chr)
    }

    transactionTemplate.execute {
      val event = goteFarmDao.getEvent(event_key).getOrElse(
        throw new NotFoundError("Event not found")
      )

      validateSignup(user, event, chr, role)

      val now = new Date

      val signup = new Signup(character, role, signup_type, now, null)
      listAdd(signup, event.getSignups, event.setSignups)

      event.setLastModification(now)

      event
    }
  }

  /*
  @Transactional{val readOnly = false}
  override
  def changeEventSignup(uid: Long, eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit = {
    val es = goteFarmDao.getEventSignup(eventsignupid)
    validateSignup(uid, es.eventid, es.chr, new_roleid)
    goteFarmDao.changeEventSignup(eventsignupid, new_roleid, new_signup_type)
  }

  @Transactional{val readOnly = false}
  override
  def removeEventSignup(uid: Long, eventsignupid: Long): Unit = {
    // character must belong to user
    val es = goteFarmDao.getEventSignup(eventsignupid)
    if (es.chr.accountid != uid) {
      throw new IllegalArgumentException("Signup does not belong to you.")
    }
    goteFarmDao.removeEventSignup(eventsignupid)
  }
  */

  override
  def publishEvent: Boolean = {
    val reftime = System.currentTimeMillis
    // Get schedules one hour into the future. If the process to publish events
    // runs every hour, publishing events up to one hour early will make it so
    // no events are published late. The "early" events can be filtered by the
    // client. Add a pad of 5 minutes to be on the safe side.
    val active_schedules = goteFarmDao.getActiveEventSchedule(3900000L)
    val count = active_schedules.size
    logger.debug("publishEvent got " + count + " results")
    if (count == 0) {
      false
    }
    else {
      val iter = active_schedules.iterator
      val sched = iter.next
      val sched_key = sched.getKey

      if (sched.getDisplayEndDate.getTime <= reftime) {
        // time is past for showing the event, don't bother instancing
        // it
        transactionTemplate.execute {
          for (s <- goteFarmDao.getEventSchedule(sched_key)) {
            s.setActive(false)
          }
        }
        count > 1
      }
      else {
        // need a detached copy of the EventTemplate
        val et = transactionTemplate.execute {
          val et = goteFarmDao.getEventTemplate(sched.getEventTemplate)
                              .getOrElse(
            throw new NotFoundError("Event template not found")
          )
          goteFarmDao.detachCopy(et)
          et
        }

        // create it
        transactionTemplate.execute {
          goteFarmDao.publishEvent(sched, et)
        }

        // update to next time
        transactionTemplate.execute {
          (for (s <- goteFarmDao.getEventSchedule(sched_key)) yield {
            if (s.getRepeatSize != JSEventSchedule.REPEAT_NEVER) {
              s.setStartTime(getEventNextOccurrence(s))

              boolean2Boolean(
                count > 1 || sched.getDisplayStart <= reftime
              )
            }
            else {
              // one-shot event, deactive it
              s.setActive(false)
              boolean2Boolean(count > 1)
            }
          }).getOrElse(java.lang.Boolean.FALSE)
        }.booleanValue
      }
    }
  }

  private
  lazy val time_zones = {
    val ids = TimeZone.getAvailableIDs
    stableSort(ids, (x: String, y: String) => x < y)
    ids
  }

  override
  def getTimeZones: Array[String] = {
    time_zones
  }

  override
  def setTimeZone(user: User, guild: Key, time_zone: String): JSGuild = {
    verifyUserIsOfficer(user, guild)

    if (!time_zones.contains(time_zone)) {
      throw new IllegalArgumentException("Invalid time zone")
    }

    val r = transactionTemplate.execute {
      val g = goteFarmDao.getGuild(guild).getOrElse(
        throw new NotFoundError("Guild does not exist")
      )
      g.setTimeZone(time_zone)
      guild2JSGuild(g)
    }

    // all the EventSchedule entities must be updated
    for {
      es <- mkList(goteFarmDao.getGuildEventSchedules(guild))
      key = es.getKey
    } {
      transactionTemplate.execute {
        for (es <- goteFarmDao.getEventSchedule(key)) {
          es.setTimeZone(time_zone)
        }
      }
    }

    r
  }
}
