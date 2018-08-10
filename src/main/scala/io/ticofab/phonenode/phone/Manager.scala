package io.ticofab.phonenode.phone

import akka.actor.{Actor, Props, RootActorPath}
import akka.cluster.Cluster
import akka.pattern.{ask, pipe}
import akka.stream.SourceRef
import io.ticofab.phonecommon.Messages.{CheckMatchingWith, DeviceActorReady, DeviceConnected, RegisterNode}
import io.ticofab.phonenode.phone.Manager.{FlowSource, GetFlowSource}
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

    case DeviceConnected(location) =>
      // spawn a new actor for each phone
      val name = s"${self.path.name}_${context.children.size + 1}"
      val device = context.actorOf(Props(new Device(location)), name)
      (device ? GetFlowSource) (3.seconds)
        .mapTo[FlowSource]
        .map(flowSource => DeviceActorReady(self, device, location, flowSource.ref))
        .pipeTo(sender)
  }

}

object Manager {

  case object GetFlowSource

  case class FlowSource(ref: SourceRef[String])

}
