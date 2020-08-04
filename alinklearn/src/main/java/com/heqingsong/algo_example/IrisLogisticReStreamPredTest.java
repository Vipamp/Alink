/**
 * File: IrisLinearRegressionTest 　　2020/07/29 17:16
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

import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.JsonValueStreamOp;
import com.alibaba.alink.operator.stream.source.Kafka011SourceStreamOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.classification.LogisticRegression;
import com.heqingsong.utils.FileReadUtils;

public class IrisLogisticReStreamPredTest {

    private static final String[] FETCHURE_COLS = new String[]
        {"sepal_length", "sepal_width", "petal_length", "petal_width"};
    private static final String LABEL_COL = "category";
    private static final String PRED_COL = "pred_result";

    private static LogisticRegression lr = new LogisticRegression()
        .setLabelCol(LABEL_COL)
        .setFeatureCols(FETCHURE_COLS)
        .setPredictionCol(PRED_COL);

    private static CsvSourceBatchOp data = new CsvSourceBatchOp()
        .setFilePath(FileReadUtils.getResourceFilePath("iris.data"))
        .setFieldDelimiter(",")
        .setSchemaStr("sepal_length double, sepal_width double, petal_length double, petal_width double, category string");

    private static Kafka011SourceStreamOp source = new Kafka011SourceStreamOp()
        .setBootstrapServers("172.20.3.225:9092")
        .setTopic("test")
        .setStartupMode("LATEST")
        .setGroupId("alink_group");

    /**
     * 实时基于csv 文件训练模型，再从 kakfa 中获取待预测数据，接入模型预测鸢尾花结果。
     * <p>
     * kafka 数据输入格式：
     * {"sepal_length":6.0,"sepal_width":3.4,"petal_length":4.5,"petal_width":1.6}
     *
     * @author HeQingsong
     */
    public static void main(String[] args) throws Exception {
        StreamOperator message = source.link(new JsonValueStreamOp()
            .setSkipFailed(true)
            .setSelectedCol("message")
            .setOutputCols(new String[]{"sepal_length", "sepal_width", "petal_length", "petal_width"})
            .setJsonPath(new String[]{"$.sepal_length", "$.sepal_width", "$.petal_length", "$.petal_width"}))
            .select("CAST(sepal_length AS DOUBLE) AS sepal_length,"
                + "CAST(sepal_width AS DOUBLE) AS sepal_width,"
                + "CAST(petal_length AS DOUBLE) AS petal_length,"
                + "CAST(petal_width AS DOUBLE) AS petal_width");
        StreamOperator result = new Pipeline().add(lr).fit(data).transform(message);
        result.print();
        StreamOperator.execute();
    }
}
