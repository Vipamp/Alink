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
package com.heqingsong.source;

import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.JsonValueStreamOp;
import com.alibaba.alink.operator.stream.source.Kafka011SourceStreamOp;
import org.junit.Test;

public class KakfaStreamReader {

    /**
     * 测试 Alink 接入kafka 获取json 格式的数据，解析后答应成 Table 的形式。
     * <p>
     * kakfa 数据的输入示例: {"name":"hqs","age":25}
     * 打印结果为：
     * name|age
     * ----|---
     * hqs |14
     *
     * @author HeQingsong
     */
    @Test
    public void test() throws Exception {
        Kafka011SourceStreamOp source = new Kafka011SourceStreamOp()
            .setBootstrapServers("172.20.3.225:9092")
            .setTopic("test")
            .setStartupMode("LATEST")
            .setGroupId("alink_group");
        source.link(new JsonValueStreamOp()
            .setSkipFailed(false)
            .setSelectedCol("message")
            .setOutputCols(new String[]{"name", "age"})
            .setJsonPath(new String[]{"$.name", "$.age"}))
            .select("name,age").print();
        StreamOperator.execute();
    }
}
