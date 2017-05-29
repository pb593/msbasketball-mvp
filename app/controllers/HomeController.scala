package controllers

import javax.inject._

import dao.ApplicationDAO
import models.User
import org.joda.time.{Days, LocalDate, Period}
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.cache.Cache
import play.api.Play.current
import play.api.db._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */


  val dao = new ApplicationDAO
  val idSubmitForm = Form(
      "id" -> text
  )

  def nextSessionDate: Option[LocalDate] = {
    dao.sessions().filter(s => s.date.compareTo(LocalDate.now()) > 0).map(s => s.date).headOption
  }

  val logger = Logger.logger

  def index = Action { implicit request =>
    Ok(views.html.idcheck())
  }

  def state = Action {
    nextSessionDate match {
      case Some(d) => {
        var rst = "%1$-25s | %2$-25s\n".format("Name", "Permanent Member")
        dao.signupList(d).foreach((u: User) => {
          rst += "%1$-25s | %2$-25s\n".format(u.name, if(u.isPermanentMember) "Yes" else "No")
        })
        Ok(rst)
      }
      case _ => {
        Ok("Can't find any future session dates...")
      }
    }
  }

  def idSubmit = Action { implicit request =>
    logger.debug(request.body.toString())
    val id = idSubmitForm.bindFromRequest.get
    logger.debug("User with id %s just tried to sign in".format(id))

    // attempt to retrieve user's profile
    dao.userProfile(id) match {
      case Some(u: User) => { // user found
        logger.debug("Access was granted")
        val today = new LocalDate
        logger.debug("Today's date is %s".format(today.toString))
        val isPM = u.isPermanentMember
        logger.debug("The user is %s".format(if(isPM) "PM" else "non-PM"))
        val allowedAdvancePeriod = if(isPM) globals.pmAdvancePeriod else globals.nonpmAdvancePeriod
        logger.debug("For this user, advance booking period is %d days".format(allowedAdvancePeriod))

        val futureDates = dao.sessions().filter(s => s.date.compareTo(today) > 0).map(s => s.date)
        logger.debug("Future dates of trainings known to the system: %s".format(futureDates.map(_.toString).mkString(",")))

        val possibleSignupDates = futureDates.filter(d => Days.daysBetween(today, d).getDays() <= allowedAdvancePeriod)
        logger.debug("Possible signup dates for this user are: %s".format(possibleSignupDates.toString()))
        val offerDate = if(possibleSignupDates.nonEmpty) possibleSignupDates.head else null
        logger.debug("Offer date for this user is: %s".format(offerDate))
        if(offerDate == null)
          Ok(views.html.message("No date for which you could sign up at the moment. Check back later"))
        else {
          val userSignupList = dao.signupList(offerDate).map(u => u.userId)
          if(userSignupList.contains(id)) { // client signed up
            Ok(views.html.signup(offerDate, id, true))
          }
          else { // client not signed up
            if(userSignupList.size >= 15) // date FULL
              Ok(views.html.message("The only date for which you could signup for is %s, but this session is currently FULL.".format(offerDate.toString)))
            else // not FULL
              Ok(views.html.signup(offerDate, id, false))
          }
        }

      }
      case None => { // user not found
        Ok(views.html.message("Unknown user. Did you type in your ID correctly???"))
      }
    }
  }

  case class SignupForm(date: String, userId: String, action: String)
  val signupForm = Form(mapping(
    "date" -> text,
    "userId" -> text,
    "action" -> text
  )(SignupForm.apply)(SignupForm.unapply))

  def signup = Action { implicit request =>
    val inputForm: SignupForm = signupForm.bindFromRequest.get
    val date = new LocalDate(inputForm.date)
    Ok(views.html.message("Got the signup form: %s".format(inputForm.toString)))
    // todo: add validation
    if(inputForm.action == "signup") { // user wants to signup
      dao.signupUser(inputForm.userId, date)
      Ok(views.html.message("User with id %s successfully signed up for session on %s".format(inputForm.userId, date.toString)))
    }
    else { // unsignup
      dao.unsignupUser(inputForm.userId, date)
      Ok(views.html.message("User with id %s successfully un-signed up from session on %s".format(inputForm.userId, date.toString)))
    }
  }

  def db = Action {
    Ok(dao.unsignupUser("2bj31s", new LocalDate("2017-06-01")).toString)
  }


}
