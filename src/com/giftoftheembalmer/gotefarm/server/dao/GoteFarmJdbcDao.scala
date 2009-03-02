package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSBadge,
  JSChrBadge,
  JSChrRole,
  JSEvent,
  JSEventBadge,
  JSEventRole,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSCharacter,
  JSRole,
  NotFoundError
}

import org.springframework.jdbc.core._
import org.springframework.jdbc.core.simple._
import org.springframework.dao.{
  DataIntegrityViolationException,
  IncorrectResultSizeDataAccessException
}

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
  val noargs = new Array[AnyRef](0)

  def boolchar(expr: Boolean): String = {
    if (expr) "Y" else "N"
  }

  def charbool(str: String): Boolean = {
    if (str == "Y") true else false
  }

  val JSCharacterMapper = new ParameterizedRowMapper[JSCharacter] {
    val ncolumns = 9
    val columns = """chr.accountid, chr.chrid, chr.realm, chr.name,
                      race.name, class.name, chr.level, chr.chrxml,
                      chr.created"""
    val tables = """chr join race on chr.raceid = race.raceid
                        join class on chr.classid = class.classid"""

    def mapRow(rs: ResultSet, rowNum: Int, start_column: Int) = {
      val jsc = new JSCharacter

      jsc.accountid = rs.getLong(start_column)
      jsc.cid = rs.getLong(start_column+1)
      jsc.realm = rs.getString(start_column+2)
      jsc.name = rs.getString(start_column+3)
      jsc.race = rs.getString(start_column+4)
      jsc.clazz = rs.getString(start_column+5)
      jsc.level = rs.getShort(start_column+6)
      jsc.characterxml = rs.getString(start_column+7)
      jsc.created = rs.getTimestamp(start_column+8)

      jsc
    }

    def mapRow(rs: ResultSet, rowNum: Int) = {
      mapRow(rs, rowNum, 1)
    }
  }

  val JSRoleMapper = new ParameterizedRowMapper[JSRole] {
    val ncolumns = 3
    val columns = "role.roleid, role.name, role.restricted"

    def mapRow(rs: ResultSet, rowNum: Int, start_column: Int) = {
      val r = new JSRole
      r.roleid = rs.getLong(start_column)
      r.name = rs.getString(start_column+1)
      r.restricted = charbool(rs.getString(start_column+2))
      r
    }

    def mapRow(rs: ResultSet, rowNum: Int) = {
      mapRow(rs, rowNum, 1)
    }
  }

  val JSBadgeMapper = new ParameterizedRowMapper[JSBadge] {
    val columns = "badgeid, name, score"

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val r = new JSBadge
      r.badgeid = rs.getLong(1)
      r.name = rs.getString(2)
      r.score = rs.getInt(3)
      r
    }
  }

  val JSChrRoleMapper = new ParameterizedRowMapper[JSChrRole] {
    val columns = """role.roleid, name, restricted, chrroleid, waiting,
      approved, message"""

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val r = new JSChrRole
      r.roleid = rs.getLong(1)
      r.name = rs.getString(2)
      r.restricted = charbool(rs.getString(3))
      r.chrroleid = rs.getLong(4)
      // force these to sensible defaults if this role is not restricted
      r.waiting = if (r.restricted) charbool(rs.getString(5)) else false
      r.approved = if (r.restricted) charbool(rs.getString(6)) else true
      r.message = rs.getString(7)
      r
    }
  }

  val JSChrBadgeMapper = new ParameterizedRowMapper[JSChrBadge] {
    val columns = """badge.badgeid, name, score, chrbadgeid, waiting,
      approved, message"""

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val r = new JSChrBadge
      r.badgeid = rs.getLong(1)
      r.name = rs.getString(2)
      r.score = rs.getInt(3)
      r.chrbadgeid = rs.getLong(4)
      r.waiting = charbool(rs.getString(5))
      r.approved = charbool(rs.getString(6))
      r.message = rs.getString(7)
      r
    }
  }

  val JSEventScheduleMapper = new ParameterizedRowMapper[JSEventSchedule] {
    val columns = """eventschedid, eventsched.eventtmplid,
                     start_time, orig_start_time,
                     timezone_offset, duration,
                     display_start, display_end, signups_start, signups_end,
                     repeat_size, repeat_freq, day_mask, repeat_by, active"""

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val jses = new JSEventSchedule

      jses.esid = rs.getLong(1)
      jses.eid = rs.getLong(2)

      jses.start_time = rs.getTimestamp(3)
      jses.orig_start_time = rs.getTimestamp(4)
      jses.timezone_offset = rs.getInt(5)
      jses.duration = rs.getInt(6)

      jses.display_start = rs.getInt(7)
      jses.display_end = rs.getInt(8)

      jses.signups_start = rs.getInt(9)
      jses.signups_end = rs.getInt(10)

      jses.repeat_size = rs.getInt(11)
      jses.repeat_freq = rs.getInt(12)
      jses.day_mask = rs.getInt(13)
      jses.repeat_by = rs.getInt(14)

      jses.active = charbool(rs.getString(15))

      jses
    }
  }

  val JSEventTemplateMapper = new ParameterizedRowMapper[JSEventTemplate] {
    val columns = """eventtmplid, eventtmpl.name, size, minimum_level, instance.name"""
    val tables = """eventtmpl join instance on eventtmpl.instanceid = instance.instanceid"""

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val et = new JSEventTemplate

      et.eid = rs.getLong(1)
      et.name = rs.getString(2)
      et.size = rs.getInt(3)
      et.minimumLevel = rs.getInt(4)
      et.instance = rs.getString(5)

      et.bosses = new java.util.ArrayList[String]
      et.roles = new java.util.ArrayList[JSEventRole]
      et.badges = new java.util.ArrayList[JSEventBadge]

      et
    }
  }

  val JSEventMapper = new ParameterizedRowMapper[JSEvent] {
    val columns = """eventid, event.name, size, minimum_level,
      instance.name, start_time, duration, display_start, display_end,
      signups_start, signups_end"""
    val tables = """event join instance on event.instanceid = instance.instanceid"""

    def mapRow(rs: ResultSet, rowNum: Int) = {
      val e = new JSEvent

      e.eid = rs.getLong(1)
      e.name = rs.getString(2)
      e.size = rs.getInt(3)
      e.minimumLevel = rs.getInt(4)
      e.instance = rs.getString(5)
      e.start_time = rs.getTimestamp(6)
      e.duration = rs.getInt(7)
      e.display_start = rs.getTimestamp(8)
      e.display_end = rs.getTimestamp(9)
      e.signups_start = rs.getTimestamp(10)
      e.signups_end = rs.getTimestamp(11)

      e.bosses = new java.util.ArrayList[String]
      e.roles = new java.util.ArrayList[JSEventRole]
      e.badges = new java.util.ArrayList[JSEventBadge]

      e
    }
  }
}

