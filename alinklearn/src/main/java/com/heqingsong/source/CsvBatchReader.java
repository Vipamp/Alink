/**
 * File: CsvBatchReader 　　2020/07/14 13:12
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

import com.alibaba.alink.operator.batch.source.CsvSourceBatchOp;
import com.heqingsong.utils.FileReadUtils;

public class CsvBatchReader {

    public static void main(String[] args) throws Exception {
        // 从文件读取数据
        CsvSourceBatchOp csvSourceBatchOp = new CsvSourceBatchOp()
            .setFilePath(FileReadUtils.getResourceFilePath("iris.data"))
            .setFieldDelimiter(",")
            .setSchemaStr("sepal_length double, sepal_width double, petal_length double, petal_width double, category string");
        csvSourceBatchOp.firstN(10).print();

        // 从网络读取数据
        csvSourceBatchOp = new CsvSourceBatchOp()
            .setFilePath("http://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data")
            .setSchemaStr("sepal_length double, sepal_width double, petal_length double, petal_width double, category string");
        csvSourceBatchOp.firstN(5).print();
    }
}
