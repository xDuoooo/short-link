package com.xduo.shortlink.project.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcelExportUtil {
    /**
     * 通用Excel导出方法
     * @param response HttpServletResponse
     * @param fileName 文件名（不含.xlsx）
     * @param dtoClass 导出DTO类.class
     * @param dataList 数据集合
     */
    public static void export(HttpServletResponse response, String fileName, Class<?> dtoClass, List<?> dataList) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodeFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodeFileName + ".xlsx");
        ExcelWriterBuilder writer = EasyExcel.write(response.getOutputStream(), dtoClass);
        writer.sheet("sheet1").doWrite(dataList);
    }
    /**
     * 通用Excel导出方法-支持OutputStream
     * @param out 输出流
     * @param sheetName Excel sheet名
     * @param dtoClass  导出DTO类.class
     * @param dataList  数据集合
     */
    public static void export(OutputStream out, String sheetName, Class<?> dtoClass, List<?> dataList) throws IOException {
        Set<String> excludeColumnFieldNames = new HashSet<>();
        excludeColumnFieldNames.add("uvType");
        ExcelWriterBuilder writer = EasyExcel.write(out, dtoClass);
        writer.sheet(sheetName).excludeColumnFieldNames(excludeColumnFieldNames).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).doWrite(dataList);
    }
}
