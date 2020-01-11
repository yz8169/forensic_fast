package test

/**
 * Created by Administrator on 2019/12/6
 */
object Learning {

  def main(args: Array[String]): Unit = {

    def failingFn(i: Int): Int = {
//      val y: Int = throw new Exception("fail!")
      try {
        val x = 42 + 5
        x + ((throw new Exception("fail!")):Int)
      }
      catch { case e: Exception => 43 }
    }

    def mean_1(xs: IndexedSeq[Double], onEmpty: Double): Double =
      if (xs.isEmpty) onEmpty
      else xs.sum / xs.length

    def mean(xs: IndexedSeq[Double]): Either[String, Double] =
      if (xs.isEmpty)
        Left("mean of empty list!")
      else
        Right(xs.sum / xs.length)

   println(failingFn(12))
    println(15)


  }

}
