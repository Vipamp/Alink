## 1、新建 alink 的maven 项目
### （1）新建 maven 项目
### （2）导入 alink 的基本使用的 pom 依赖
```xml
    <dependency>
        <groupId>com.alibaba.alink</groupId>
        <artifactId>alink_core_flink-1.10_2.11</artifactId>
        <version>1.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-scala_2.11</artifactId>
        <version>1.10.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-table-planner_2.11</artifactId>
        <version>1.10.0</version>
    </dependency>
```

## 2、在 flink 集群中部署 alink 
### （1）部署 alink ：
* 下载 whl 文件，并解压。
* 拿出 pyalink/lib 下面的三个 jar 包。
* 将该三个 jar 包，部署到 flink 集群即可。
### （2）启动：
* standalone 模式启动：在flink 的配置项中加入```classloader.resolve-order: parent-first```，正常启动即可。
