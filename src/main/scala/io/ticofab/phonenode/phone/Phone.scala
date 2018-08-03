package io.ticofab.phonenode.phone

import akka.actor.{Actor, ActorRef}
import io.ticofab.phonecommon.Location
import io.ticofab.phonecommon.Messages.{CheckMatchingWith, YouMatchedWith}
import wvlet.log.LogSupport

class Phone(myLocation: Location) extends Actor with LogSupport {

  info(s"phone actor ${self.path.name} created for location $myLocation")

  override def receive: Receive = {
    case CheckMatchingWith(phone, itsLocation) =>
      if (phone != self && myLocation.isCloseEnoughTo(itsLocation)) {
        logMatched(self, phone)
        phone ! YouMatchedWith(self)
      }

    case YouMatchedWith(phone) =>
      logMatched(self, phone)
  }

  // logs that this phone matched with another phone
  def logMatched(me: ActorRef, it: ActorRef) = info(s"phone ${me.path.name}, matched with phone ${it.path.name}")

}

