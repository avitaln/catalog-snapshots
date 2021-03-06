package own

/**
 * Created by avitaln
 * 9/17/14
 */
object Lenses {
  case class Lens[O, V](get: O => V, set: (O, V) => O)

  def compose[O, I, V](lens1: Lens[O, I], lens2: Lens[I, V]): Lens[O, V] =
    Lens(
      lens1.get andThen lens2.get,
      (o, v) =>
        lens1.set(o,
          lens2.set(lens1.get(o), v)))

//  def modify[O, V](lens: Lens[O, V], obj: O)(fn: V => V) =
//    lens.set(obj, fn(lens.get(obj)))

//  def lset[O,V](l: Lens[_,V], o: O, v: V) : O = l.asInstanceOf[Lens[O,V]].set(o, v)
//
//  def lget[O,V](l: Lens[O,V], o: O) : V = l.get(o)
}