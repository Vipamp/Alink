/**
 * File: MyElementSourceOp 　　2020/07/29 18:12
 * <p>
 * Copyright (c) 2018-2028  HeQingsong(ahheqingsong@126.com) All rights reserved.
 * <p>
 * 用于测试自己构造 BatchOperator 和 StreamOperator.
 *
 * @version 1.0
 * @author HeQingsong
 * @since JDK1.8
 */
package com.heqingsong.source;

import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.source.MemSourceStreamOp;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.Arrays;

public class MyElementSourceOp {

    Row[] rows = new Row[]{
        Row.of(1, 5.1, 3.5, 1.4, 0.2, "setosa"),
        Row.of(2, 4.9, 3.0, 1.4, 0.2, "setosa"),
        Row.of(3, 4.7, 3.2, 1.3, 0.2, "setosa"),
        Row.of(4, 4.6, 3.1, 1.5, 0.2, "setosa"),
        Row.of(5, 5.0, 3.6, 1.4, 0.2, "setosa")
    };

    private TableSchema schema = new TableSchema(
        new String[]{"id", "sLength", "sWidth", "pLength", "pWidth", "Species"},
        new TypeInformation<?>[]{Types.INT, Types.DOUBLE, Types.DOUBLE, Types.DOUBLE, Types.DOUBLE, Types.STRING}
    );

    private String[] colNames = new String[]{"id", "sLength", "sWidth", "pLength", "pWidth", "Species"};

    /**
     * 测试两种方式，将自己定义的数据，构造成 BatchOperator
     * 1. 直接给数据执行列名
     * 2. 该数据指定列名和数据类型。
     *
     * @author HeQingsong
     */
    @Test
    public void testBatchOp() throws Exception {
        new MemSourceBatchOp(Arrays.asList(rows), colNames).print();
        new MemSourceBatchOp(Arrays.asList(rows), schema).print();
    }

    /**
     * 测试两种方式，将自己定义的数据，构造成 StreamOperator
     * 1. 直接给数据执行列名
     * 2. 该数据指定列名和数据类型。
     * <p>
     * 备注：Flink 的流任务，print() 函数不会触发任务提交和执行，必须要执行
     * StreamOperator.execute() 方法，批任务，直接 print() 可以触发任务提交。
     *
     * @author HeQingsong
     */
    @Test
    public void testStreamOp() throws Exception {
        new MemSourceStreamOp(Arrays.asList(rows), colNames).print();
        new MemSourceStreamOp(Arrays.asList(rows), schema).print();
        StreamOperator.execute();
    }
}
