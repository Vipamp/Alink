/**
 * File: JsonToVectorStreamOpTest 　　2020/07/29 18:34
 * <p>
 * Copyright (c) 2018-2028  HeQingsong(ahheqingsong@126.com) All rights reserved.
 * <p>
 *
 * @version 1.0
 * @author HeQingsong
 * @since JDK1.8
 */
package com.heqingsong.datapresolve;

import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.JsonValueStreamOp;
import com.alibaba.alink.operator.stream.source.TextSourceStreamOp;
import com.heqingsong.utils.FileReadUtils;
import org.junit.Test;


public class JsonValueSplitColumesTest {

    /**
     * 该测试，用于将json 字符串，列式转换成 Table 的格式。
     *
     * @author HeQingsong
     */
    @Test
    public void test() throws Exception {

        // 读取 txt 文件，该文件中各行，存放格式相同的 json 字符串，就一个 context 固定列名
        TextSourceStreamOp textSourceBatchOp = new TextSourceStreamOp()
            .setFilePath(FileReadUtils.getResourceFilePath("json_format.txt"))
            .setTextCol("context");

        // 从 json 字符串提取指定字段，转换成 table 格式。
        StreamOperator result = textSourceBatchOp.link(new JsonValueStreamOp()
            .setSkipFailed(false)
            .setSelectedCol("context")
            .setOutputCols(new String[]{"name", "age"})
            .setJsonPath(new String[]{"$.name", "$.age"}))
            .select("name,age");
        result.print();
        StreamOperator.execute();
    }
}
