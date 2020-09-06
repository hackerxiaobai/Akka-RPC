package com.wl.rpc.complex

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * @author wanglei
 * @email wl_9322@163.com
 * @date 2020/9/6 11:16 下午
 */

class Master(val host: String, val port: Int) extends Actor{
  // workerId -> WorkerInfo
  val idToWorker = new mutable.HashMap[String, WorkerInfo]()
  // WorkerInfo
  val workers = new mutable.HashSet[WorkerInfo]() //使用set删除快, 也可用linkList
  //超时检查的间隔
  val CHECK_INTERVAL = 15000

  override def preStart(): Unit = {
    println("preStart invoked")
    //导入隐式转换
    import context.dispatcher //使用timer太low了, 可以使用akka的, 使用定时器, 要导入这个包
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
  }

  override def receive: Receive = {

    case RegisterWorker(id, memory, cores) => {
      //把Worker的信息封装起来保存到内存当中
      val workerInfo = new WorkerInfo(id, memory, cores)
      idToWorker(id) = workerInfo
      workers += workerInfo
      sender ! RegisteredWorker(s"akka.tcp://MasterSystem@$host:$port/user/Master")//通知worker注册
    }

    case Heartbeat(id) => {
      if(idToWorker.contains(id)){
        val workerInfo = idToWorker(id)
        //报活
        val currentTime = System.currentTimeMillis()
        workerInfo.lastHeartbeatTime = currentTime
      }
    }

    case CheckTimeOutWorker => {
      val currentTime = System.currentTimeMillis()
      val toRemove = workers.filter(x => currentTime - x.lastHeartbeatTime > CHECK_INTERVAL)
      for(w <- toRemove) {
        workers -= w
        idToWorker -= w.id
      }
      println(workers.size)
    }
  }
}

object Master {
  def main(args: Array[String]) {

    val host = args(0)
    val port = args(1).toInt
    // 准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
    val actorSystem = ActorSystem("MasterSystem", config)
    //创建Actor
    val master = actorSystem.actorOf(Props(new Master(host, port)), "Master")
    actorSystem.awaitTermination()
  }
}
