/**
 * File: LinearRegressionTrain 　　2020/07/28 17:09
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
import org.apache.flink.util.FileUtils;

import java.io.File;
import java.util.Arrays;

public class LinearRegressionTest {
    static final String MODEL_PATH = "/tmp/alink";
    static final String MODEL_FILENAME = MODEL_PATH + "/linear-regression.csv";

    static Row[] vecrows = new Row[]{
        Row.of(1.0, 7.0, 9.0, 16.8),
        Row.of(1.0, 3.0, 3.0, 6.7),
        Row.of(1.0, 2.0, 4.0, 6.9),
        Row.of(1.0, 3.0, 4.0, 8.0)
    };
    static String[] veccolNames = new String[]{"f0", "f1", "f2", "label"};
    static BatchOperator vecdata = new MemSourceBatchOp(Arrays.asList(vecrows), veccolNames);
    static StreamOperator svecdata = new MemSourceStreamOp(Arrays.asList(vecrows), veccolNames);

    static String[] xVars = new String[]{"f0", "f1", "f2"};
    static String yVar = "label";
    static LinearRegression linear = new LinearRegression()
        .setLabelCol(yVar)
        .setFeatureCols(xVars)
        .setPredictionCol("linpred");

    /**
     * 测试模型训练，并将模型结果 pipemodel 导成文件。
     * 文件如果只是单纯训练，没有预测，必须要加上 BatchOperator.execute() 启动任务，否则不执行。
     *
     * @author HeQingsong
     */
    public static void init() throws Exception {
        Pipeline pl = new Pipeline().add(linear);
        PipelineModel pipelineModel = pl.fit(vecdata);
        pipelineModel.save(MODEL_FILENAME);
        BatchOperator.execute();
    }

    /**
     * 测试结束后，删除模型文件
     *
     * @author HeQingsong
     */
    public static void clear() throws Exception {
        FileUtils.deleteDirectory(new File(MODEL_PATH));
    }

    /**
     * 线性回归的批预测，其结果 BatchOperator，直接 print()，既可以触发其执行。
     *
     * @author HeQingsong
     */
    public static void batchPredict() throws Exception {
        init();
        PipelineModel model = PipelineModel.load(MODEL_FILENAME);
        BatchOperator result = model.transform(vecdata).select(
            new String[]{"label", "linpred"});
        result.print();
        clear();
    }

    /**
     * 线性回归的流预测，必须要调用 StreamOperator 的 execute 方法，否则不执行
     * 任务提交执行的逻辑和Flink 的批/流 的任务提交一样。
     *
     * @author HeQingsong
     */
    public static void streamPredict() throws Exception {
        init();
        PipelineModel model = PipelineModel.load(MODEL_FILENAME);
        StreamOperator result = model.transform(svecdata).select(
            new String[]{"label", "linpred"});
        result.print();
        StreamOperator.execute();
        clear();
    }

    public static void main(String[] args) throws Exception {
        batchPredict();
        streamPredict();
    }
}
