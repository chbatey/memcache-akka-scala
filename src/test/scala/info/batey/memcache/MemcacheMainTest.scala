package info.batey.memcache

import java.io.{OutputStream, DataInputStream, BufferedReader, InputStreamReader}
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

  test("should send back STORED") {
    underTest.bind()

    val socket = new Socket(Host, Port)
    val outputStream: OutputStream = socket.getOutputStream
    outputStream.write("set batey 0 100 2\r\n".getBytes)
    outputStream.write(Array[Byte](1, 2))
    outputStream.write("\r\n".getBytes)
    val dataInputReader = new DataInputStream(socket.getInputStream)

    dataInputReader.readLine() should equal("STORED")
  }
}
