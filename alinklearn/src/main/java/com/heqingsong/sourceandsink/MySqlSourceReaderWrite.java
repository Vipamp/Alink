/**
 * File: MySqlSourceReader 　　2020/08/03 11:05
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

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.sink.MySqlSinkBatchOp;
import com.alibaba.alink.operator.batch.source.MySqlSourceBatchOp;

public class MySqlSourceReaderWrite {

    public static void main(String[] args) throws Exception {
        // 从 mysql 表中读取数据。
        MySqlSourceBatchOp data = new MySqlSourceBatchOp()
            .setDbName("fin_quant")
            .setUsername("root")
            .setPassword("1")
            .setIp("localhost")
            .setPort("33065")
            .setInputTableName("grade");
        data.print();

        // 将数据 sink 到 mysql，表必须是不存在的。
        MySqlSinkBatchOp sinkBatchOp = new MySqlSinkBatchOp()
            .setDbName("fin_quant")
            .setUsername("root")
            .setPassword("1")
            .setIp("localhost")
            .setPort("33065")
            .setOutputTableName("grade2");
        sinkBatchOp.linkFrom(data);
        BatchOperator.execute();
    }
}
