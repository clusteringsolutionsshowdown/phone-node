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
