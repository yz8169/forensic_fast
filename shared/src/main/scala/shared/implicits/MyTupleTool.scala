package shared.implicits

import scala.collection.SeqMap

/**
 * Created by Administrator on 2019/11/11
 */

trait MyTupleTool {

  implicit class MyTuple[A, B](t: List[(A, B)]) {

    def toSeqMap = {
      val tmpAcc = SeqMap[A, B]()
      t.foldLeft(tmpAcc) { (inMap, t) =>
        val key = t._1
        val value = t._2
        if (inMap.contains(key)) {
          val values = inMap(key)
          inMap.updated(key, value)
        } else {
          inMap.updated(key, value)
        }
      }
    }

    def groupSeqMap = {
      val tmpAcc = SeqMap[A, List[B]]()
      t.foldLeft(tmpAcc) { (inMap, t) =>
        val key = t._1
        val value = t._2
        if (inMap.contains(key)) {
          val values = inMap(key)
          inMap.updated(key, values ::: List(value))
        } else {
          inMap.updated(key, List(value))
        }
      }
    }


  }


}
