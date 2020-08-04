/**
 * File: TextReaderWriter 　　2020/08/03 15:34
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
import com.alibaba.alink.operator.batch.sink.TextSinkBatchOp;
import com.alibaba.alink.operator.batch.source.TextSourceBatchOp;
import com.heqingsong.utils.FileReadUtils;

public class TextBatchReaderWriter {

    public static void main(String[] args) throws Exception {
        TextSourceBatchOp textSourceBatchOp = new TextSourceBatchOp()
            .setFilePath(FileReadUtils.getResourceFilePath("json_format.txt"))
            .setTextCol("context");
        textSourceBatchOp.print();

        TextSinkBatchOp textSinkBatchOp = new TextSinkBatchOp()
            .setFilePath(FileReadUtils.getResourcePath() + "json_format2.txt")
            .setOverwriteSink(true);

        textSinkBatchOp.linkFrom(textSourceBatchOp);
        BatchOperator.execute();
    }
}
