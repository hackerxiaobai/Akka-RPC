package com.wl.rpc.complex

/**
 * @author wanglei
 * @email wl_9322@163.com
 * @date 2020/9/6 11:31 下午
 */
class WorkerInfo(val id: String, val memory: Int, val cores: Int) {
  //TODO 上一次心跳
  var lastHeartbeatTime : Long = _
}
