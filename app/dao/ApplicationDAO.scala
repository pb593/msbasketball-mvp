package dao

import java.util

import models.User
import org.joda.time.LocalDate
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._

import scala.collection.mutable

/**
  * Created by pb593 on 27/05/2017.
  */
class ApplicationDAO {

  def sessions() : List[LocalDate] = {
    val conn = DB.getConnection()
    var result = mutable.MutableList.empty[LocalDate]
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT dtime FROM Sessions")
      while(rs.next) {
        result += new LocalDate(rs.getTimestamp("dtime").toString.substring(0, 10))
      }
      return result.toList
    }
    finally{
      conn.close()
    }
  }

  def singupList(date: LocalDate) : List[String] = {
    List()
  }

  def userProfile(userId: String) : Option[User] = {
    Some(User("a", "a", true))
  }

}
