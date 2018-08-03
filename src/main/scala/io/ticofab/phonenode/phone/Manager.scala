package io.ticofab.phonenode.phone

import akka.actor.{Actor, Props, RootActorPath}
import akka.cluster.Cluster
import io.ticofab.phonecommon.Messages.{CheckMatchingWith, PhoneActorReady, PhoneConnected, RegisterNode}
import wvlet.log.LogSupport

class Manager extends Actor with LogSupport {
  info(s"starting, name is ${self.path.name}")

  // make sure that we register only we we are effectively up
  val cluster = Cluster(context.system)
  cluster registerOnMemberUp {
    cluster.state.leader.foreach(leaderAddress =>
      context.actorSelection(RootActorPath(leaderAddress) / "user" / "supervisor") ! RegisterNode)
  }

  override def receive = {
    case cmw: CheckMatchingWith =>
      // asks all my kids if they match
      context.children.foreach(_ forward cmw)

    case PhoneConnected(location) =>
      // spawn a new actor for each phone
      val name = s"${self.path.name}_${context.children.size + 1}"
      val phone = context.actorOf(Props(new Phone(location)), name)
      sender ! PhoneActorReady(phone, location)
  }

}
