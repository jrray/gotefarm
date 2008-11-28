package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSEventBadge,
  JSEventRole,
  JSEventSchedule,
  JSEventTemplate,
  JSCharacter,
  JSRole,
  NotFoundError
}

import org.springframework.jdbc.core._
import org.springframework.jdbc.core.simple._
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.dao.IncorrectResultSizeDataAccessException

import org.apache.commons.logging.LogFactory;

import java.sql.{
  PreparedStatement,
  ResultSet
}
import java.net.{
  URL,
  URLEncoder
}
import java.util.Date

import scala.collection.jcl.Conversions._

import scala.Predef.{
  intWrapper => _,
  longWrapper => _,
  _
}

object GoteFarmJdbcDao {
  val JSCharacterMapper = new ParameterizedRowMapper[JSCharacter] {
    def mapRow(rs: ResultSet, rowNum: Int) = {
      val jsc = new JSCharacter

      jsc.cid = rs.getInt(1)
      jsc.realm = rs.getString(2)
      jsc.name = rs.getString(3)
      jsc.race = rs.getString(4)
      jsc.clazz = rs.getString(5)
      jsc.characterxml = rs.getString(6)
      jsc.created = new Date(rs.getLong(7))

      jsc
    }
  }

  val JSEventScheduleMapper = new ParameterizedRowMapper[JSEventSchedule] {
    val columns = """eventschedid, eventtmplid, start_time, duration,
                     display_start, display_end, signups_start, signups_end,
                     repeat_size, repeat_freq, day_mask, repeat_by"""

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val jses = new JSEventSchedule

      jses.esid = rs.getInt(1)
      jses.eid = rs.getInt(2)

      jses.start_time = new Date(rs.getLong(3))
      jses.duration = rs.getInt(4)

      jses.display_start = rs.getInt(5)
      jses.display_end = rs.getInt(6)

      jses.signups_start = rs.getInt(7)
      jses.signups_end = rs.getInt(8)

      jses.repeat_size = rs.getInt(9)
      jses.repeat_freq = rs.getInt(10)
      jses.day_mask = rs.getInt(11)
      jses.repeat_by = rs.getInt(12)

      jses
    }
  }
}

