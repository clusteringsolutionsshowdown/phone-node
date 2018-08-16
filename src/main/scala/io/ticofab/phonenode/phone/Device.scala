package io.ticofab.phonenode.phone

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import akka.stream.scaladsl.{Keep, Sink, Source, StreamRefs}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import io.ticofab.phonecommon.Location
import io.ticofab.phonecommon.Messages.{CheckMatchingWith, MessageForMatchedDevice, YouMatchedWith}
import io.ticofab.phonenode.phone.Manager.{FlowSource, GetFlowSource}
import org.reactivestreams.Publisher
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

class Device(myLocation: Location) extends Actor with LogSupport {

  implicit val as = context.system
  implicit val am = ActorMaterializer()

  info(s"phone actor ${self.path.name} created for location $myLocation")

  val (down, publisher: Publisher[String]) = Source
    .actorRef[String](1000, OverflowStrategy.fail)
    .toMat(Sink.asPublisher(fanout = false))(Keep.both)
    .run()

  val streamRef = Source.fromPublisher(publisher).runWith(StreamRefs.sourceRef())

  var matchedDevice: Option[ActorRef] = None

  override def receive: Receive = {
    case GetFlowSource => streamRef.map(FlowSource).pipeTo(sender)

    case CheckMatchingWith(device, itsLocation) =>
      if (device != self && myLocation.isCloseEnoughTo(itsLocation)) {
        matchedDevice = Some(device)
        logMatched(self, device)
        down ! s"I found a match with device '${device.path.name}'!"
        device ! YouMatchedWith(self)
      }

    case YouMatchedWith(device) =>
      down ! s"I got notified that I matched with device '${device.path.name}'!"
      logMatched(self, device)

    case MessageForMatchedDevice(msg) =>
      debug(s"received a message for my matched one: $msg")
      matchedDevice.foreach(_ ! msg)

    case msg: String =>
      debug(s"received a message for my device: $msg")
      down ! msg
  }

  // logs that this phone matched with another phone
  def logMatched(me: ActorRef, it: ActorRef) = info(s"phone ${me.path.name}, matched with phone ${it.path.name}")

}

