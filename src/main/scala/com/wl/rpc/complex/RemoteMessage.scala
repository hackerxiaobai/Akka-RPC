package com.wl.rpc.complex

/**
 * @author wanglei
 * @email wl_9322@163.com
 * @date 2020/9/6 11:25 下午
 */

//通过 case class 发送消息，因为要走网络，所以必须序列化
trait RemoteMessage extends Serializable

//Worker -> Master
case class RegisterWorker(id: String, memory: Int, cores: Int) extends RemoteMessage

//Worker 向 Master 发送心跳
case class Heartbeat(id: String) extends RemoteMessage

//Master -> Worker
case class RegisteredWorker(masterUrl: String) extends RemoteMessage

//Worker -> self
case object SendHeartbeat

// Master -> self
case object CheckTimeOutWorker