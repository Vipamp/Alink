/**
 * File: OneHotEncodeTest 　　2020/07/30 17:32
 * <p>
 * Copyright (c) 2018-2028  HeQingsong(ahheqingsong@126.com) All rights reserved.
 * <p>
 * //TODO
 *
 * @version 1.0
 * @author HeQingsong
 * @since JDK1.8
 */
package com.heqingsong.datapresolve;

import com.alibaba.alink.operator.batch.feature.OneHotTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.apache.flink.types.Row;

import java.util.Arrays;

public class OneHotEncodeTest {

    private static Row[] testArray =
        new Row[]{
            Row.of("String1", "String1"),
            Row.of("String1", "String2"),
            Row.of("String2", "String5"),
            Row.of("String2", "String7"),
            Row.of("String1", "String1"),
            Row.of("String3", "String4")
        };
    private static String[] inputCols = new String[]{"context1", "context2"};

    private static MemSourceBatchOp source = new MemSourceBatchOp(Arrays.asList(testArray), inputCols);

    public static void main(String[] args) throws Exception {
        OneHotTrainBatchOp context = source.link(new OneHotTrainBatchOp()
            .setSelectedCols("context1", "context2"));
        context.print();
    }
}