class GoteFarmJdbcDao extends SimpleJdbcDaoSupport
  with GoteFarmDaoT {
  import GoteFarmJdbcDao._

  private def tableExists(name: String): Boolean = {
    try {
      getSimpleJdbcTemplate().queryForObject(
        """SELECT name FROM sqlite_master WHERE type='table' and name = ?""",
        classOf[java.lang.String],
        Array[AnyRef](name)
      )
      true
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        false
    }
  }

  def generateTables() = {
    val jdbc = getSimpleJdbcTemplate().getJdbcOperations()

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS account (
        accountid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        username TEXT UNIQUE NOT NULL,
        email TEXT NOT NULL,
        password TEXT NOT NULL,
        admin INTEGER NOT NULL,
        created INTEGER NOT NULL,
        lastevent INTEGER
      )"""
    )

    if (!tableExists("race")) {
      jdbc.execute(
        """CREATE TABLE race (
          raceid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL
        )"""
      )

      val races = Array(
        "Draenei",
        "Dwarf",
        "Gnome",
        "Human",
        "Night Elf",
        "Blood Elf",
        "Orc",
        "Tauren",
        "Troll",
        "Undead"
      )

      jdbc.batchUpdate(
        """INSERT INTO race (name) VALUES (?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = races.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, races(i))
          }
        }
      )
    }

    if (!tableExists("class")) {
      jdbc.execute(
        """CREATE TABLE class (
          classid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL
        )"""
      )

      val classes = Array(
        "Druid",
        "Hunter",
        "Mage",
        "Paladin",
        "Priest",
        "Rogue",
        "Shaman",
        "Warlock",
        "Warrior"
      )

      jdbc.batchUpdate(
        """INSERT INTO class (name) VALUES (?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = classes.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, classes(i))
          }
        }
      )
    }

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS character (
        characterid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        accountid INTEGER NOT NULL,
        realm TEXT NOT NULL,
        name TEXT NOT NULL,
        raceid INTEGER NOT NULL,
        classid INTEGER NOT NULL,
        characterxml TEXT,
        created INTEGER NOT NULL,
        UNIQUE(realm,name)
      )"""
    )

    if (!tableExists("role")) {
      // A restricted role cannot be self-assigned to a character.
      // An admin can add restricted roles to any character.
      jdbc.execute(
        """CREATE TABLE role (
          roleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL,
          restricted INTEGER NOT NULL
        )"""
      )

      val roles = Array(
        "Tank" -> 0,
        "Healer" -> 0,
        "DPS" -> 0,
        "AoE DPS" -> 0,
        "Mage Tank (Krosh)" -> 1,
        "Warlock Tank (Leotheras)" -> 1,
        "Nature Resist Tank (Hydross)" -> 1,
        "Frost Resist Tank (Hydross)" -> 1,
        "Nature/Frost Resist Tank (Hydross adds)" -> 1
      )

      jdbc.batchUpdate(
        """INSERT INTO role (name,restricted) VALUES (?,?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = roles.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, roles(i)._1)
            ps.setInt(2, roles(i)._2)
          }
        }
      )
    }

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS chrrole (
        chrroleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        characterid INTEGER NOT NULL,
        roleid INTEGER NOT NULL,
        approved INTEGER NOT NULL
      )"""
    )

    if (!tableExists("badge")) {
      // A badge is like a role, it is an attribute that a character can earn.
      // The score per badge is an arbitrary, unitless value used to compare
      // the relative worth of badges.
      jdbc.execute(
        """CREATE TABLE badge (
          badgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL,
          score INTEGER NOT NULL
        )"""
      )

      val badges = Array(
        "Karazhan Geared" -> 300,
        "Gruul/Mag Geared" -> 400,
        "ZA Geared" -> 450,
        "SSC/TK Geared" -> 500,
        "BT/HYJ Geared" -> 600,
        "Sunwell Geared" -> 700
      )

      jdbc.batchUpdate(
        """INSERT INTO badge (name,score) VALUES (?,?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = badges.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, badges(i)._1)
            ps.setInt(2, badges(i)._2)
          }
        }
      )
    }

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS chrbadge (
        chrbadgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        characterid INTEGER NOT NULL,
        badgeid INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmpl (
        eventtmplid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT UNIQUE NOT NULL,
        size INTEGER NOT NULL,
        minimum_level INTEGER NOT NULL,
        instanceid INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmplrole (
        eventtmplroleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventtmplid INTEGER NOT NULL,
        roleid INTEGER NOT NULL,
        min_count INTEGER NOT NULL CHECK (min_count >= 0),
        max_count INTEGER NOT NULL CHECK (max_count > 0)
      )"""
    )

    // event badge requirements
    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmplbadge (
        eventtmplbadgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventtmplid INTEGER NOT NULL,
        badgeid INTEGER NOT NULL,
        require_for_signup INTEGER NOT NULL,
        roleid INTEGER,
        num_slots INTEGER NOT NULL,
        early_signup INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS instance (
        instanceid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT UNIQUE NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS boss (
        bossid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        instanceid INTEGER NOT NULL,
        name TEXT NOT NULL,
        UNIQUE(instanceid, name)
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmplboss (
        eventtmplbossid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventtmplid INTEGER NOT NULL,
        bossid INTEGER NOT NULL,
        UNIQUE(eventtmplid, bossid)
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventsched (
        eventschedid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventtmplid INTEGER NOT NULL,
        active INTEGER NOT NULL,
        start_time INTEGER NOT NULL,
        duration INTEGER NOT NULL,
        display_start INTEGER NOT NULL,
        display_end INTEGER NOT NULL,
        signups_start INTEGER NOT NULL,
        signups_end INTEGER NOT NULL,
        repeat_size INTEGER NOT NULL DEFAULT 0,
        repeat_freq INTEGER NOT NULL DEFAULT 0,
        day_mask INTEGER NOT NULL DEFAULT 0,
        repeat_by INTEGER NOT NULL DEFAULT 0
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS event (
        eventid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT UNIQUE NOT NULL,
        size INTEGER NOT NULL,
        minimum_level INTEGER NOT NULL,
        instanceid INTEGER NOT NULL,
        start_time INTEGER NOT NULL,
        duration INTEGER NOT NULL,
        display_start INTEGER NOT NULL,
        display_end INTEGER NOT NULL,
        signups_start INTEGER NOT NULL,
        signups_end INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventrole (
        eventroleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventid INTEGER NOT NULL,
        roleid INTEGER NOT NULL,
        min_count INTEGER NOT NULL CHECK (min_count >= 0),
        max_count INTEGER NOT NULL CHECK (max_count > 0)
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventbadge (
        eventbadgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventid INTEGER NOT NULL,
        badgeid INTEGER NOT NULL,
        require_for_signup INTEGER NOT NULL,
        roleid INTEGER,
        num_slots INTEGER NOT NULL,
        early_signup INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventboss (
        eventbossid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventid INTEGER NOT NULL,
        bossid INTEGER NOT NULL,
        UNIQUE(eventid, bossid)
      )"""
    )
  }

  def validateUser(username: String, password: String) = {
    val jdbc = getSimpleJdbcTemplate()
    val acct = jdbc.query(
      "select accountid, password from account where username = ?",
      new ParameterizedRowMapper[(Int, String)] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          (rs.getInt(1), rs.getString(2))
        }
      },
      Array[AnyRef](username)
    )

    if (acct.isEmpty) {
      throw new InvalidCredentialsError
    }

    // validate password
    if (!BCrypt.checkpw(password, acct(0)._2)) {
      throw new InvalidCredentialsError
    }

    acct(0)._1
  }

  def createUser(username: String, email: String, password: String) = {
    val crypt = BCrypt.hashpw(password, BCrypt.gensalt(12));

    val jdbc = getSimpleJdbcTemplate()

    try {
      jdbc.update(
        """insert into account (username, email, password, admin, created)
                        values (?,        ?,     ?,        ?,     ?      )""",
        Array[AnyRef](username, email, crypt, 0, System.currentTimeMillis())
      )
    }
    catch {
      case _: UncategorizedSQLException =>
        throw new AlreadyExistsError("User '" + username + "' already exists.")
    }

    jdbc.queryForInt(
      "select accountid from account where username = ?",
      Array[AnyRef](username)
    )
  }

  private def getRaceId(race: String): Int = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.queryForInt(
        "select raceid from race where name = ?",
        Array[AnyRef](race)
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        jdbc.update(
          "insert into race (name) values (?)",
          Array[AnyRef](race)
        )
        getRaceId(race)
    }
  }

  private def getClassId(clazz: String): Int = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.queryForInt(
        "select classid from class where name = ?",
        Array[AnyRef](clazz)
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        jdbc.update(
          "insert into class (name) values (?)",
          Array[AnyRef](clazz)
        )
        getRaceId(clazz)
    }
  }

  def createCharacter(uid: Int, realm: String, character: String) = {
    val jdbc = getSimpleJdbcTemplate()

    try {
      val cid = jdbc.queryForInt(
        "select characterid from character where realm = ? and name = ?",
        Array[AnyRef](realm, character)
      )

      throw new AlreadyExistsError("Character '" + character + "' already exists.")
    }
    catch {
      case _ =>
    }

    val conn = new URL("http://www.wowarmory.com/character-sheet.xml?r=" +
      URLEncoder.encode(realm, "UTF-8") + "&n=" +
      URLEncoder.encode(character, "UTF-8")).openConnection

    conn.setAllowUserInteraction(false)
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

    val charxml = scala.xml.XML.load(conn.getInputStream())

    val charInfo = charxml \ "characterInfo"

    // check if the character was found
    if (!(charInfo \ "@errCode").isEmpty) {
      throw new NotFoundError("Character '" + character + "' not found.")
    }

    val char = charInfo \ "character"

    val race = char \ "@race"
    val clazz = char \ "@class"

    val raceid = getRaceId(race.toString)
    val classid = getClassId(clazz.toString)

    try {
      jdbc.update(
        """insert into character (accountid, realm, name, raceid, classid, characterxml, created)
                          values (?,         ?,     ?,    ?,      ?,       ?,            ?      )""",
        Array[AnyRef](uid, realm, character, raceid, classid, charxml.toString, System.currentTimeMillis)
      )
    }
    catch {
      case _: UncategorizedSQLException =>
        throw new AlreadyExistsError("Character '" + character + "' already exists.")
    }

    jdbc.queryForInt(
      "select characterid from character where realm = ? and name = ?",
      Array[AnyRef](realm, character)
    )
  }

  def getCharacters(uid: Int) = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.query(
      """select characterid, realm, character.name, race.name, class.name, characterxml, created
          from character, race, class
          where character.raceid = race.raceid
            and character.classid = class.classid
            and accountid = ?""",
      JSCharacterMapper,
      Array[AnyRef](uid)
    )
  }

  def getCharacter(cid: Int) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.queryForObject(
        """select characterid, realm, character.name, race.name, class.name, characterxml, created
            from character, race, class
            where character.raceid = race.raceid
              and character.classid = class.classid
              and characterid = ?""",
        JSCharacterMapper,
        Array[AnyRef](cid)
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Character not found.")
    }
  }

  def getRoles = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select roleid, name, restricted from role order by name",
      new ParameterizedRowMapper[JSRole] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          val jsrole = new JSRole
          jsrole.roleid = rs.getInt(1)
          jsrole.name = rs.getString(2)
          jsrole.restricted = rs.getInt(3) != 0
          jsrole
        }
      },
      Array[AnyRef]()
    )
  }

  def addRole(name: String, restricted: Boolean) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into role (name, restricted)
                     values (?,    ?         )""",
        Array[AnyRef](name, if (restricted) 1 else 0)
      )
    }
    catch {
      case _: UncategorizedSQLException =>
        throw new AlreadyExistsError("Role '" + name + "' already exists.")
    }

    jdbc.queryForInt(
      "select roleid from role where name = ?",
      Array[AnyRef](name)
    )
  }

  def getBadges = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select name from badge order by name",
      new ParameterizedRowMapper[String] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          rs.getString(1)
        }
      },
      Array[AnyRef]()
    )
  }

  def addBadge(name: String, score: Int) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into badge (name, score)
                      values (?,    ?    )""",
        Array[AnyRef](name, score)
      )
    }
    catch {
      case _: UncategorizedSQLException =>
        throw new AlreadyExistsError("Badge '" + name + "' already exists.")
    }

    jdbc.queryForInt(
      "select badgeid from badge where name = ?",
      Array[AnyRef](name)
    )
  }

  def addInstance(name: String) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into instance (name)
                         values (?   )""",
        Array[AnyRef](name)
      )
    }
    catch {
      case _: UncategorizedSQLException =>
        throw new AlreadyExistsError("Instance '" + name + "' already exists.")
    }

    jdbc.queryForInt(
      "select instanceid from instance where name = ?",
      Array[AnyRef](name)
    )
  }

  def addBoss(instance: String, boss: String) = {
    val jdbc = getSimpleJdbcTemplate()

    val iid = try {
      getInstanceId(instance)
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Instance '" + instance + "' not found.")
    }

    val parms = Array[AnyRef](iid, boss)

    try {
      jdbc.update(
        """insert into boss (instanceid, name)
                     values (?,          ?   )""",
        parms
      )
    }
    catch {
      case _: UncategorizedSQLException =>
        throw new AlreadyExistsError("Boss '" + boss + "' already exists.")
    }

    jdbc.queryForInt(
      "select bossid from boss where instanceid = ? and name = ?",
      parms
    )
  }

  def getInstances = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select name from instance order by name",
      new ParameterizedRowMapper[String] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          rs.getString(1)
        }
      },
      Array[AnyRef]()
    )
  }

  def getInstanceId(instance: String) = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.queryForInt(
      "select instanceid from instance where name = ?",
      Array[AnyRef](instance)
    )
  }

  def getInstanceBosses(instance: String) = {
    val jdbc = getSimpleJdbcTemplate()

    try {
      val iid = getInstanceId(instance)
      jdbc.query(
        "select name from boss where instanceid = ? order by name",
        new ParameterizedRowMapper[String] {
          def mapRow(rs: ResultSet, rowNum: Int) = {
            rs.getString(1)
          }
        },
        Array[AnyRef](iid)
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        new java.util.ArrayList[String]()
    }
  }

  def getEventTemplate(name: String) = {
    val jdbc = getSimpleJdbcTemplate()

    val jset = try {
      jdbc.queryForObject(
        """select eventtmplid, size, minimum_level, instance.name
            from eventtmpl join instance using (instanceid)
            where eventtmpl.name = ?""",
        new ParameterizedRowMapper[JSEventTemplate] {
          def mapRow(rs: ResultSet, rowNum: Int) = {
            val et = new JSEventTemplate

            et.eid = rs.getInt(1)
            et.name = name
            et.size = rs.getInt(2)
            et.minimumLevel = rs.getInt(3)
            et.instance = rs.getString(4)

            et.bosses = new java.util.ArrayList[String]
            et.roles = new java.util.ArrayList[JSEventRole]
            et.badges = new java.util.ArrayList[JSEventBadge]

            et
          }
        },
        Array[AnyRef](name)
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Template '" + name + "' not found.")
    }

    val ops = jdbc.getJdbcOperations()

    ops.query(
      """select boss.name
          from boss join eventtmplboss using (bossid)
          where eventtmplid = ?""",
      Array[AnyRef](jset.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          jset.bosses.add(rs.getString(1))
        }
      }
    )

    ops.query(
      """select role.name, min_count, max_count
          from role join eventtmplrole using (roleid)
          where eventtmplid = ?""",
      Array[AnyRef](jset.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          val ev = new JSEventRole

          ev.name = rs.getString(1)
          ev.min = rs.getInt(2)
          ev.max = rs.getInt(3)

          jset.roles.add(ev)
        }
      }
    )

    ops.query(
      """select badge.name, require_for_signup, role.name, num_slots, early_signup
          from eventtmplbadge left join role using (roleid), badge
          where eventtmplbadge.badgeid = badge.badgeid and eventtmplid = ?""",
      Array[AnyRef](jset.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          val eb = new JSEventBadge

          eb.name = rs.getString(1)
          eb.requireForSignup = if (rs.getInt(2) != 0) true else false
          eb.applyToRole = rs.getString(3)
          eb.numSlots = rs.getInt(4)
          eb.earlySignup = rs.getInt(5)

          jset.badges.add(eb)
        }
      }
    )

    jset
  }

  def getEventTemplates = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.query(
      "select name from eventtmpl order by name",
      new ParameterizedRowMapper[String] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          rs.getString(1)
        }
      },
      Array[AnyRef]()
    )
  }

  def saveEventTemplate(et: JSEventTemplate) = {
    val jdbc = getSimpleJdbcTemplate()

    val iid = getInstanceId(et.instance)

    if (et.eid == -1) {
      try {
        jdbc.update(
          """insert into eventtmpl (name, size, minimum_level, instanceid)
                            values (?,    ?,    ?,             ?         )""",
          Array[AnyRef](et.name, et.size, et.minimumLevel, iid)
        )

        et.eid = jdbc.queryForInt(
          "select eventtmplid from eventtmpl where name = ?",
          Array[AnyRef](et.name)
        )
      }
      catch {
        case _: UncategorizedSQLException =>
          throw new AlreadyExistsError("Template '" + et.name + "' already exists.")
      }
    }
    else {
      try {
        val c = jdbc.update(
          """update eventtmpl set name = ?, size = ?, minimum_level = ?, instanceid = ? where eventtmplid = ?""",
          Array[AnyRef](et.name, et.size, et.minimumLevel, iid, et.eid)
        )

        if (c == 0) {
          throw new NotFoundError("Template " + et.eid + " not found.")
        }
      }
      catch {
        case _: UncategorizedSQLException =>
          throw new AlreadyExistsError("Template '" + et.name + "' already exists.")
      }
    }

    jdbc.update(
      """delete from eventtmplboss where eventtmplid = ?""",
      Array[AnyRef](et.eid)
    )

    for (boss <- et.bosses) {
      val bossid = try {
        jdbc.queryForInt(
          "select bossid from boss where instanceid = ? and name = ?",
          Array[AnyRef](iid, boss)
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Boss '" + boss + "' not found.")
      }

      jdbc.update(
        """insert into eventtmplboss (eventtmplid, bossid)
                              VALUES (?,           ?     )""",
        Array[AnyRef](et.eid, bossid)
      )
    }

    jdbc.update(
      """delete from eventtmplrole where eventtmplid = ?""",
      Array[AnyRef](et.eid)
    )

    for (role <- et.roles) {
      val roleid = try {
        jdbc.queryForInt(
          "select roleid from role where name = ?",
          Array[AnyRef](role.name)
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Role '" + role.name + "' not found.")
      }

      jdbc.update(
        """insert into eventtmplrole (eventtmplid, roleid, min_count, max_count)
                              VALUES (?,           ?,      ?,         ?        )""",
        Array[AnyRef](et.eid, roleid, role.min, role.max)
      )
    }

    jdbc.update(
      """delete from eventtmplbadge where eventtmplid = ?""",
      Array[AnyRef](et.eid)
    )

    // XXX: JCL wrapper creating weird compiler errors for et.badges
    val iter = et.badges.iterator
    while (iter.hasNext) {
      val badge = iter.next

      val badgeid = try {
        jdbc.queryForInt(
          "select badgeid from badge where name = ?",
          Array[AnyRef](badge.name)
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Badge '" + badge.name + "' not found.")
      }

      val roleid: Option[java.lang.Integer] = if (badge.applyToRole ne null) {
        try {
          Some(jdbc.queryForInt(
            "select roleid from role where name = ?",
            Array[AnyRef](badge.applyToRole)
          ))
        }
        catch {
          case _: IncorrectResultSizeDataAccessException =>
            throw new NotFoundError("Role '" + badge.applyToRole + "' not found.")
        }
      }
      else {
        None
      }

      jdbc.update(
        """insert into eventtmplbadge (eventtmplid, badgeid, require_for_signup, roleid, num_slots, early_signup)
                               VALUES (?,           ?,       ?,                  ?,      ?,         ?           )""",
        Array[AnyRef](et.eid, badgeid, if (badge.requireForSignup) 1 else 0, roleid.getOrElse(null), badge.numSlots, badge.earlySignup)
      )

      ()
    }

    et.eid
  }

  def getEventSchedules(name: String) = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSEventScheduleMapper.columns +
      """ from eventsched join eventtmpl using (eventtmplid)
          where eventtmpl.name = ?
          order by start_time""",
      JSEventScheduleMapper,
      Array[AnyRef](name)
    )
  }
}
