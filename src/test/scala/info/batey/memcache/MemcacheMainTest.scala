package info.batey.memcache

import java.net.{ConnectException, Socket}

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}

class MemcacheMainTest extends FunSuite with Matchers with BeforeAndAfter {
  val Port = 5555
  val Host: String = "localhost"

  var underTest : MemcacheMain = _

  before {
    underTest = MemcacheMain(Port)
  }

  after {
    underTest.close();
  }

  test("should bind to port") {
    underTest.bind()

    val socket = new Socket(Host, Port)
    socket.isConnected should equal(true)
  }

  test("should close port on close") {
    underTest.bind()

    underTest.close()

    intercept[ConnectException] {
      new Socket(Host, Port)
    }
  }
}
