package utils

/**
 * Created by avitaln
 * 9/11/14
 */
object aoptest extends App {
  val r1b = new Renderer1 with RendererLogging with InstanceChecker
  r1b.render(Map("instance"->"good"))
}

trait Renderer {
  def render(params: Map[String,String])
}

trait RendererLogging extends Renderer {
  abstract override def render(params: Map[String,String]) {
    println("logging enter")
    super.render(params)
    println("logging exit")
  }
}

trait InstanceChecker extends Renderer {
  abstract override def render(params: Map[String,String]) {
    println("check instance...")
    val instance = params("instance")
    if (instance == "good") {
      super.render(params)
    } else {
      throw new RuntimeException("illegal instance")
    }
  }
}

class Renderer1 extends Renderer {
  def render(params: Map[String,String]) = println(" . .. . . RENDER1 . . .. ")
}
