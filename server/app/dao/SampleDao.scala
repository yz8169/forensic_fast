package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SampleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  import com.github.tototoshi.slick.MySQLJodaSupport._

  def insertOrUpdate(row: SampleRow) = db.run(Sample.insertOrUpdate(row)).map(_ => ())

  def insertAll(rows: List[SampleRow]) = db.run(Sample ++= rows).map(_ => ())

  def deleteAll = db.run(Sample.delete).map(_ => ())

  def selectOpById(id: String, userId: Int) = db.run(Sample.
    filter(_.userId === userId).filter(_.id === id).result.head)

  def selectById(userId: Int, id: String) = db.run(Sample.
    filter(_.userId === userId).filter(_.id === id).result.head)

  def selectAll(userId: Int): Future[Seq[SampleRow]] = db.run(Sample.filter(_.userId === userId).sortBy(_.updateTime.desc).result)

  def deleteById(userId: Int, id: String) = db.run(Sample.filter(_.userId === userId).filter(_.id === id).delete).map(_ => ())


}
