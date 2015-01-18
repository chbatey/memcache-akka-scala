package info.batey.memcache

import akka.actor.ActorSystem
import akka.io.Tcp.{Received, Write}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfter, FunSuiteLike, Matchers}

class MemcacheConnectionTest extends TestKit(ActorSystem("MemcacheConnectionTest")) with FunSuiteLike
  with ImplicitSender with Matchers with BeforeAndAfter {
  var underTest : TestActorRef[MemcacheConnection] = _

  before {
    underTest = TestActorRef[MemcacheConnection](new MemcacheConnection)
  }

  test("Should respond with STORED") {
    underTest ! Received(ByteString("set batey 0 100 2\r\n".getBytes ++ Array[Byte](1, 2) ++ "\r\n".getBytes))
    expectMsg(Write(ByteString("STORED\r\n")))
  }
}
