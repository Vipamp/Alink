/**
 * File: FileReadUtils 　　2020/07/14 13:26
 * <p>
 * Copyright (c) 2018-2028  HeQingsong(ahheqingsong@126.com) All rights reserved.
 * <p>
 * //TODO
 *
 * @version 1.0
 * @author HeQingsong
 * @since JDK1.8
 */
package com.heqingsong.utils;

public class FileReadUtils {

    /**
     * 输入 resource 下的文件名称，获取该文件的 绝对路径 + 文件名称。
     *
     * @param filename filename
     * @return java.lang.String
     * @author HeQingsong
     */
    public static String getResourceFilePath(String filename) {
        return FileReadUtils.class.getClassLoader().getResource(filename).getPath();
    }
}
