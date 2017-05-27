import java.util.Date

import org.joda.time.LocalDate

/**
  * Created by pb593 on 24/05/2017.
  */
package object globals {

  val pmAdvancePeriod = 7; // days
  val nonpmAdvancePeriod = 2; // days

  // uniqueID -> Name, ,isPM
  val users: Map[String, (String, String, Boolean)] = Map(
    "pb593" -> ("Pavel Berkovich", "" ,true),
    "evgeng" -> ("Evgeniy Grigoriev", "", false)
  )

  val dates = List(new LocalDate(2017, 5, 25), new LocalDate(2017, 6, 1), new LocalDate(2017, 6, 8), new LocalDate(2017, 6, 15)) // possible dates for signup

  // date -> list(uniqueID)
  val signups = scala.collection.mutable.Map.empty[LocalDate, Set[String]]
  dates.foreach(d => signups+=(d -> Set()))

}
