import scala.collection.mutable

/**
 * Created by avitaln
 * 8/26/14
 */

trait CatalogEvent {
  def sid: String
  def ver: Long
  def withVer(ver: Long) : CatalogEvent
}
trait ProductEvent extends CatalogEvent {
  def pid: String
}
case class CloneStore(sid: String, from_sid: String, from_sver: Long, ver:Long = 0) extends CatalogEvent { // old ver
def withVer(ver: Long) = copy(ver = ver)
}
case class CreateProduct(sid: String, pid: String, ver:Long = 0) extends ProductEvent {
  def withVer(ver: Long) = copy(ver = ver)
}
case class SetPrice(sid: String, pid: String, price: Long, ver:Long = 0) extends ProductEvent {
  def withVer(ver: Long) = copy(ver = ver)
}
case class SetTitle(sid: String, pid: String, title: String, ver:Long = 0) extends ProductEvent {
  def withVer(ver: Long) = copy(ver = ver)
}

trait EventsRepository {
  def insertEvent(e: CatalogEvent) : Unit
  def storeEvents(sid: String, fromVer: Long, toVer:Long) : Seq[CatalogEvent]
  def productEvents(sid: String, pid: String, fromVer: Long, toVer:Long) : Seq[CatalogEvent]
}

class InMemoryEventsRepository extends EventsRepository {
  val events = new mutable.MutableList[CatalogEvent]

  val eventsFilter = (sid:String,minVer:Long,maxVer:Long,pid:String) =>
    (e:CatalogEvent) => isProductEvent(e,sid,minVer,maxVer,pid) || isCloneStore(e, sid)

  def storeEvents(sid: String, fromVer: Long, toVer:Long) : Seq[CatalogEvent] =
    events.filter(eventsFilter(sid,fromVer,toVer,"")).toSeq

  def productEvents(sid: String, pid: String, fromVer: Long, toVer:Long) : Seq[CatalogEvent] = {
    events.filter(eventsFilter(sid,fromVer,toVer,pid)).toSeq
  }

  private def isProductEvent(e: CatalogEvent, sid: String, minVer: Long, maxVer: Long, pid: String = "") : Boolean =
    e.isInstanceOf[ProductEvent] &&
      (pid == "" || e.asInstanceOf[ProductEvent].pid == pid) &&
      e.sid == sid &&
      e.ver <= maxVer &&
      e.ver >= minVer

  private def isCloneStore(e: CatalogEvent, sid: String) : Boolean =
    e.isInstanceOf[CloneStore] && e.sid == sid

  def insertEvent(e: CatalogEvent) : Unit = events += e


}