class GoteFarmJdbcDao extends SimpleJdbcDaoSupport
  with GoteFarmDaoT {
  import GoteFarmJdbcDao._

  def generateTables() = {
    val jdbc = getSimpleJdbcTemplate
    val jdbco = jdbc.getJdbcOperations

    val schema_name = "gotefarm"

    var schema_vers = try {
      jdbc.queryForLong(
        """select version from schema_vers where schema_name = ? for update""",
        Array[AnyRef](schema_name): _*
      )
    }
    catch {
      case e: org.springframework.jdbc.BadSqlGrammarException
        if e.getMessage.contains("does not exist") =>
          // create the schema_vers table
          jdbco.execute(
            """CREATE TABLE schema_vers (
                schema_name VARCHAR(32) NOT NULL PRIMARY KEY,
                version BIGINT NOT NULL
              )"""
          )

          jdbc.update(
            """INSERT INTO schema_vers
                (schema_name, version) VALUES (?, ?)""",
            Array[AnyRef](schema_name, 0L): _*
          )

          0L
    }

    var prev_schema_vers: Option[Long] = None
    var new_schema_vers: Option[Long] = None

    def migrate_to_vers(vers: Long)(f: => Unit) = {
      for {
        last_version <- prev_schema_vers
        if vers <= last_version
      } {
        throw new RuntimeException(  "Migration version "
                                   + vers
                                   + " less or equal to previous version "
                                   + last_version)
      }

      if (schema_vers < vers) {
        logger.info(  "Migrating database to version " + vers
                    + " (currently at " + schema_vers + ")")
        schema_vers = vers
        new_schema_vers = Some(vers)
        f
      }

      prev_schema_vers = Some(vers)
    }

    migrate_to_vers(200811280139L) {
      jdbco.execute(
        """CREATE TABLE account (
          accountid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          username VARCHAR(16) UNIQUE NOT NULL,
          email VARCHAR(64) NOT NULL,
          password VARCHAR(64) NOT NULL,
          admin CHAR(1) NOT NULL CONSTRAINT account_admin_bool CHECK (admin in ('Y', 'N')),
          created TIMESTAMP NOT NULL,
          lastevent TIMESTAMP
        )"""
      )

      jdbco.execute(
        """CREATE TABLE race (
          raceid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          name VARCHAR(32) UNIQUE NOT NULL
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

      jdbco.batchUpdate(
        """INSERT INTO race (name) VALUES (?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = races.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, races(i))
          }
        }
      )

      jdbco.execute(
        """CREATE TABLE class (
          classid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          name VARCHAR(32) UNIQUE NOT NULL
        )"""
      )

      val classes = Array(
        "Death Knight",
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

      jdbco.batchUpdate(
        """INSERT INTO class (name) VALUES (?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = classes.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, classes(i))
          }
        }
      )

      jdbco.execute(
        """CREATE TABLE chr (
          chrid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          accountid BIGINT NOT NULL,
          realm VARCHAR(32) NOT NULL,
          name VARCHAR(32) NOT NULL,
          raceid BIGINT NOT NULL,
          classid BIGINT NOT NULL,
          level INTEGER NOT NULL,
          chrxml LONG VARCHAR,
          created TIMESTAMP NOT NULL,
          CONSTRAINT character_realm_name_unique UNIQUE (realm, name),
          CONSTRAINT character_accountid_fk FOREIGN KEY (accountid) REFERENCES account (accountid) ON DELETE CASCADE,
          CONSTRAINT character_raceid_fk FOREIGN KEY (raceid) REFERENCES race (raceid) ON DELETE RESTRICT,
          CONSTRAINT character_classid_fk FOREIGN KEY (classid) REFERENCES class (classid) ON DELETE RESTRICT
        )"""
      )

      // A restricted role cannot be self-assigned to a character.
      // An admin can add restricted roles to any character.
      jdbco.execute(
        """CREATE TABLE role (
          roleid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          name VARCHAR(128) UNIQUE NOT NULL,
          restricted CHAR(1) NOT NULL CONSTRAINT role_restricted_bool CHECK (restricted in ('Y', 'N'))
        )"""
      )

      val roles = Array(
        "Tank" -> 0,
        "Healer" -> 0,
        "DPS" -> 0,
        "AoE DPS" -> 0
      )

      jdbco.batchUpdate(
        """INSERT INTO role (name,restricted) VALUES (?,?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = roles.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, roles(i)._1)
            ps.setString(2, boolchar(roles(i)._2 == 1))
          }
        }
      )

      jdbco.execute(
        """CREATE TABLE chrrole (
          chrroleid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          chrid BIGINT NOT NULL,
          roleid BIGINT NOT NULL,
          waiting CHAR(1) NOT NULL DEFAULT 'N' CONSTRAINT chrrole_waiting_bool CHECK (waiting in ('Y','N')),
          approved CHAR(1) NOT NULL DEFAULT 'N' CONSTRAINT chrrole_approved_bool CHECK (approved in ('Y','N')),
          message VARCHAR(2048),
          CONSTRAINT chrrole_chrid_roleid_unique UNIQUE (chrid, roleid),
          CONSTRAINT chrrole_chrid_fk FOREIGN KEY (chrid) REFERENCES chr (chrid) ON DELETE CASCADE,
          CONSTRAINT chrrole_roleid_fk FOREIGN KEY (roleid) REFERENCES role (roleid) ON DELETE RESTRICT
        )"""
      )

      // A badge is like a role, it is an attribute that a character can earn.
      // The score per badge is an arbitrary, unitless value used to compare
      // the relative worth of badges.
      jdbco.execute(
        """CREATE TABLE badge (
          badgeid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          name VARCHAR(128) UNIQUE NOT NULL,
          score INTEGER NOT NULL
        )"""
      )

      val badges = Array(
        "Tier 7 Geared" -> 100,
        "Tier 7.5 Geared" -> 150,
        "Tier 8 Geared" -> 200
      )

      jdbco.batchUpdate(
        """INSERT INTO badge (name,score) VALUES (?,?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = badges.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, badges(i)._1)
            ps.setInt(2, badges(i)._2)
          }
        }
      )

      jdbco.execute(
        """CREATE TABLE chrbadge (
          chrbadgeid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          chrid BIGINT NOT NULL,
          badgeid BIGINT NOT NULL,
          waiting CHAR(1) NOT NULL DEFAULT 'N' CONSTRAINT chrbadge_waiting_bool CHECK (waiting in ('Y','N')),
          approved CHAR(1) NOT NULL DEFAULT 'N' CONSTRAINT chrbadge_approved_bool CHECK (approved in ('Y','N')),
          message VARCHAR(2048),
          CONSTRAINT chrbadge_chrid_badgeid_unique UNIQUE (chrid, badgeid),
          CONSTRAINT chrbadge_chrid_fk FOREIGN KEY (chrid) REFERENCES chr (chrid) ON DELETE CASCADE,
          CONSTRAINT chrbadge_badgeid_fk FOREIGN KEY (badgeid) REFERENCES badge (badgeid) ON DELETE RESTRICT
        )"""
      )

      jdbco.execute(
        """CREATE TABLE instance (
          instanceid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          name VARCHAR(64) UNIQUE NOT NULL
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventtmpl (
          eventtmplid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          name VARCHAR(128) UNIQUE NOT NULL,
          size INTEGER NOT NULL,
          minimum_level INTEGER NOT NULL,
          instanceid BIGINT NOT NULL,
          CONSTRAINT eventtmpl_instanceid_fk FOREIGN KEY (instanceid) REFERENCES instance (instanceid) ON DELETE RESTRICT
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventtmplrole (
          eventtmplroleid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventtmplid BIGINT NOT NULL,
          roleid BIGINT NOT NULL,
          min_count INTEGER NOT NULL CONSTRAINT eventtmplrole_min_count_ge_zero CHECK (min_count >= 0),
          max_count INTEGER NOT NULL CONSTRAINT eventtmplrole_max_count_gt_zero CHECK (max_count > 0),
          CONSTRAINT eventtmplrole_eventtmplid_fk FOREIGN KEY (eventtmplid) REFERENCES eventtmpl (eventtmplid) ON DELETE CASCADE,
          CONSTRAINT eventtmplrole_roleid_fk FOREIGN KEY (roleid) REFERENCES role (roleid) ON DELETE RESTRICT
        )"""
      )

      // event badge requirements
      jdbco.execute(
        """CREATE TABLE eventtmplbadge (
          eventtmplbadgeid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventtmplid BIGINT NOT NULL,
          badgeid BIGINT NOT NULL,
          require_for_signup CHAR(1) NOT NULL CONSTRAINT eventtmplbadge_require_for_signup_bool CHECK (require_for_signup in ('Y','N')),
          roleid BIGINT,
          num_slots INTEGER NOT NULL,
          early_signup INTEGER NOT NULL,
          CONSTRAINT eventtmplbadge_eventtmplid_fk FOREIGN KEY (eventtmplid) REFERENCES eventtmpl (eventtmplid) ON DELETE CASCADE,
          CONSTRAINT eventtmplbadge_badgeid_fk FOREIGN KEY (badgeid) REFERENCES badge (badgeid) ON DELETE RESTRICT,
          CONSTRAINT eventtmplbadge_roleid_fk FOREIGN KEY (roleid) REFERENCES role (roleid) ON DELETE RESTRICT
        )"""
      )

      jdbco.execute(
        """CREATE TABLE boss (
          bossid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          instanceid BIGINT NOT NULL,
          name VARCHAR(64) NOT NULL,
          CONSTRAINT boss_instanceid_name_unique UNIQUE (instanceid, name),
          CONSTRAINT boss_instanceid_fk FOREIGN KEY (instanceid) REFERENCES instance (instanceid) ON DELETE CASCADE
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventtmplboss (
          eventtmplbossid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventtmplid BIGINT NOT NULL,
          bossid BIGINT NOT NULL,
          CONSTRAINT eventtmplboss_eventtmplid_bossid_unique UNIQUE (eventtmplid, bossid),
          CONSTRAINT eventtmplboss_eventtmplid_fk FOREIGN KEY (eventtmplid) REFERENCES eventtmpl (eventtmplid) ON DELETE CASCADE
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventsched (
          eventschedid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventtmplid BIGINT NOT NULL,
          active CHAR(1) NOT NULL CONSTRAINT eventsched_active_bool CHECK (active in ('Y', 'N')),
          start_time TIMESTAMP NOT NULL,
          orig_start_time TIMESTAMP NOT NULL,
          timezone_offset INTEGER NOT NULL,
          duration INTEGER NOT NULL,
          display_start INTEGER NOT NULL,
          display_end INTEGER NOT NULL,
          signups_start INTEGER NOT NULL,
          signups_end INTEGER NOT NULL,
          repeat_size INTEGER NOT NULL DEFAULT 0,
          repeat_freq INTEGER NOT NULL DEFAULT 0,
          day_mask INTEGER NOT NULL DEFAULT 0,
          repeat_by INTEGER NOT NULL DEFAULT 0,
          CONSTRAINT eventsched_eventtmplid_fk FOREIGN KEY (eventtmplid) REFERENCES eventtmpl (eventtmplid) ON DELETE CASCADE
        )"""
      )

      jdbco.execute(
        """CREATE TABLE event (
          eventid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventtmplid BIGINT,
          name VARCHAR(128) NOT NULL,
          size INTEGER NOT NULL,
          minimum_level INTEGER NOT NULL,
          instanceid BIGINT NOT NULL,
          start_time TIMESTAMP NOT NULL,
          duration INTEGER NOT NULL,
          display_start TIMESTAMP NOT NULL,
          display_end TIMESTAMP NOT NULL,
          signups_start TIMESTAMP NOT NULL,
          signups_end TIMESTAMP NOT NULL,
          last_signup_modification TIMESTAMP NOT NULL DEFAULT current_timestamp,
          CONSTRAINT event_eventmplid_fk FOREIGN KEY (eventtmplid) REFERENCES eventtmpl (eventtmplid) ON DELETE SET NULL,
          CONSTRAINT event_instanceid_fk FOREIGN KEY (instanceid) REFERENCES instance (instanceid) ON DELETE RESTRICT
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventsignup (
          eventsignupid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventid BIGINT NOT NULL,
          chrid BIGINT NOT NULL,
          roleid BIGINT NOT NULL,
          signup_type INTEGER NOT NULL,
          signup_time TIMESTAMP NOT NULL,
          note VARCHAR(2048),
          CONSTRAINT eventsignup_eventid_chrid_unique UNIQUE (eventid, chrid),
          CONSTRAINT eventsignup_eventid_fk FOREIGN KEY (eventid) REFERENCES event (eventid) ON DELETE CASCADE,
          CONSTRAINT eventsignup_chrid_fk FOREIGN KEY (chrid) REFERENCES chr (chrid) ON DELETE CASCADE,
          CONSTRAINT eventsignup_roleid_fk FOREIGN KEY (roleid) REFERENCES role (roleid) ON DELETE RESTRICT
        )"""
      )

      // create triggers so that any modification to eventsignup will update
      // the last_signup_modification column in the related event
      jdbco.execute(
        """CREATE TRIGGER maint_last_signup_mod_insert
            AFTER INSERT ON eventsignup
            REFERENCING NEW AS NEW
            FOR EACH ROW
            UPDATE event SET last_signup_modification = current_timestamp
              WHERE eventid = NEW.eventid"""
      )

      jdbco.execute(
        """CREATE TRIGGER maint_last_signup_mod_update
            AFTER UPDATE ON eventsignup
            REFERENCING NEW AS NEW
            FOR EACH ROW
            UPDATE event SET last_signup_modification = current_timestamp
              WHERE eventid = NEW.eventid"""
      )

      jdbco.execute(
        """CREATE TRIGGER maint_last_signup_mod_delete
            AFTER DELETE ON eventsignup
            REFERENCING OLD AS OLD
            FOR EACH ROW
            UPDATE event SET last_signup_modification = current_timestamp
              WHERE eventid = OLD.eventid"""
      )

      jdbco.execute(
        """CREATE TABLE eventrole (
          eventroleid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventid BIGINT NOT NULL,
          roleid BIGINT NOT NULL,
          min_count INTEGER NOT NULL CONSTRAINT eventrole_min_count_ge_zero CHECK (min_count >= 0),
          max_count INTEGER NOT NULL CONSTRAINT eventrole_max_count_gt_zero CHECK (max_count > 0),
          CONSTRAINT eventrole_eventid_fk FOREIGN KEY (eventid) REFERENCES event (eventid) ON DELETE CASCADE,
          CONSTRAINT eventrole_roleid_fk FOREIGN KEY (roleid) REFERENCES role (roleid) ON DELETE RESTRICT
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventbadge (
          eventbadgeid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventid BIGINT NOT NULL,
          badgeid BIGINT NOT NULL,
          require_for_signup CHAR(1) NOT NULL CONSTRAINT eventbadge_require_for_signup_bool CHECK (require_for_signup in ('Y','N')),
          roleid BIGINT,
          num_slots INTEGER NOT NULL,
          early_signup INTEGER NOT NULL,
          CONSTRAINT eventbadge_eventid_fk FOREIGN KEY (eventid) REFERENCES event (eventid) ON DELETE CASCADE,
          CONSTRAINT eventbadge_badgeid_fk FOREIGN KEY (badgeid) REFERENCES badge (badgeid) ON DELETE RESTRICT,
          CONSTRAINT eventbadge_roleid_fk FOREIGN KEY (roleid) REFERENCES role (roleid) ON DELETE RESTRICT
        )"""
      )

      jdbco.execute(
        """CREATE TABLE eventboss (
          eventbossid BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL,
          eventid BIGINT NOT NULL,
          bossid BIGINT NOT NULL,
          CONSTRAINT eventboss_eventid_bossid_unique UNIQUE (eventid, bossid),
          CONSTRAINT eventboss_eventid_fk FOREIGN KEY (eventid) REFERENCES event (eventid) ON DELETE CASCADE
        )"""
      )
    }

    for (version <- new_schema_vers) {
      jdbc.update(
        """UPDATE schema_vers SET version = ? WHERE schema_name = ?""",
        Array[AnyRef](version, schema_name): _*
      )
    }
  }

  def validateUser(username: String, password: String) = {
    val jdbc = getSimpleJdbcTemplate()
    val acct = jdbc.query(
      "select accountid, password from account where username = ?",
      new ParameterizedRowMapper[(Long, String)] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          (rs.getLong(1), rs.getString(2))
        }
      },
      Array[AnyRef](username): _*
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
                        values (?,        ?,     ?,        ?,     CURRENT_TIMESTAMP)""",
        Array[AnyRef](username, email, crypt, "N"): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("User '" + username + "' already exists.")

      case e =>
        logger.debug("Error creating user", e)
        throw new RuntimeException("Error creating user: " + e.getMessage)
    }

    jdbc.queryForLong(
      "select accountid from account where username = ?",
      Array[AnyRef](username): _*
    )
  }

  private def getRaceId(race: String): Long = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.queryForLong(
        "select raceid from race where name = ?",
        Array[AnyRef](race): _*
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        jdbc.update(
          "insert into race (name) values (?)",
          Array[AnyRef](race): _*
        )
        getRaceId(race)
    }
  }

  private def getClassId(clazz: String): Long = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.queryForLong(
        "select classid from class where name = ?",
        Array[AnyRef](clazz): _*
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        jdbc.update(
          "insert into class (name) values (?)",
          Array[AnyRef](clazz): _*
        )
        getRaceId(clazz)
    }
  }

  def createCharacter(uid: Long, realm: String, character: String) = {
    val jdbc = getSimpleJdbcTemplate()

    try {
      val cid = jdbc.queryForLong(
        """select chrid from chr where realm = ? and name = ?""",
        Array[AnyRef](realm, character): _*
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
    val level = (char \ "@level").toString.toInt

    val raceid = getRaceId(race.toString)
    val classid = getClassId(clazz.toString)

    try {
      jdbc.update(
        """insert into chr (accountid, realm, name, raceid, classid, level, chrxml, created)
                    values (?,         ?,     ?,    ?,      ?,       ?,     ?,      CURRENT_TIMESTAMP)""",
        Array[AnyRef](uid, realm, character, raceid, classid, level, charxml.toString): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Character '" + character + "' already exists.")
    }

    jdbc.queryForLong(
      """select chrid from chr where realm = ? and name = ?""",
      Array[AnyRef](realm, character): _*
    )
  }

  def getCharacterRoles(cid: Long): Seq[JSChrRole] = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.query(
      "select " + JSChrRoleMapper.columns + " from role join chrrole on role.roleid = chrrole.roleid where chrid = ? order by name",
      JSChrRoleMapper,
      Array[AnyRef](cid): _*
    )
  }

  def getCharacterBadges(cid: Long): Seq[JSChrBadge] = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.query(
      "select " + JSChrBadgeMapper.columns + " from badge join chrbadge on badge.badgeid = chrbadge.badgeid where chrid = ? order by name",
      JSChrBadgeMapper,
      Array[AnyRef](cid): _*
    )
  }

  def getCharacters(uid: Long) = {
    val jdbc = getSimpleJdbcTemplate()
    val r = jdbc.query(
      "select " + JSCharacterMapper.columns + " from "
        + JSCharacterMapper.tables
        + """ where accountid = ?""",
      JSCharacterMapper,
      Array[AnyRef](uid): _*
    )
    for (chr <- r) {
      chr.roles = getCharacterRoles(chr.cid).toArray
      chr.badges = getCharacterBadges(chr.cid).toArray
    }
    r
  }

  def getCharacter(cid: Long) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      val chr = jdbc.queryForObject(
        "select " + JSCharacterMapper.columns + " from "
          + JSCharacterMapper.tables
          + """ where chrid = ?""",
        JSCharacterMapper,
        Array[AnyRef](cid): _*
      )

      chr.roles = getCharacterRoles(chr.cid).toArray
      chr.badges = getCharacterBadges(chr.cid).toArray

      chr
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Character not found.")
    }
  }

  def getRoles = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSRoleMapper.columns + " from role order by name",
      JSRoleMapper,
      Array[AnyRef](): _*
    )
  }

  def addRole(name: String, restricted: Boolean) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into role (name, restricted)
                     values (?,    ?         )""",
        Array[AnyRef](name, boolchar(restricted)): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Role '" + name + "' already exists.")
    }

    jdbc.queryForLong(
      "select roleid from role where name = ?",
      Array[AnyRef](name): _*
    )
  }

  def updateCharacterRole(cid: Long, roleid: Long, adding: Boolean): Unit = {
    val jdbc = getSimpleJdbcTemplate()
    if (adding) {
      try {
        jdbc.update("""insert into chrrole (chrid, roleid, waiting, approved)
                        VALUES (?, ?, 'Y', 'N')""",
                    cid, roleid)
      }
      catch {
        case e: DataIntegrityViolationException if e.getMessage.contains("CHRROLE_CHRID_FK") =>
          throw new NotFoundError

        case e: DataIntegrityViolationException if e.getMessage.contains("UNIQUE") =>
          // role already there, ignore
      }
    }
    else {
      // TODO: Update last_signup_modification for any event that this
      // character is signed up for with the role being deleted.

      // TODO: Delete any signups by this character with this role.

      jdbc.update("""delete from chrrole where chrid = ? and roleid = ?""",
                  cid, roleid)
    }
  }

  def getBadges = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSBadgeMapper.columns + " from badge order by score desc, name",
      JSBadgeMapper
    )
  }

  def addBadge(name: String, score: Int) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into badge (name, score)
                      values (?,    ?    )""",
        Array[AnyRef](name, score): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Badge '" + name + "' already exists.")
    }

    jdbc.queryForLong(
      "select badgeid from badge where name = ?",
      Array[AnyRef](name): _*
    )
  }

  def updateCharacterBadge(cid: Long, badgeid: Long, adding: Boolean): Unit = {
    val jdbc = getSimpleJdbcTemplate()
    if (adding) {
      try {
        jdbc.update("""insert into chrbadge (chrid, badgeid, waiting, approved)
                        VALUES (?, ?, 'Y', 'N')""",
                    cid, badgeid)
      }
      catch {
        case e: DataIntegrityViolationException if e.getMessage.contains("CHRBADGE_CHRID_FK") =>
          throw new NotFoundError

        case e: DataIntegrityViolationException if e.getMessage.contains("UNIQUE") =>
          // badge already there, ignore
      }
    }
    else {
      // TODO: Update last_signup_modification for any event that this
      // character is signed up for that has badge requirements involving
      // the badge being deleted.

      jdbc.update("""delete from chrbadge where chrid = ? and badgeid = ?""",
                  cid, badgeid)
    }
  }

  def addInstance(name: String) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into instance (name)
                         values (?   )""",
        Array[AnyRef](name): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Instance '" + name + "' already exists.")
    }

    jdbc.queryForLong(
      "select instanceid from instance where name = ?",
      Array[AnyRef](name): _*
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
        parms: _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Boss '" + boss + "' already exists.")
    }

    jdbc.queryForLong(
      "select bossid from boss where instanceid = ? and name = ?",
      parms: _*
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
      Array[AnyRef](): _*
    )
  }

  def getInstanceId(instance: String) = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.queryForLong(
      "select instanceid from instance where name = ?",
      Array[AnyRef](instance): _*
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
        Array[AnyRef](iid): _*
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        new java.util.ArrayList[String]()
    }
  }

  private def populateEventTemplate[T <: JSEventTemplate](et: T,
                                                          eventstub: String)
    :T = {

    val jdbc = getSimpleJdbcTemplate()
    val ops = jdbc.getJdbcOperations()

    ops.query(
      """select boss.name
          from boss join """ + eventstub + """boss
            on boss.bossid = """ + eventstub + """boss.bossid
          where """ + eventstub + """id = ?""",
      Array[AnyRef](et.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          et.bosses.add(rs.getString(1))
        }
      }
    )

    ops.query(
      """select role.roleid, role.name, min_count, max_count
          from role join """ + eventstub + """role
            on role.roleid = """ + eventstub + """role.roleid
          where """ + eventstub + """id = ?""",
      Array[AnyRef](et.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          val ev = new JSEventRole

          ev.roleid = rs.getLong(1)
          ev.name = rs.getString(2)
          ev.min = rs.getInt(3)
          ev.max = rs.getInt(4)

          et.roles.add(ev)
        }
      }
    )

    ops.query(
      """select badge.badgeid, badge.name, require_for_signup, role.name, num_slots, early_signup
          from
            """ + eventstub + """badge left outer join role
              on """ + eventstub + """badge.roleid = role.roleid,
            badge
          where
            """ + eventstub + """badge.badgeid = badge.badgeid
            and """ + eventstub + """id = ?""",
      Array[AnyRef](et.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          val eb = new JSEventBadge

          eb.badgeid = rs.getLong(1)
          eb.name = rs.getString(2)
          eb.requireForSignup = charbool(rs.getString(3))
          eb.applyToRole = rs.getString(4)
          eb.numSlots = rs.getInt(5)
          eb.earlySignup = rs.getInt(6)

          et.badges.add(eb)
        }
      }
    )

    et
  }

  private def populateEventTemplate(et: JSEventTemplate): JSEventTemplate =
    populateEventTemplate(et, "eventtmpl")

  private def populateEvent(e: JSEvent): JSEvent =
    populateEventTemplate(e, "event")

  def getEventTemplate(name: String) = {
    val jdbc = getSimpleJdbcTemplate()

    val jset = try {
      jdbc.queryForObject(
        "select " + JSEventTemplateMapper.columns
                  + " from "
                  + JSEventTemplateMapper.tables
                  + " where eventtmpl.name = ?",
        JSEventTemplateMapper,
        Array[AnyRef](name): _*
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Template '" + name + "' not found.")
    }

    populateEventTemplate(jset)
  }

  def getEventTemplates = {
    val jdbc = getSimpleJdbcTemplate()
    val jsets = jdbc.query(
      "select " + JSEventTemplateMapper.columns
                + " from "
                + JSEventTemplateMapper.tables
                + " order by eventtmpl.name",
      JSEventTemplateMapper,
      Array[AnyRef](): _*
    ).map(populateEventTemplate)

    val r = new scala.collection.jcl.ArrayList[JSEventTemplate]
    r.addAll(jsets)
    r
  }

  private def rebuildEvents(et: JSEventTemplate): Unit = {
    val jdbc = getSimpleJdbcTemplate

    val iid = getInstanceId(et.instance)

    for {
      leventid <- jdbc.query("""select eventid from event
                                  where event.eventtmplid = ? for update""",
                             new ParameterizedRowMapper[java.lang.Long] {
                               def mapRow(rs: ResultSet, rowNum: Int) = {
                                 rs.getLong(1)
                               }
                             },
                             et.eid)
      eventid = leventid.longValue
    } {
      // update the event row itself
      jdbc.update("""update event set
        name = ?,
        size = ?,
        minimum_level = ?,
        instanceid = ?
        where eventid = ?""",
        et.name, et.size, et.minimumLevel, iid, eventid)

      // delete and recreate the bosses, roles and badges
      jdbc.update("delete from eventrole where eventid = ?", eventid)
      populateEventRoles(eventid, et.eid)

      jdbc.update("delete from eventbadge where eventid = ?", eventid)
      populateEventBadges(eventid, et.eid)

      jdbc.update("delete from eventboss where eventid = ?", eventid)
      populateEventBosses(eventid, et.eid)
    }
  }

  def saveEventTemplate(et: JSEventTemplate) = {
    val jdbc = getSimpleJdbcTemplate()

    val iid = getInstanceId(et.instance)

    val new_event = et.eid == -1

    if (new_event) {
      try {
        jdbc.update(
          """insert into eventtmpl (name, size, minimum_level, instanceid)
                            values (?,    ?,    ?,             ?         )""",
          Array[AnyRef](et.name, et.size, et.minimumLevel, iid): _*
        )

        et.eid = jdbc.queryForLong(
          "select eventtmplid from eventtmpl where name = ?",
          Array[AnyRef](et.name): _*
        )
      }
      catch {
        case _: DataIntegrityViolationException =>
          throw new AlreadyExistsError("Template '" + et.name + "' already exists.")
      }
    }
    else {
      try {
        val c = jdbc.update(
          """update eventtmpl set name = ?, size = ?, minimum_level = ?, instanceid = ? where eventtmplid = ?""",
          Array[AnyRef](et.name, et.size, et.minimumLevel, iid, et.eid): _*
        )

        if (c == 0) {
          throw new NotFoundError("Template " + et.eid + " not found.")
        }
      }
      catch {
        case _: DataIntegrityViolationException =>
          throw new AlreadyExistsError("Template '" + et.name + "' already exists.")
      }
    }

    jdbc.update(
      """delete from eventtmplboss where eventtmplid = ?""",
      Array[AnyRef](et.eid): _*
    )

    for (boss <- et.bosses) {
      val bossid = try {
        jdbc.queryForLong(
          "select bossid from boss where instanceid = ? and name = ?",
          Array[AnyRef](iid, boss): _*
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Boss '" + boss + "' not found.")
      }

      jdbc.update(
        """insert into eventtmplboss (eventtmplid, bossid)
                              VALUES (?,           ?     )""",
        Array[AnyRef](et.eid, bossid): _*
      )
    }

    jdbc.update(
      """delete from eventtmplrole where eventtmplid = ?""",
      Array[AnyRef](et.eid): _*
    )

    for (role <- et.roles) {
      val roleid = try {
        jdbc.queryForLong(
          "select roleid from role where name = ?",
          Array[AnyRef](role.name): _*
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Role '" + role.name + "' not found.")
      }

      try {
        jdbc.update(
          """insert into eventtmplrole (eventtmplid, roleid, min_count, max_count)
                                VALUES (?,           ?,      ?,         ?        )""",
          Array[AnyRef](et.eid, roleid, role.min, role.max): _*
        )
      }
      catch {
        case e: DataIntegrityViolationException
          if e.getMessage.contains("MIN_COUNT_GE_ZERO") =>
            throw new IllegalArgumentException(
                "Role '"
              + role.name
              + "' must have a non-negative min value.")

        case e: DataIntegrityViolationException
          if e.getMessage.contains("MAX_COUNT_GT_ZERO") =>
            throw new IllegalArgumentException(
                "Role '"
              + role.name
              + "' must have a max value greater than zero.")
      }
    }

    jdbc.update(
      """delete from eventtmplbadge where eventtmplid = ?""",
      Array[AnyRef](et.eid): _*
    )

    // XXX: JCL wrapper creating weird compiler errors for et.badges
    val iter = et.badges.iterator
    while (iter.hasNext) {
      val badge = iter.next

      val badgeid = try {
        jdbc.queryForLong(
          "select badgeid from badge where name = ?",
          Array[AnyRef](badge.name): _*
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Badge '" + badge.name + "' not found.")
      }

      val roleid: Option[java.lang.Long] = if (badge.applyToRole ne null) {
        try {
          Some(jdbc.queryForLong(
            "select roleid from role where name = ?",
            Array[AnyRef](badge.applyToRole): _*
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
        Array[AnyRef](et.eid, badgeid, boolchar(badge.requireForSignup), roleid.getOrElse(null), badge.numSlots, badge.earlySignup): _*
      )

      ()
    }

    if (!new_event && et.modifyEvents) {
      rebuildEvents(et);
    }

    et.eid
  }

  def getActiveEventSchedules = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSEventScheduleMapper.columns +
      """ from eventsched join eventtmpl
            on eventsched.eventtmplid = eventtmpl.eventtmplid
          where active = 'Y'""",
      JSEventScheduleMapper,
      noargs: _*
    )
  }

  def getEventSchedules(name: String) = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSEventScheduleMapper.columns +
      """ from eventsched join eventtmpl
            on eventsched.eventtmplid = eventtmpl.eventtmplid
          where eventtmpl.name = ?
          order by start_time""",
      JSEventScheduleMapper,
      Array[AnyRef](name): _*
    )
  }

  def saveEventSchedule(es: JSEventSchedule) = {
    val jdbc = getSimpleJdbcTemplate

    if (es.esid == -1) {
      // new schedule
      jdbc.update(
        """insert into eventsched
            (eventtmplid, active,
             start_time, orig_start_time,
             timezone_offset, duration,
             display_start, display_end,
             signups_start, signups_end,
             repeat_size, repeat_freq,
             day_mask, repeat_by)
            VALUES
            (?, ?,
             ?, ?,
             ?, ?,
             ?, ?,
             ?, ?,
             ?, ?,
             ?, ?)""",
          Array[AnyRef](es.eid, boolchar(es.active),
                        es.start_time, es.orig_start_time,
                        es.timezone_offset, es.duration,
                        es.display_start, es.display_end,
                        es.signups_start, es.signups_end,
                        es.repeat_size, es.repeat_freq,
                        es.day_mask, es.repeat_by): _*)

      jdbc.queryForLong("values IDENTITY_VAL_LOCAL()", noargs: _*)
    }
    else {
      // update existing
      val r = jdbc.update(
        """update eventsched
            set
              eventtmplid = ?, active = ?,
              start_time = ?, orig_start_time = ?,
              timezone_offset = ?, duration = ?,
              display_start = ?, display_end = ?,
              signups_start = ?, signups_end = ?,
              repeat_size = ?, repeat_freq = ?, day_mask = ?,
              repeat_by = ?
            where
              eventschedid = ?""",
          Array[AnyRef](es.eid, boolchar(es.active),
                        es.start_time, es.orig_start_time,
                        es.timezone_offset, es.duration,
                        es.display_start, es.display_end,
                        es.signups_start, es.signups_end,
                        es.repeat_size, es.repeat_freq, es.day_mask,
                        es.repeat_by, es.esid): _*)

      if (r == 0) {
        throw new NotFoundError(  "Event schedule not found, "
                                + "did somebody else delete it?")
      }

      es.esid
    }
  }

  private def populateEventRoles(eventid: Long, eventtmplid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    jdbc.update(
      """insert into eventrole (eventid, roleid, min_count, max_count)
          select ?, roleid, min_count, max_count
            from eventtmplrole
            where eventtmplid = ?""",
      Array[AnyRef](eventid, eventtmplid): _*)
  }

  private def populateEventBadges(eventid: Long, eventtmplid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    jdbc.update(
      """insert into eventbadge (eventid, badgeid, require_for_signup, roleid,
          num_slots, early_signup)
          select ?, badgeid, require_for_signup, roleid, num_slots,
            early_signup
            from eventtmplbadge
            where eventtmplid = ?""",
      Array[AnyRef](eventid, eventtmplid): _*)
  }

  private def populateEventBosses(eventid: Long, eventtmplid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    jdbc.update(
      """insert into eventboss (eventid, bossid)
          select ?, bossid
            from eventtmplboss
            where eventtmplid = ?""",
      Array[AnyRef](eventid, eventtmplid): _*)
  }

  def publishEvent(es: JSEventSchedule) = {
    val jdbc = getSimpleJdbcTemplate

    // create event

    //   calculate display/signup dates
    val display_start = new Date(es.start_time.getTime - es.display_start * 1000)
    val display_end = new Date(es.start_time.getTime + es.display_end * 1000)
    val signups_start = new Date(es.start_time.getTime - es.signups_start * 1000)
    val signups_end = new Date(es.start_time.getTime + es.signups_end * 1000)

    val jset = jdbc.queryForObject(
      "select " + JSEventTemplateMapper.columns
                + " from "
                + JSEventTemplateMapper.tables
                + " where eventtmpl.eventtmplid = ?",
      JSEventTemplateMapper,
      Array[AnyRef](es.eid): _*
    )

    val iid = getInstanceId(jset.instance)

    jdbc.update(
      """insert into event (eventtmplid, name, size, minimum_level, instanceid,
          start_time, duration, display_start, display_end, signups_start,
          signups_end)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      Array[AnyRef](es.eid, jset.name, jset.size, jset.minimumLevel, iid,
        es.start_time, es.duration, display_start, display_end,
        signups_start, signups_end
      ): _*)

    val eventid = jdbc.queryForLong("values IDENTITY_VAL_LOCAL()", noargs: _*)

    populateEventRoles(eventid, es.eid)
    populateEventBadges(eventid, es.eid)
    populateEventBosses(eventid, es.eid)
  }

  def getEvents = {
    val jdbc = getSimpleJdbcTemplate

    val events = jdbc.query(
      "select " + JSEventMapper.columns
                + " from "
                + JSEventMapper.tables
                + """ where event.display_start <= current_timestamp
                        and event.display_end >= current_timestamp
                      order by event.start_time""",
      JSEventMapper,
      noargs: _*
    )

    for (event <- events) {
      populateEvent(event)
    }

    events
  }

  def getEventSignups(eventid: Long,
                      if_changed_since: Date): Option[JSEventSignups] = {
    val jdbc = getSimpleJdbcTemplate
    val last_modification = try {
      jdbc.queryForObject(
        "select last_signup_modification from event where eventid = ?",
        classOf[java.sql.Timestamp],
        eventid
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("No such event")
    }

    if (last_modification.getTime > if_changed_since.getTime) {
      val r = new JSEventSignups
      r.eventid = eventid
      r.signups = jdbc.query(
        "select eventsignupid, eventid, " + JSCharacterMapper.columns + ", "
          + JSRoleMapper.columns + """, signup_type, signup_time, note
          from
            eventsignup
            join chr on eventsignup.chrid = chr.chrid
            join race on chr.raceid = race.raceid
            join class on chr.classid = class.classid
            join role on eventsignup.roleid = role.roleid
          where eventid = ?
          order by signup_time""",
        new ParameterizedRowMapper[JSEventSignup] {
          override def mapRow(rs: ResultSet, rowNum: Int) = {
            val r = new JSEventSignup
            var col = 1
            r.eventsignupid = rs.getLong(col)
            col += 1
            r.eventid = rs.getLong(col)
            col += 1
            r.chr = JSCharacterMapper.mapRow(rs, rowNum, col)
            col += JSCharacterMapper.ncolumns
            r.role = JSRoleMapper.mapRow(rs, rowNum, col)
            col += JSRoleMapper.ncolumns
            r.signup_type = rs.getInt(col)
            col += 1
            r.signup_time = rs.getTimestamp(col)
            col += 1
            r.note = rs.getString(col)
            r
          }
        },
        eventid
      )

      for (es <- r.signups) {
        es.chr.roles = getCharacterRoles(es.chr.cid).toArray
        es.chr.badges = getCharacterBadges(es.chr.cid).toArray
      }

      r.asof = last_modification

      Some(r)
    }
    else {
      None
    }
  }
}
