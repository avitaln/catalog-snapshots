import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.mutable


/**
 * Created by avitaln
 * 8/25/14
 */

case class StoreWithProductIds(sid: String, sver: Long, productMap: Map[String, String] = Map[String,String]()) {
  def productIds = productMap.keys
  def productMapWith[T](mapper: (String) => T) = productMap.map(pair=>pair._1->mapper(pair._2))
  def md5s = productMap.values.toSeq.toSet
  def md5(pid: String) : Option[String] = productMap.get(pid)
}

trait SnapshotsRepository {
  def getLatestFullStoreSnapshot(sid: String, sver: Long) : Option[(StoreView,Set[String])]
  def getLatestStoreSnapshot(sid: String, maxVer: Long) : Option[(StoreWithProductIds)]
  def saveStoreSnapshot(fullStore: StoreView, previousMD5s: Set[String])
  def productSnapshot(md5: String) : Option[Product]
}

class InMemSnapshotsRepository extends SnapshotsRepository {

  def getLatestFullStoreSnapshot(sid: String, sver: Long) : Option[(StoreView,Set[String])] = {
    getLatestStoreSnapshot(sid, sver).map { s =>
      StoreView(s.sid,s.sver,s.productMapWith(products)) -> s.md5s
    }
  }

  val om = {
    val om1 = new ObjectMapper
    om1.registerModule(new DefaultScalaModule)
    om1
  }

  def dump() = {
    println("#### stores\n")

    stores.values.foreach { s=>
      println(s"sid=${s.sid} sver=${s.sver}")
      s.productMap.foreach { p=>
        println(p)
      }
      println()
    }

    println("#### products\n")

    products.foreach {
      println
    }
    println()


  }


  // map store-id,store-ver to Store snapshot
  val stores = mutable.HashMap[(String,Long), StoreWithProductIds]()
  // map md5->product
  val products = mutable.HashMap[String, Product]()

  def getLatestStoreSnapshot(sid: String, maxVer: Long) : Option[(StoreWithProductIds)] = { // ordey by limit 1
  val filteredStores = stores.filterKeys(pair => pair._1 == sid && pair._2 <= maxVer).values
    if (filteredStores.isEmpty) None else Some(filteredStores.maxBy(_.sver))
  }

  private def createStoreSnapshot(store: StoreWithProductIds) = synchronized {
    val key = store.sid->store.sver
    //    if (stores.contains(key)) throw new RuntimeException("store snapshot already exist")
    stores.update(store.sid->store.sver, store)
  }

  def saveStoreSnapshot(fullStore: StoreView, previousMD5s: Set[String]) = {
    val productMap = fullStore.products.map { pair=>pair._1 -> md5(pair._2) }
    val storeSnapshot = StoreWithProductIds(fullStore.sid, fullStore.sver, productMap)
    val needToBeSaved = productMap.filterNot(pair=>previousMD5s.contains(pair._2))
    needToBeSaved.foreach { pair => createProductSnapshot(fullStore.getProduct(pair._1)) }
    createStoreSnapshot(storeSnapshot)
  }

  private def md5(p: Product) : String = MD5.fromString(om.writeValueAsString(p))

  private def getLatestProductSnapshot(sid: String, maxVer: Long, pid: String) : Option[Product] = {
    val maybeStore = getLatestStoreSnapshot(sid, maxVer)
    val maybeProduct = maybeStore.flatMap { _.productMap.get(pid).flatMap(getProductSnapshot) }
    maybeProduct
  }

  private def getProductSnapshot(md5: String) : Option[Product] = products.get(md5)

  private def createProductSnapshot(product: Product) : Unit = createProductSnapshot(md5(product), product)

  private def createProductSnapshot(md5: String, product: Product) : Unit = synchronized {
    //    if (products.contains(md5)) throw new RuntimeException("product snapshot already exist")
    products.update(md5, product)
  }

  def productSnapshot(md5: String) : Option[Product] = products.get(md5)
}


