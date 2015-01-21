package info.batey.memcache

import java.util
import java.util.NoSuchElementException

import akka.actor.FSM
import akka.actor.FSM.Event
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
  }

  when(PartialData) {
    case Event(Received(newData), Partial(oldData)) =>
      parseMessage(oldData ++ newData)
  }

  onTransition {
    case NoData -> PartialData => logger.debug("Received partial data")
  }

  initialize()


  def parseMessage(data: ByteString): State = {
    try {
      logger.debug(s"Processing raw data $data")
      val command = data.takeWhile(_ != '\n'.toByte).utf8String.trim
      val restOfMessage = data.drop(command.length + 1)

      val commandParts = command.split("\\s+")
      val commandType = commandParts(0)
      val key = commandParts(1)

      // assume set for now
      val dataLength = commandParts(4).toInt
      val iterator = restOfMessage.iterator
      //    logger.debug(s"Received command $command data left is ${iterator.size} and expected data is $dataLength")

      val setData = new Array[Byte](dataLength)

      iterator.getBytes(setData)

      logger.debug(s"Received data ${util.Arrays.toString(setData)}")

      iterator.drop(2) // the end of line

      sender() ! Write(ByteString("STORED\r\n"))

      goto(NoData) using Empty
    }
    catch {
      case e:NoSuchElementException =>
        logger.debug(s"Not received whole message, keeping it for next time data arrives", e)
        goto(PartialData) using Partial(data)
    }

  }
}
