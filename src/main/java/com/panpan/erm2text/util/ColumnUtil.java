package com.panpan.erm2text.util;

public class ColumnUtil {

    /**
     * 将数据类型转换为容易阅读的形式
     *
     * @param columnType
     * @param length
     * @param scale
     * @return
     */
    public static String getColumnType(String columnType, int length, int scale){
            if (length > 0){
                if (scale > 0)
                {
                    return String.format("%s(%d,%d)", columnType, length, scale);
                }
                else
                {
                    return String.format("%s(%d)", columnType, length);
                }
            } else{
                return String.format("%s", columnType);
            }
    }
}
