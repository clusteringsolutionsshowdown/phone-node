package io.ticofab.phonenode.phone

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import akka.stream.scaladsl.{Keep, Sink, Source, StreamRefs}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import io.ticofab.phonecommon.Location
import io.ticofab.phonecommon.Messages.{CheckMatchingWith, YouMatchedWith}
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

  override def receive: Receive = {
    case GetFlowSource => streamRef.map(FlowSource).pipeTo(sender)

    case CheckMatchingWith(phone, itsLocation) =>
      if (phone != self && myLocation.isCloseEnoughTo(itsLocation)) {
        logMatched(self, phone)
        down ! s"I found a match with device '${phone.path.name}'!"
        phone ! YouMatchedWith(self)
      }

    case YouMatchedWith(phone) =>
      down ! s"I got notified that I matched with device '${phone.path.name}'!"
      logMatched(self, phone)
  }

  // logs that this phone matched with another phone
  def logMatched(me: ActorRef, it: ActorRef) = info(s"phone ${me.path.name}, matched with phone ${it.path.name}")

}

