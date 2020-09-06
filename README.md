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