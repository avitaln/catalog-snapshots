
import collection.JavaConversions.JConcurrentMapWrapper
import java.util.{ concurrent => juc }

/**
 * Scala Mutable concurrent HashMap
 * @author yoav
 * @since 8/21/11
 */

class ConcurrentHashMap[A,B] extends JConcurrentMapWrapper[A,B](new juc.ConcurrentHashMap[A,B]){

  def doPutIfAbsent(a: A, doPut: => B) {
    if (!contains(a))
      underlying.synchronized {
        if (!contains(a)) {
          put(a,doPut)
        }
      }
  }

  def getOrPut(a: A, doPut: => B): B = {
    if (!contains(a))
      underlying.synchronized {
        if (!contains(a)) {
          val b:B = doPut
          put(a,b)
          b
        }
        else
          get(a).get
      }
    else
      get(a).get
  }
}

object ConcurrentHashMap {

  def apply[A,B](): ConcurrentHashMap[A,B] = new ConcurrentHashMap[A,B]

}