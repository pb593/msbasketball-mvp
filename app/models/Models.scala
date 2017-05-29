package models

import org.joda.time.LocalDate

/**
  * Created by pb593 on 27/05/2017.
  */
case class User(userId: String, name: String, isPermanentMember: Boolean)
case class Session(SessionId: Int, date: LocalDate)