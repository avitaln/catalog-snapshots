import java.util.concurrent.atomic.AtomicLong

/**
 * Created by avitaln
 * 8/26/14
 */
trait VersionsRepository {
  def nextVersion(sid: String) : Long
  def getVersion(sid: String) : Long
}

class InMemVersionsRepository extends VersionsRepository {
  val lastStoreVersion = ConcurrentHashMap[String, AtomicLong]()
  def nextVersion(sid: String) : Long = {
    val atomicLong = lastStoreVersion.getOrElseUpdate(sid, new AtomicLong(0))
    val ver = atomicLong.incrementAndGet()
    ver
  }
  def getVersion(sid: String) : Long = lastStoreVersion(sid).longValue
}

