package info.batey.memcache

import java.util

import akka.actor.FSM
import akka.io.Tcp.{Write, Received}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

sealed trait State
case object NoData extends State
case object PartialData extends State

sealed trait Data
case object Empty extends Data
final case class Partial(byteString: ByteString) extends Data

class MemcacheConnectionFSM extends FSM[State, Data] with LazyLogging {
  startWith(NoData, Empty)


  when(NoData) {
    case Event(Received(byteString), Empty) =>
      parseMessage(byteString)
      stay()
  }

  when(PartialData) {
    case Event(Received(byteString), Partial(oldData)) =>
      parseMessage(byteString ++ oldData)
      goto(NoData) using Empty
  }

  onTransition {
    case NoData -> PartialData => logger.debug("Received partial data")
  }

  initialize()


  def parseMessage(data: ByteString) = {
//    val iterator = data.iterator
//
//    val command = iterator.takeWhile(_ != '\n').toByteString.utf8String.trim
//    iterator.
//    val array = command.split("\\s+")
//    val commandType = array(0)
//    logger.debug(s"Received command type: $commandType whole command: $command")
//
//    commandType match {
//      case "set" =>
//        val key = array(1)
//        val dataSize = array(4).toInt
//        logger.debug(s"Expecting $dataSize bytes")
//        val dataArray: Array[Byte] = new Array[Byte](dataSize)
//        iterator.getBytes(dataArray)
//        iterator.getByte
//        iterator.getByte
//    }

    sender() ! Write(ByteString("STORED\r\n"))
  }
}
