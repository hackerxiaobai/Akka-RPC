package com.wl.rpc.complex

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
 * @author wanglei
 * @email wl_9322@163.com
 * @date 2020/9/6 11:16 下午
 */

class Worker(val masterHost: String, val masterPort: Int, val memory: Int, val cores: Int) extends Actor {
  var master: ActorSelection = _
  val workerId = UUID.randomUUID().toString
  val HEART_INTERVAL = 10000

  override def preStart(): Unit = {
    //跟Master建立连接
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
    //向Master发送注册消息,其实就是向 Master 申请注册，我的 id、我要多少内存、我要几个核
    master ! RegisterWorker(workerId, memory, cores)
  }

  override def receive: Receive = {
    case RegisteredWorker(masterUrl) => {
      println(masterUrl)
      //启动定时器发送心跳
      import context.dispatcher
      //多长时间后执行 单位,多长时间执行一次 单位, 消息的接受者(直接给master发不好, 先给自己发送消息, 以后可以做下判断, 什么情况下再发送消息), 信息
      context.system.scheduler.schedule(0 millis, HEART_INTERVAL millis, self, SendHeartbeat)
    }
    case SendHeartbeat => {
      println("send heartbeat to master")
      master ! Heartbeat(workerId)
    }
  }
}

object Worker {
  def main(args: Array[String]) {
    val host = args(0)
    val port = args(1).toInt
    val masterHost = args(2)
    val masterPort = args(3).toInt
    val memory = args(4).toInt
    val cores = args(5).toInt
    // 准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
    val actorSystem = ActorSystem("WorkerSystem", config)
    actorSystem.actorOf(Props(new Worker(masterHost, masterPort, memory, cores)), "Worker")
    actorSystem.awaitTermination()
  }
}
