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


public class JsonValueSplitColumesTest {

    /**
     * 该测试，用于将json 字符串，列式转换成 Table 的格式，支持 json 嵌套。
     *
     * @author HeQingsong
     */
    public static void main(String[] args) throws Exception {
        // 读取 txt 文件，该文件中各行，存放格式相同的 json 字符串，就一个 context 固定列名
        TextSourceStreamOp textSourceBatchOp = new TextSourceStreamOp()
            .setFilePath(FileReadUtils.getResourceFilePath("json_format.txt"))
            .setTextCol("context");

        // 从 json 字符串提取指定字段，转换成 Table 格式，并使用 Table API 的select 语法，转换字段类型。
        StreamOperator result = textSourceBatchOp.link(new JsonValueStreamOp()
            .setSkipFailed(false)
            .setSelectedCol("context")
            .setOutputCols(new String[]{"name", "age", "addr"})
            .setJsonPath(new String[]{"$.name", "$.age", "$.info.addr"}))
            .select("CAST(name AS STRING) AS name, "
                + "CAST(age AS INTEGER) AS age,"
                + "CAST(addr AS STRING) AS addr");
        result.getOutputTable().printSchema();
        result.print();
        StreamOperator.execute();
    }
}
