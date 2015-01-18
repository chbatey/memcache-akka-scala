package info.batey.memcache

import akka.actor.Actor
import akka.io.Tcp.{Write, Received}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

class MemcacheConnection extends Actor with LazyLogging {
  override def receive: Receive = {
    case Received(data) =>
      logger.debug(s"received data $data")
      sender() ! Write(ByteString("STORED\r\n"))
    case msg @ _ =>
      logger.warn(s"received unknown message $msg")
  }
}
