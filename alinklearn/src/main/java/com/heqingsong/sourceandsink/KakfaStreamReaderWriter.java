/**
 * File: KakfaStreamReader 　　2020/07/14 14:39
 * <p>
 * Copyright (c) 2018-2028  HeQingsong(ahheqingsong@126.com) All rights reserved.
 * <p>
 * //TODO
 *
 * @version 1.0
 * @author HeQingsong
 * @since JDK1.8
 */
package com.heqingsong.sourceandsink;

import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.JsonValueStreamOp;
import com.alibaba.alink.operator.stream.sink.Kafka011SinkStreamOp;
import com.alibaba.alink.operator.stream.source.Kafka011SourceStreamOp;
import com.alibaba.alink.params.io.shared_params.HasDataFormat;

public class KakfaStreamReaderWriter {

    /**
     * 测试 Alink 接入kafka 获取json 格式的数据，解析后转换成 Table 的形式，并修改
     * 各字段的数据类型。
     * 参考：https://zhuanlan.zhihu.com/p/101106492
     * <p>
     * kakfa 数据的输入示例: {"name":"hqs","age":25}
     * 打印结果为：
     * name|age
     * ----|---
     * hqs |14
     *
     * @author HeQingsong
     */
    public static void main(String[] args) throws Exception {
        Kafka011SourceStreamOp source = new Kafka011SourceStreamOp()
            .setBootstrapServers("172.20.3.225:9092")
            .setTopic("input")
            .setStartupMode("LATEST")
            .setGroupId("alink_group");
        StreamOperator message = source.link(new JsonValueStreamOp()
            .setSkipFailed(true)
            .setSelectedCol("message")
            .setOutputCols(new String[]{"name", "age"})
            .setJsonPath(new String[]{"$.name", "$.age"}))
            .select("CAST(name AS STRING) AS name,"
                + "CAST(age AS INTEGER) AS age");
        message.getOutputTable().printSchema();
        message.print();

        // 将数据实时 sink 到 kafka 队列中。
        Kafka011SinkStreamOp sink = new Kafka011SinkStreamOp()
            .setBootstrapServers("172.20.3.225:9092")
            .setDataFormat(HasDataFormat.DataFormat.CSV)
            .setTopic("output");
        sink.linkFrom(message);
        StreamOperator.execute();
    }
}
