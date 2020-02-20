package test

import shapeless._
import shapeless.ops.hlist
import shapeless.labelled._
import cats.Monoid
import cats.instances.all._
import shapeless.record._
import shapeless.syntax.RecordOps

/**
 * Created by Administrator on 2019/12/6
 */
object Learning {

  def main(args: Array[String]): Unit = {

    def failingFn(i: Int): Int = {
      //      val y: Int = throw new Exception("fail!")
      try {
        val x = 42 + 5
        x + ((throw new Exception("fail!")): Int)
      }
      catch {
        case e: Exception => 43
      }
    }

    def mean_1(xs: IndexedSeq[Double], onEmpty: Double): Double =
      if (xs.isEmpty) onEmpty
      else xs.sum / xs.length

    def mean(xs: IndexedSeq[Double]): Either[String, Double] =
      if (xs.isEmpty)
        Left("mean of empty list!")
      else
        Right(xs.sum / xs.length)

    case class IceCreamV1(name: String, numCherries: Int, inCone: Boolean)

    case class IceCreamV2a(name: String, inCone: Boolean)

    case class IceCreamV2b(name: String, inCone: Boolean, numCherries: Int)

    case class IceCreamV2c(name: String, inCone: Boolean, numCherries: Int, numWaffles: Int)

    trait Migration[A, B] {
      def apply(a: A): B
    }

    implicit class MigrationOps[A](a: A) {
      def migrateTo[B](implicit migration: Migration[A, B]): B =
        migration.apply(a)
    }

    def createMonoid[A](zero: A)(add: (A, A) => A): Monoid[A] =
      new Monoid[A] {
        def empty = zero

        def combine(x: A, y: A): A = add(x, y)
      }

    implicit val hnilMonoid: Monoid[HNil] =
      createMonoid[HNil](HNil)((x, y) => HNil)

    implicit def emptyHList[K <: Symbol, H, T <: HList](
                                                         implicit
                                                         hMonoid: Lazy[Monoid[H]],
                                                         tMonoid: Monoid[T]
                                                       ): Monoid[FieldType[K, H] :: T] =
      createMonoid(field[K](hMonoid.value.empty) :: tMonoid.empty) {
        (x, y) =>
          field[K](hMonoid.value.combine(x.head, y.head)) ::
            tMonoid.combine(x.tail, y.tail)
      }

    implicit def genericMigration[
      A, B, ARepr <: HList, BRepr <: HList,
      Common <: HList, Added <: HList, Unaligned <: HList
    ](
       implicit
       aGen: LabelledGeneric.Aux[A, ARepr],
       bGen: LabelledGeneric.Aux[B, BRepr],
       inter: hlist.Intersection.Aux[ARepr, BRepr, Common],
       diff: hlist.Diff.Aux[BRepr, Common, Added],
       monoid: Monoid[Added],
       prepend: hlist.Prepend.Aux[Added, Common, Unaligned],
       align: hlist.Align[Unaligned, BRepr]
     ): Migration[A, B] =
      new Migration[A, B] {
        def apply(a: A): B =
          bGen.from(align(prepend(monoid.empty, inter(aGen.to(a)))))
      }

    case class IceCream(name: String, numCherries: Int, inCone: Boolean)

    val sundae = LabelledGeneric[IceCream].
      to(IceCream("Sundae", 1, false))

    trait Case[P, A] {
      type Result

      def apply(a: A): Result
    }

    trait Poly {
      def apply[A](arg: A)(implicit cse: Case[this.type, A]): cse.Result =
        cse.apply(arg)
    }

    object myPoly extends Poly {
      implicit def intCase =
        new Case[this.type, Int] {
          type Result = Double
          def apply(num: Int): Double = num / 2.0
        }
      implicit def stringCase =
        new Case[this.type, String] {
          type Result = Int
          def apply(str: String): Int = str.length
        }
    }

    val rs = myPoly.apply(123)

    println(rs)


  }

}
