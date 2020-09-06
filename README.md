# Akka-RPC
> 周六周日闲来无事， 将读研期间学习大数据时候的一些东西拿出来看，扫了一眼还是RPC这个东西有点意思，顺手就写个demo复习一下
>

### simple_rpc
1. 非常简单的一个rpc通信，其实都是模版代码，没什么意思
2. 一个 Master 和 一个 Worker
3. Master 启动起来 一直监听 Worker 端发过来的请求
4. Master 接到请求 给 Worker 一个响应
5. 通过 scala 的模式匹配来匹配相关请求，complex_rpc 用到 case class 
6. case class 可以做模式匹配，另一个好处是用来传递数据


### complex_rpc
+ 使用到 scala 的 小技术
    + case class
    + 模式匹配
    + 隐式转换
+ Akka 自带的一个时间调度定时器
+ 简单说明
    + 首先启动 Master，等待很多 Worker的连接注册相关信息
    + 然后启动 Worker， 在 Worker 的 preStart 函数中 尝试连接 Master，
    + 连接成功，就像 Master 发送 RegisterWorker(workerId, memory, cores) 注册消息
    + Master 的 receive 函数中 通过一个 case RegisterWorker(id, memory, cores) 来模式匹配
    + 匹配成功 就将 Worker 的相关信息 放到 idToWorker 和 workers 里面，做一个记录作用
    + 随后 像 Worker 发送 注册成功消息 sender ! RegisteredWorker
    + Worker 在 receive 函数里匹配到该消息，其实就可以做一些相关业务逻辑的事情，随后要向 Master 报活
    + 调用 akka 自带的一个时间定时器 context.system.scheduler.schedule
    + 在时间定时器里可以发一个参数 SendHeartbeat，其实就是发给他自己，做一层转接，（直接发给master也是可以的）
    + 在 case SendHeartbeat 里 向 Master 报活，告诉 Master 我还活着
    + Master 接到该消息，就去更新 该 Worker 的 lastHeartbeatTime 时间，以便 在后续的 检测中，看看它是不是一个活着的Worker
    + 在 Master 的 preStart 函数里 每隔 CHECK_INTERVAL 时间 定时 检测一下 哪些Worker 还活着
    + 死了 的 Worker 就从 idToWorker、workers 剔除
    + 以后 Master 接到任务就从活着的这些 Worker 中挑选相应 适合的 Worker 去执行相关任务
    
### 总结
+ 没啥好说的，使用 akka 实现一个 远程过程调用（RPC），就是这么简单