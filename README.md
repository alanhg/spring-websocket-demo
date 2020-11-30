# spring-websocket-demo

## 功能

1. redis解决集群环境下，发送消息问题，同时存储活跃登陆用户
2. 支持集群情况下的`点对点发送`和`广播`,`主题广播`
3. 集群情况下房间支持，可以对单个房间下的所有用户发送消息，且可以获得房间中用户列表

##  问题

1. WS连接用户数即系统topic订阅用户情况
    - redis online表查询
2. 单个topic订阅用户情况
    - redis online room表查询

    
