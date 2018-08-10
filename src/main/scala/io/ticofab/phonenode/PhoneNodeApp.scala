package io.ticofab.phonenode

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import io.ticofab.phonenode.phone.Manager
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log.{LogLevel, LogSupport, Logger}

object PhoneNodeApp extends App with LogSupport {
  Logger.setDefaultFormatter(SourceCodeLogFormatter)
  Logger.setDefaultLogLevel(LogLevel.DEBUG)
  info("phone app starting")
  val as = ActorSystem("showdown")

  val port = ConfigFactory.load().getInt("akka.remote.netty.tcp.port")
  as.actorOf(Props[Manager], s"manager_$port")

}

/*

  SPECIFICATIONS:

  - there is one "listener" node which listens to incoming connection requests
  - each request contains simply location information (lat, lon) in integer
  - "phone" nodes store state about connected phones
  - upon a new connection, the listener sends a message to all phone nodes
  - each phone actor checks if it matches with the newly connected phone
  - the listener chooses the phone node to which send the newly connected phone as the one which has the least load
  - if all phone nodes are approaching full load, the listener will require the spawning of a new node // TODO

  LIMITATIONS for the demo:

  - we don't deal with failures except maybe notifying them
  - we only use a pseudo-location as state of a connected phone
  - we only work with scaling up the nodes and not down // TODO discuss

 */
