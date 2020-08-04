/**
 * File: VectorAssemblerTest 　　2020/07/29 14:52
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

import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.pipeline.dataproc.vector.VectorAssembler;
import org.apache.flink.types.Row;

import java.util.Arrays;

/**
 * VectorAssembler 将多个列合并成一个列。
 *
 * @author HeQingsong
 */
public class VectorAssemblerTest {

    static Row[] testArray =
        new Row[]{
            Row.of("1", "2", "3", "4"),
            Row.of("1", "2", "3", "4"),
            Row.of("1", "2", "3", "4"),
            Row.of("1", "2", "3", "4")
        };
    static String[] inputCols = new String[]{"c1", "c2", "c3", "c4"};
    static String[] fectureCols = new String[]{"c2", "c3", "c4"};
    static String outputCol = "features";

    static MemSourceBatchOp source = new MemSourceBatchOp(Arrays.asList(testArray), inputCols);

    public static void main(String[] args) throws Exception {
        VectorAssembler va = new VectorAssembler()
            .setSelectedCols(fectureCols)
            .setOutputCol(outputCol);
        va.transform(source).print();
    }
}
