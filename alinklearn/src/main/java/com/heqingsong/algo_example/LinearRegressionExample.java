/**
 * File: LinearRegressionExample 　　2020/07/16 19:08
 * <p>
 * Copyright (c) 2018-2028  HeQingsong(ahheqingsong@126.com) All rights reserved.
 * <p>
 * //TODO
 *
 * @version 1.0
 * @author HeQingsong
 * @since JDK1.8
 */
package com.heqingsong.algo_example;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.source.MemSourceStreamOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.regression.LinearRegression;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.List;

public class LinearRegressionExample {
    static Row[] vecrows = new Row[]{
        Row.of("$3$0:1.0 1:7.0 2:9.0", "1.0 7.0 9.0", 1.0, 7.0, 9.0, 16.8),
        Row.of("$3$0:1.0 1:3.0 2:3.0", "1.0 3.0 3.0", 1.0, 3.0, 3.0, 6.7),
        Row.of("$3$0:1.0 1:2.0 2:4.0", "1.0 2.0 4.0", 1.0, 2.0, 4.0, 6.9),
        Row.of("$3$0:1.0 1:3.0 2:4.0", "1.0 3.0 4.0", 1.0, 3.0, 4.0, 8.0)
    };
    static String[] veccolNames = new String[]{"svec", "vec", "f0", "f1", "f2", "label"};
    static BatchOperator vecdata = new MemSourceBatchOp(Arrays.asList(vecrows), veccolNames);
    static StreamOperator svecdata = new MemSourceStreamOp(Arrays.asList(vecrows), veccolNames);

    public static void main(String[] args) throws Exception {
        String[] xVars = new String[]{"f0", "f1", "f2"};
        String yVar = "label";
        String vec = "vec";
        String svec = "svec";
        LinearRegression linear = new LinearRegression()
            .setLabelCol(yVar)  // 这里把变量都设置好了，后续会用到
            .setFeatureCols(xVars)
            .setPredictionCol("linpred");
        Pipeline pl = new Pipeline().add(linear);
        PipelineModel model = pl.fit(vecdata);
        BatchOperator result = model.transform(vecdata).select(
            new String[]{"label", "linpred"});
        result.print();
    }
}
