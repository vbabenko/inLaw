package models

import java.util.Date

import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import org.joda.time.DateTime
import play.api.libs.json.{Reads, Writes, JsPath, Format}
import play.api.libs.functional.syntax._
import scala.util.Random
import forms.UserAccountInfo
import utils.CryptUtils


/**
 * Created by Alex on 11/23/14.
 */
case class Lawyer(_id: Option[BSONObjectID],
                  email: String,
                  password: String,
                  bearerToken: Option[BearerToken],
                  createdAt: DateTime,
                  profile: Option[Profile])

object Lawyer {

  implicit val accountWrites: Writes[Lawyer] = (
    (JsPath \ "_id").writeNullable[BSONObjectID] and
      (JsPath \ "email").write[String] and
      (JsPath \ "password").write[String] and
      (JsPath \ "token").writeNullable[BearerToken] and
      (JsPath \ "createdAt").write[DateTime] and
      (JsPath \ "profile").writeNullable[Profile]
    )(unlift(Lawyer.unapply))

  implicit val accountReads: Reads[Lawyer] = (
    (JsPath \ "_id").readNullable[BSONObjectID].map(_.getOrElse(BSONObjectID.generate)).map(Some(_)) and
    (JsPath \ "email").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "token").readNullable[BearerToken] and
    (JsPath \ "createdAt").readNullable[DateTime].map(_.getOrElse(new DateTime(0))) and
    (JsPath \ "profile").readNullable[Profile]
  )(Lawyer.apply _)

  def createAccount(accountInfo: UserAccountInfo) = {
    val account = Lawyer(
      _id = None,
      email = accountInfo.email,
      password = CryptUtils.encryptPassword(accountInfo.password),
      bearerToken = None,
      createdAt = new DateTime(),
      profile = None
    )
    account
  }

}

case class Profile(gender: Option[String],
                   firstName: Option[String],
                   lastName: Option[String],
                   middleName: Option[String],
                   birthDate: Option[Date],
                   minRate: Option[Int])

object Profile {

  implicit val profileFormat: Format[Profile] = (
    (JsPath \ "gender").formatNullable[String] and
    (JsPath \ "firstName").formatNullable[String] and
    (JsPath \ "lastName").formatNullable[String] and
    (JsPath \ "middleName").formatNullable[String] and
    (JsPath \ "birthDate").formatNullable[Date] and
    (JsPath \ "minRate").formatNullable[Int]
  )(Profile.apply, unlift(Profile.unapply))

}

case class BearerToken(bearer: String, status: String, expirationDate: Option[DateTime])

object BearerToken {

  implicit val bearerTokenFormat: Format[BearerToken] = (
      (JsPath \ "bearer").format[String] and
      (JsPath \ "status").format[String] and
      (JsPath \ "expirationDate").formatNullable[DateTime]
    )(BearerToken.apply, unlift(BearerToken.unapply))

  def generateToken() = {
    Random.alphanumeric.take(12).mkString
  }

}

object Status extends Enumeration {
  type Status = Value
  val Active, Blocked, Pending, Inactive = Value
}
