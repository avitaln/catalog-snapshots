//import catalog._
//import org.specs2.matcher.Scope
//import org.specs2.mock.Mockito
//import org.specs2.mutable.SpecificationWithJUnit
//import scala.util.Random
//
//
///**
// * Created by avitaln
// * 8/7/14
// */
//
//class SnapshotsTest extends SpecificationWithJUnit with Mockito {
//
//  sequential
//
//  trait Context extends Scope {
//
//    lazy val snapshotsRepo = new InMemSnapshotsRepository
//    lazy val versionsRepo = new InMemVersionsRepository
//    lazy val eventsRepo : EventsRepository = new InMemoryEventsRepository
//    lazy val catalog = new ProductsCatalog(snapshotsRepo, versionsRepo, eventsRepo)
//
//    def generateName() = "name" + (Random.nextInt(1000)+1)
//    def generatePrice() = Random.nextLong()%1000+1
//
//    def createProduct(sid: String, pid: String, title: String = generateName(), price: Long = generatePrice()) : Seq[CatalogEvent] = {
//      val events = Seq(
//        CreateProduct(sid, pid),
//        SetPrice(sid,pid,price),
//        SetTitle(sid,pid,title)
//      )
//      events.foreach(catalog.addEvent)
//      events
//    }
//
//    def setTitle(sid: String, pid: String, title: String) =
//      catalog.addEvent(SetTitle(sid,pid,title))
//
//    def setPrice(sid: String, pid: String, price: Long) =
//      catalog.addEvent(SetPrice(sid,pid,price))
//
//    def expectProduct(sid: String, pid: String, expectedTitle: String, expectedPrice: Long, expectedEventCount: Option[Int] = None) = {
//      catalog.getProduct(sid, pid) must beSome(Product(pid,expectedTitle,expectedPrice))
//    }
//
//    def cloneStore(sid: String, fromSid: String) = {
//      val storeVer = versionsRepo.getVersion(fromSid)
//      catalog.addEvent(CloneStore(sid,fromSid, storeVer))
//    }
//
//    def expectStoreWithProductIds(sid: String, productIds: Set[String]) = {
//      val sver = versionsRepo.getVersion(sid)
//      val store = catalog.getStoreView(sid)
//
//      store.sid must beEqualTo(sid)
//      store.sver must be_<=(sver)
//      store.productIds must beEqualTo(productIds)
//    }
//
//    def expectStoreWithFullProducts(sid: String, products: Map[String,_root_.catalog.Product]) = {
//      val sver = versionsRepo.getVersion(sid)
//      val store = catalog.getStoreView(sid)
//      store.sid must_== sid
//      store.sver must be_<=(sver)
//      store.products must_== products
//    }
//
//  }
//
//  "snapshots" should {
//
//    "create and read product" in new Context {
//      createProduct("s1","p1","name1",88)
//      setTitle("s1","p1","name2")
//      expectProduct("s1","p1","name2",88)
//    }
//
//    "clone store and read product from cloned store" in new Context {
//      createProduct("s1","p1","name1",88)
//      cloneStore("s2","s1")
//      expectProduct("s2","p1","name1",88)
//      setTitle( "s2", "p1", "name2")
//      setPrice( "s2", "p1", 99)
//      expectProduct("s2","p1","name2",99)
//      setPrice( "s1", "p1", 100)
//      expectProduct("s1","p1","name1",100)
//      expectProduct("s2","p1","name2",99)
//    }
//
//    "changing original store should not change cloned store" in new Context {
//      createProduct("s1","p1","name1",88)
//      createProduct("s1","p2","name2",99)
//      cloneStore("s2","s1")
//      setPrice("s1", "p1", 102)
//      expectProduct("s2","p1","name1",88)
//      expectProduct("s2","p2","name2",99)
//    }
//
//    /////////////////// stores with product ids //////////
//
//    "return a store with one product" in new Context {
//      createProduct("s1","p1")
//      expectStoreWithProductIds("s1",Set("p1"))
//    }
//
//    "return a store with many product" in new Context {
//      createProduct("s1","p1")
//      createProduct("s1","p2")
//      createProduct("s1","p3")
//      expectStoreWithProductIds("s1",Set("p1","p2","p3"))
//    }
//
//    "return a cloned store with products from cloned" in new Context {
//      createProduct("s1","p1")
//      createProduct("s1","p2")
//      cloneStore("s2","s1")
//      createProduct("s2","p3")
//      createProduct("s2","p4")
//      createProduct("s1","p5")
//      expectStoreWithProductIds("s2",Set("p1","p2","p3","p4"))
//      expectStoreWithProductIds("s1",Set("p1","p2","p5"))
//    }
//
//    "return store which is cloned from a cloned store" in new Context {
//      createProduct("s1","p1")
//      cloneStore("s2","s1")
//      createProduct("s2","p2")
//      cloneStore("s3","s2")
//      createProduct("s3","p3")
//      expectStoreWithProductIds("s3",Set("p1","p2","p3"))
//    }
//
//    /////////////////// full stores //////////
//
//    "return simple store with full products" in new Context {
//      createProduct("s1","p1","name1",88)
//      expectStoreWithFullProducts("s1",Map("p1"->Product("p1","name1",88)))
//    }
//
//    "return cloned store with full products" in new Context {
//      createProduct("s1","p1","name1",88)
//      cloneStore("s2","s1")
//      createProduct("s2","p2","name2",99)
//
//      expectStoreWithFullProducts("s2",Map(
//        "p1"->Product("p1","name1",88),
//        "p2"->Product("p2","name2",99)
//      ))
//
//    }
//
//
//    ///////////////////////////////////////////////
//
//    "product snaps hot with simple store" in new Context {
//      createProduct("s1","p1","name1",88)
//      catalog.getStoreView("s1") // TODO find another way to create a snapshot
//      eventsRepo.clear()
//      expectProduct("s1","p1","name1",88)
//    }
//
//    "product snaps hot with cloned store" in new Context {
//      createProduct("s1","p1","name1",88)
//      cloneStore("s2","s1")
//      createProduct("s2","p2","name2",99)
//      catalog.getStoreView("s1")
//      catalog.getStoreView("s2")
//      eventsRepo.clear()
//
//      expectProduct("s1","p1","name1",88)
//      expectProduct("s2","p1","name1",88)
//      expectProduct("s2","p2","name2",99)
//    }
//
//
//  }
//}
