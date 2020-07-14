/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.alibaba.alink.operator.stream.source;

import com.alibaba.alink.common.io.annotations.AnnotationUtils;
import com.alibaba.alink.common.io.annotations.IOType;
import com.alibaba.alink.common.io.annotations.IoOpAnnotation;
import com.alibaba.alink.operator.common.io.kafka.BaseKafkaSourceBuilder;
import com.alibaba.alink.operator.common.io.kafka011.Kafka011SourceBuilder;
import com.alibaba.alink.params.io.Kafka011SourceParams;
import org.apache.flink.ml.api.misc.param.Params;


/**
 * Data source for kafka 0.11.x.
 */
@IoOpAnnotation(name = "kafka011", hasTimestamp = true, ioType = IOType.SourceStream)
public final class Kafka011SourceStreamOp extends BaseKafkaSourceStreamOp<Kafka011SourceStreamOp>
    implements Kafka011SourceParams<Kafka011SourceStreamOp> {

    public Kafka011SourceStreamOp() {
        this(new Params());
    }

    public Kafka011SourceStreamOp(Params params) {
        super(AnnotationUtils.annotatedName(Kafka011SourceStreamOp.class), params);
    }

    @Override
    protected BaseKafkaSourceBuilder getKafkaSourceBuilder() {
        return new Kafka011SourceBuilder();
    }
}

