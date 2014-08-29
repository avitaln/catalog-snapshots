
/**
 * Created by avitaln
 * 8/7/14
 */

case class Product(pid:String="", title: String="", price: Long=0) // add store version

case class StoreView(sid: String, sver: Long, products: Map[String,Product] = Map.empty) {
  def productIds = products.keySet

  def getProduct(pid: String) = products(pid)

  def withPrice(pid: String, price: Long) : StoreView = {
    if (products.contains(pid))
      copy(products = products.updated(pid, products(pid).copy(price = price)))
    else
      this
  }

  def withTitle(pid: String, title: String) : StoreView = {
    if (products.contains(pid))
      copy(products = products.updated(pid, products(pid).copy(title = title)))
    else
      this
  }
}

///////////////////////////////////////////////////////////////////////

class MyCatalog(snapshotsRepo: SnapshotsRepository, versionsRepo: VersionsRepository, eventsRepo: EventsRepository) {

  //  val events = new mutable.MutableList[CatalogEvent]

  def addEvent(e: CatalogEvent) = {
    val ver = versionsRepo.nextVersion(e.sid)
    val eWithVer = e.withVer(ver)
    //    println(eWithVer)
    eventsRepo.insertEvent(eWithVer)
  }

  ////////////// product view /////////////////////

  def getProduct(sid: String, pid: String, full: Boolean= true) : Option[Product] = {
    val sver = versionsRepo.getVersion(sid)
    val initialProduct = Product()
    Some(internalGetProduct(initialProduct, sid, sver, pid, full))
  }

  def internalGetProduct(initialProduct: Product, sid: String, sver: Long, pid: String, full: Boolean) : Product = {
    val productAndStoreSnapshot = for {
      storeSnapshot <- snapshotsRepo.getLatestStoreSnapshot(sid, sver)
      md5 <- storeSnapshot.md5(pid)
      product <- snapshotsRepo.productSnapshot(md5)
    } yield (product,storeSnapshot)

    if (full) {
      val product = productAndStoreSnapshot match {
        // product not found - try to play it
        case None => playProduct(initialProduct, sid, pid, 0, sver) // only if fully consistent, should snapshot
        // product found and up to date
        case Some((p, storeSnap)) if storeSnap.sver >= sver => p
        // product is not up to date. play only if fully consistent. should snapshot if verson gap is larger than...
        case Some((p, storeSnap)) if storeSnap.sver < sver => playProduct(p, sid, pid, storeSnap.sver + 1, sver)
      }

      product
    } else {
      productAndStoreSnapshot.map(_._1).getOrElse(Product())
    }
  }

  private def playProduct(initialProduct: Product, sid: String, pid: String, fromVer: Long, toVer: Long) : Product = {
    val filteredEvents = eventsRepo.productEvents(sid, pid, fromVer, toVer)
    filteredEvents.foldLeft(initialProduct)(applyGetProductEvent(pid))
  }

  def applyGetProductEvent(pid: String)(p: Product, e: CatalogEvent) : Product = {
    e match {
      case CloneStore(_,from_sid,from_sver,_) => internalGetProduct(p, from_sid, from_sver, pid, full=true)
      case CreateProduct(_,_pid,_) if _pid == pid  => p.copy(pid = pid)
      case SetPrice(_,_,price,_) => p.copy(price = price)
      case SetTitle(_,_,title,_) => p.copy(title = title)
    }
  }

  ////////////// store view /////////////////////

  def getStoreView(sid: String) : StoreView = {
    val sver = versionsRepo.getVersion(sid)
    internalGetStoreView(sid, sver)
  }

  def internalGetStoreView(sid: String, maxVer: Long) : StoreView = {
    val latestStoreSnapshot = snapshotsRepo.getLatestFullStoreSnapshot(sid, maxVer)

    val (storeToStartFrom, md5s, eventsToPlay) = latestStoreSnapshot match {
      case Some((StoreView(_sid, _sver, _),_)) if _sver >= maxVer =>
        (latestStoreSnapshot.get._1,latestStoreSnapshot.get._2,Nil)
      case Some((StoreView(_sid, _sver, _),_)) if _sver < maxVer =>
        (latestStoreSnapshot.get._1,latestStoreSnapshot.get._2, eventsRepo.storeEvents(sid,_sver+1,maxVer))
      case None =>
        (StoreView(sid,maxVer),Set[String](),eventsRepo.storeEvents(sid,0,maxVer))
    }

    val store = eventsToPlay.foldLeft(storeToStartFrom)(applyStoreEvent)

    if (eventsToPlay.nonEmpty) snapshotsRepo.saveStoreSnapshot(store, md5s)

    store
  }

  def applyStoreEvent(s: StoreView, e: CatalogEvent) : StoreView = {
    e match {
      case CloneStore(_,from_sid,from_sver,_) =>
        internalGetStoreView(from_sid, from_sver).copy(sid = s.sid)
      case CreateProduct(sid,pid,sver) =>
        s.copy(products = s.products + (pid ->Product(pid)))
      case SetPrice(_,pid,price,_) => s.withPrice(pid,price)
      case SetTitle(_,pid,title,_) => s.withTitle(pid,title)
      case _ => s
    }
  }
}

