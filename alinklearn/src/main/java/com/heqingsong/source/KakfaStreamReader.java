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

public class KakfaStreamReader {
    public static void main(String[] args) {
        Kafka011SourceStreamOp source = new Kafka011SourceStreamOp()
            .setBootstrapServers("localhost:9092")
            .setTopic("iris")
            .setStartupMode("EARLIEST")
            .setGroupId("alink_group");
    }
}
