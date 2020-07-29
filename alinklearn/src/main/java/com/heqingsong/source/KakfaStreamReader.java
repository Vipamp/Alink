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

import com.alibaba.alink.operator.stream.source.Kafka011SourceStreamOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.dataproc.vector.VectorAssembler;
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment;

public class KakfaStreamReader {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Kafka011SourceStreamOp source = new Kafka011SourceStreamOp()
            .setBootstrapServers("172.20.3.225:9092")
            .setTopic("test")
            .setStartupMode("EARLIEST")
            .setGroupId("alink_group");
        source.print();
        VectorAssembler va = new VectorAssembler()
            .setSelectedCols(new String[]{"c1"})
            .setOutputCol("c2");
        Pipeline pipeline = new Pipeline().add(va);
        PipelineModel model = pipeline.fit(source);
        model.transform(source);
        env.execute();
    }
}
