package info.batey.memcache

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.io.Tcp.{Bound, CommandFailed, Bind}
import akka.io.{IO, Tcp}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await

object MemcacheMain extends App {
  val memcache = new MemcacheMain(9090)
  memcache.bind()

  def apply(port: Int) = new MemcacheMain(port)
}

class MemcacheMain(port: Int) extends LazyLogging {

  import akka.pattern.ask
  import scala.concurrent.duration._

  val actorSystem = ActorSystem("memcache")

  def bind(): Unit = {
    val server: ActorRef = actorSystem.actorOf(Props(classOf[MemcacheServer], new InetSocketAddress("localhost", port)), "MemcacheServer")
    logger.debug("Instructing server to bind")
    implicit val timeout: Timeout = Timeout(5 seconds)
    val future = server ? "bind"
    Await.result(future, timeout.duration).asInstanceOf[String]
    logger.debug("Server started, start the party")
  }

  def close(): Any = {
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }
}

class MemcacheServer(inetSocketAddress: InetSocketAddress) extends Actor with LazyLogging {

  import context.system

  val manager = IO(Tcp)
  var start : ActorRef = _

  override def receive: Receive = {
    case "bind" =>
      logger.debug(s"Binding to $inetSocketAddress")
      IO(Tcp) ! Bind(self, inetSocketAddress)
      start = sender()

    case cf @ CommandFailed(_: Bind) =>
      logger.debug(s"Bind failed :( $cf")
      context stop self

    case b @ Bound(localAddress) =>
      logger.debug(s"Bound to $inetSocketAddress, alerting our starter")
      start ! "we're done baby!"

    case msg @ _ =>
      logger.debug(s"Received message $msg")
  }
}
