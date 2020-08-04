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

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.classification.LogisticRegression;
import com.alibaba.alink.pipeline.classification.OneVsRest;
import com.heqingsong.utils.FileReadUtils;

public class IrisMultiClassifyLogisticRegressionTest {

    private static final String[] FETCHURE_COLS = new String[]
        {"sepal_length", "sepal_width", "petal_length", "petal_width"};
    private static final String LABEL_COL = "category";
    private static final String PRED_COL = "pred_result";

    private static LogisticRegression lr = new LogisticRegression()
        .setLabelCol(LABEL_COL)
        .setFeatureCols(FETCHURE_COLS)
        .setPredictionCol(PRED_COL);

    private static OneVsRest oneVsRest = new OneVsRest()
        .setClassifier(lr).setNumClass(3)
        .setPredictionCol(PRED_COL);

    private static CsvSourceBatchOp data = new CsvSourceBatchOp()
        .setFilePath(FileReadUtils.getResourceFilePath("iris2.data"))
        .setFieldDelimiter(",")
        .setSchemaStr("sepal_length double, sepal_width double, petal_length double, petal_width double, category string");


    public static void main(String[] args) throws Exception {
        Pipeline pl = new Pipeline().add(oneVsRest);
        PipelineModel model = pl.fit(data);
        BatchOperator transform = model.transform(data);
        transform.print();
    }
}
