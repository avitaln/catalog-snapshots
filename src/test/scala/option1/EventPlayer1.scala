package option1

/**
 * Created by avitaln
 * 9/16/14
 */

import domain._
import own.Lenses._

trait CatalogEventHandler[O] {
  type Handler = PartialFunction[(O, CatalogEvent),O]

  private[this] val nop: Handler = { case (o, _) => o }
  private[this] var handlers: Seq[Handler] = Nil

  def registerHandler(handler: Handler): Unit = handlers +:= handler

  lazy val handle = handlers.foldLeft(nop)((a, b) => b orElse a)
}

trait ProductCreatedHandler[O] extends CatalogEventHandler[O] {
  val idLens : Lens[O,String]
  
  registerHandler { case (o, ProductCreated(id)) => idLens.set(o,id) }
}

trait ProductNamedHandler[O] extends CatalogEventHandler[O] {
  val nameLens : Lens[O,String]

  registerHandler { case (o, ProductNamed(_, name)) => nameLens.set(o, name) }
}


trait ProductPricedHandler[O] extends CatalogEventHandler[O] {
  val priceLens : Lens[O,Int]

  registerHandler { case (o, ProductPriced(_, price)) => priceLens.set(o, price)}
}

class CatalogEventPlayer[O](handler: CatalogEventHandler[O]) {
  def applyEvents(o: O, events: Seq[CatalogEvent]): O = {
    events.foldLeft(o)((page, event) => applyEvent(page, event))
  }

  private def applyEvent(o: O, event: CatalogEvent): O = handler.handle((o, event))
}

object EventPlayer1 extends App {
  val ProductView1_Id = Lens[ProductView1,String](_.id, (o, p) => o.copy(id = p))
  val ProductView1_Name = Lens[ProductView1,String](_.name, (o, p) => o.copy(name = p))
  val ProductView1_Price = Lens[ProductView1,Int](_.price, (o, p) => o.copy(price = p))

  val ProductView2_Id = Lens[ProductView2,String](_.id, (o, p) => o.copy(id = p))
  val ProductView2_Name = Lens[ProductView2,String](_.name, (o, p) => o.copy(name = p))

  val handlers1 = new ProductCreatedHandler[ProductView1] with ProductNamedHandler[ProductView1] with ProductPricedHandler[ProductView1] {
    val idLens = ProductView1_Id
    val nameLens = ProductView1_Name
    val priceLens = ProductView1_Price
  }

  val handlers2 = new ProductCreatedHandler[ProductView2] with ProductNamedHandler[ProductView2] {
    val idLens = ProductView2_Id
    val nameLens = ProductView2_Name
  }

  val events = Seq[CatalogEvent](ProductCreated("some_id"), ProductNamed("some_id","some_name"), ProductPriced("some_id",88), ProductNamed("some_id","some_other_name"))

  val p1 = new CatalogEventPlayer[ProductView1](handlers1).applyEvents(ProductView1(), events)
  val p2 = new CatalogEventPlayer[ProductView2](handlers2).applyEvents(ProductView2(), events)

  println(p1)
  println(p2)
}

