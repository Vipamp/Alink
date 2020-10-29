## 1. 新建 alink 的maven 项目
### 1.1 新建 maven 项目
### 1.2 导入 alink 的基本使用的 pom 依赖，这是基础依赖，如果使用了其他功能，需要针对于功能增加依赖。
```xml
    <dependencys>
       <dependency>
           <groupId>com.alibaba.alink</groupId>
           <artifactId>alink_core_flink-1.11_2.11</artifactId>
           <version>1.2-SNAPSHOT</version>
       </dependency>
       <dependency>
           <groupId>org.apache.flink</groupId>
           <artifactId>flink-streaming-scala_2.11</artifactId>
           <version>1.11.0</version>
       </dependency>
       <dependency>
           <groupId>org.apache.flink</groupId>
           <artifactId>flink-table-planner_2.11</artifactId>
           <version>1.11.0</version>
       </dependency>
    </dependencys>
```

## 2.在 flink 集群中部署 alink 
### 2.1 部署 alink ：
* 下载 whl 文件，并解压，如果安装过 pyalink，在 $PYTHON_HOME/site-packages/pyalink/lib 下。
* 拿出 pyalink/lib 下面的三个 jar 包：
  * alink_connector_all-1.2.0.jar
  * alink_core_flink-1.10_2.11-1.2.0.jar
  * alink_python-1.2.0-shaded.jar
* 将该三个 jar 包，复制到 $FLINK_HOME/lib 下面。
### 2.2 启动：
* standalone 模式启动：在flink 的配置项中加入```classloader.resolve-order: parent-first```，正常启动即可。

## 3. 目前情况：
### 3.1 alink-connector:
* hive: 目前支持，暂未测试
* mysql: 目前测试支持 5.x.x，对 8.x 的支持目前好像有问题。
