package info.batey.memcache

import akka.actor.ActorSystem
import akka.io.Tcp.{Write, Received}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfter, FunSuiteLike, Matchers}

class MemcacheConnectionFSMTest extends TestKit(ActorSystem("MemcacheConnectionTest")) with FunSuiteLike
  with ImplicitSender with Matchers with BeforeAndAfter {

  var underTest: TestActorRef[MemcacheConnectionFSM] = _

  before {
    underTest = TestActorRef[MemcacheConnectionFSM](new MemcacheConnectionFSM)
  }

  test("Should respond with STORED") {
    underTest ! Received(ByteString("set batey 0 100 2\r\n".getBytes ++ Array[Byte](1, 2) ++ "\r\n".getBytes))
    expectMsg(Write(ByteString("STORED\r\n")))
  }

  test("Should do nothing if data does not arrive") {
    val partialMessage: ByteString = ByteString("set batey 0 100 2\r\n".getBytes)
    underTest ! Received(partialMessage)
    expectNoMsg()
  }

}
