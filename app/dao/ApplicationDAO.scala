package dao

import java.util

import models.{Session, User}
import org.joda.time.LocalDate
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._

import scala.collection.mutable

/**
  * Created by pb593 on 27/05/2017.
  */
class ApplicationDAO {

  def sessions() : List[Session] = {
    val conn = DB.getConnection()
    var result = mutable.MutableList.empty[Session]
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT * FROM Sessions")
      while(rs.next) {
        result += Session(rs.getInt("SessionId"), new LocalDate(rs.getTimestamp("dtime").toString.substring(0, 10)))
      }
      return result.toList
    }
    finally{
      conn.close()
    }
  }

  def signupList(date: LocalDate) : List[User] = {
    val conn = DB.getConnection()
    var result = mutable.MutableList.empty[User]
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT Users.UserId, Users.RealName, Users.isPermanentMember FROM ((Signups INNER JOIN Sessions ON Signups.SessionId = Sessions.SessionId) INNER JOIN Users ON Signups.UserId = Users.UserId) WHERE DATE(Sessions.dtime)='%s'::date".format(date.toString("yyyy-MM-dd")))
      while(rs.next) {
        result += User(rs.getString("UserId"), rs.getString("RealName"), rs.getBoolean("isPermanentMember"))
      }
      return result.toList
    }
    finally{
      conn.close()
    }
  }

  def userProfile(userId: String) : Option[User] = {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT * FROM Users WHERE UserId='%s'".format(userId))
      if(rs.next)
        return Some(User(rs.getString("UserId"), rs.getString("RealName"), rs.getBoolean("isPermanentMember")))
      else
        return None
    }
    finally{
      conn.close()
    }
  }

  def signupUser(userId: String, date: LocalDate): Boolean = {
    val ss = this.sessions()
    val sessionIds = ss.filter(s => s.date.equals(date)).map(s => s.SessionId)
    if(sessionIds.length > 0) {
      // there is a session on this date
      val id = sessionIds.head
      val conn = DB.getConnection()
      try {
        val stmt = conn.createStatement
        val status = stmt.executeUpdate("INSERT INTO Signups VALUES (%d, '%s')".format(id, userId))
        return status != 0
      }
      finally {
        conn.close()
      }
    }
    else { // no such session on this date
      return false
    }
  }

  def unsignupUser(userId: String, date: LocalDate): Unit = {
    val ss = this.sessions()
    val sessionIds = ss.filter(s => s.date.equals(date)).map(s => s.SessionId)
    if(sessionIds.length > 0) {
      // there is a session on this date
      val id = sessionIds.head
      val conn = DB.getConnection()
      try {
        val stmt = conn.createStatement
        val status = stmt.executeUpdate("DELETE FROM Signups WHERE SessionId=%d AND UserId='%s'".format(id, userId))
        return status != 0
      }
      finally {
        conn.close()
      }
    }
    else { // no such session on this date
      return false
    }
  }

}
