package option2

/**
* Created by avitaln
* 9/16/14
*/

import domain._
import own.Lenses._

class CatalogEventHandler[O](handlers: Seq[PartialFunction[(O, CatalogEvent),O]]) {
  private[this] val nop : PartialFunction[(O, CatalogEvent),O] = { case (o, _) => o }
  lazy val handle = handlers.foldLeft(nop)((a, b) => b orElse a)
}

object ProductCreatedHandler {
  def func[O](idLens : Lens[O,String]) :PartialFunction[(O, CatalogEvent),O] = { case (o, ProductCreated(id)) => idLens.set(o, id)}
}

object ProductNamedHandler {
  def func[O](nameLens : Lens[O,String]) : PartialFunction[(O, CatalogEvent),O] = { case (o, ProductNamed(_, name)) => nameLens.set(o, name)}
}


object ProductPricedHandler {
  def func[O](priceLens : Lens[O,Int]) : PartialFunction[(O, CatalogEvent),O]= { case (o, ProductPriced(_, price)) => priceLens.set(o, price)}
}

class CatalogEventPlayer[O](handler: CatalogEventHandler[O]) {
  def applyEvents(o: O, events: Seq[CatalogEvent]): O = {
    events.foldLeft(o)((page, event) => applyEvent(page, event))
  }

  private def applyEvent(o: O, event: CatalogEvent): O = handler.handle((o, event))
}

object EventPlayer2 extends App {
  val ProductView1_Id = Lens[ProductView1,String](_.id, (o, p) => o.copy(id = p))
  val ProductView1_Name = Lens[ProductView1,String](_.name, (o, p) => o.copy(name = p))
  val ProductView1_Price = Lens[ProductView1,Int](_.price, (o, p) => o.copy(price = p))

  val ProductView2_Id = Lens[ProductView2,String](_.id, (o, p) => o.copy(id = p))
  val ProductView2_Name = Lens[ProductView2,String](_.name, (o, p) => o.copy(name = p))

  val handlers1 = Seq(
    ProductCreatedHandler.func[ProductView1](ProductView1_Id),
    ProductNamedHandler.func[ProductView1](ProductView1_Name),
    ProductPricedHandler.func[ProductView1](ProductView1_Price)
  )
  val handlers2 = Seq(
    ProductCreatedHandler.func[ProductView2](ProductView2_Id),
    ProductNamedHandler.func[ProductView2](ProductView2_Name)
  )

  val events = Seq[CatalogEvent](ProductCreated("some_id"), ProductNamed("some_id","some_name"), ProductPriced("some_id",88))

  val p1 = new CatalogEventPlayer[ProductView1](new CatalogEventHandler(handlers1)).applyEvents(ProductView1(), events)
  val p2 = new CatalogEventPlayer[ProductView2](new CatalogEventHandler(handlers2)).applyEvents(ProductView2(), events)

  println(p1)
  println(p2)
}

