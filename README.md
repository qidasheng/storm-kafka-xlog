storm-kafka-xlog
================

使用java语言开发的基于storm、kafka、xlog的web日志实时分析系统，实时分析指定时间周期内web日志中每个ip访问的各项数据指标(访问总次数，动、静态次数，GET、POST、HEAD等请求类型次数，HTTP状态码各种状态前缀次数，访问广度、sql注入和xss检测可疑度、useragent个数、是否使用代理访问)，可通过设置web日志指定字段的阀值选择性的记录分析结果到mysql数据库，通过分析数据能够做很多事情，比如判断拦截恶意访问ip，了解网站访问情况，对sql注入和xss跨站进行检测。


编译
--------
```Bash
mvn clean package 
```


生成包路径    
--------
```Bash
target/storm-xlog-version-jar-with-dependencies.jar  
```

创建数据库       
--------
库名可以自己定，导入storm_xlog.sql文件中的ips表结构           


配置    
--------
```Vim
#zookeeper.server地址，多个逗号分隔          
xlog.zookeeper.server=10.19.1.16,10.19.1.17     
#storm nimbus host        
xlog.nimbus.host=10.19.1.18      
#kafka topic name         
xlog.kafka.topic.name=test    
#mysql相关配置             
mysql.url=jdbc:mysql://10.19.1.19:3306/storm_xlog          
mysql.user=xlog           
mysql.password=123456      
#单个ip interval.time时间内至少请求超过多少次数才记录到mysql数据库             
insert.into.mysql.min.total=60      
#单个ip interval.time时间内sqlxss可疑度超过多少才记录到mysql数据库                 
insert.into.mysql.min.sqlxss=100      
#单个ip interval.time时间内请求广度小于多少的才记录到mysql数据库           
insert.into.mysql.max.scope=15    
#统计周期      
xlog.interval.time=60
#sql注入和跨站脚本检测黑名单字符(发现一个sqlxss加1)         
xlog.sqlxss.char=',", ,(,),..,|,\,+,null    
#sql注入和跨站脚本检测黑名单字符串(发现一个sqlxss加10)              
xlog.sqlxss.string=select ,alert(,prompt(,select(,sleep(,<script,_wvs     
#是否开启sql注入和跨站脚本检测，对性能有影响，如果不需要建议不开启              
xlog.sqlxss.enable=false   
#是否开启storm的debug功能      
xlog.debug=false     
```


部署：提交任务到单机storm或者storm集群    
--------
```Bash
storm jar target/storm-xlog-version-jar-with-dependencies.jar  storm.xlog.XlogKafkaSpoutTopology xlog.properties   
```

注意事项              
--------
确保web日志格式如下，以nginx为例        
```Vim
log_format  xlog  '$remote_addr $host $remote_user [$time_local] "$request" '
              '$status $body_bytes_sent "$http_referer" '
              '"$http_user_agent" "$http_x_forwarded_for" $upstream_response_time $request_time';
```
