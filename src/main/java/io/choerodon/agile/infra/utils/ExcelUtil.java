package io.choerodon.agile.infra.utils;

import io.choerodon.agile.app.domain.Predefined;
import io.choerodon.core.exception.CommonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author dinghuang123@gmail.com
 * @since 2018/8/17
 */
public class ExcelUtil {

    public enum Mode {
        SXSSF("SXSSF"), HSSF("HSSF"), XSSF("XSSF");
        private String value;

        Mode(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static class GuideSheet {

        private int rowNum;

        private String fieldName;

        private String requestStr;

        private Boolean hasStyle;

        public GuideSheet(int rowNum, String fieldName, String requestStr, Boolean hasStyle) {
            this.rowNum = rowNum;
            this.fieldName = fieldName;
            this.requestStr = requestStr;
            this.hasStyle = hasStyle;
        }

        public int rowNum() {
            return this.rowNum;
        }

        public String fieldName() {
            return this.fieldName;
        }

        public String requestStr() {
            return this.requestStr;
        }

        public Boolean hasStyle() {
            return this.hasStyle;
        }

    }

    private ExcelUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtil.class);
    private static final String EXCEPTION = "Exception:{}";
    private static final String ERROR_IO_WORKBOOK_WRITE_OUTPUTSTREAM = "error.io.workbook.write.output.stream";

    private static void initGuideSheetByRow(Workbook workbook, Sheet sheet, int rowNum, String fieldName, String requestStr, Boolean hasStyle) {
        CellStyle ztStyle = workbook.createCellStyle();
        Font ztFont = workbook.createFont();
        ztFont.setColor(Font.COLOR_RED);
        ztStyle.setFont(ztFont);
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(fieldName);
        Cell cell = row.createCell(1);
        cell.setCellValue(requestStr);
        if (hasStyle) {
            cell.setCellStyle(ztStyle);
        }
    }

    private static void initGuideSheetRemind(Workbook workbook, Sheet sheet, String remindInfo) {
        CellStyle ztStyle = workbook.createCellStyle();
        Font ztFont = workbook.createFont();
        ztFont.setColor(Font.COLOR_RED);
        ztStyle.setFont(ztFont);
        ztStyle.setAlignment(CellStyle.ALIGN_CENTER);
        ztStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        sheet.addMergedRegion(new CellRangeAddress(0, 9, 2, 4));
        Row row = sheet.getRow(0);
        Cell cell = row.createCell(2);
        cell.setCellValue(remindInfo);
        cell.setCellStyle(ztStyle);
    }

    public static List<GuideSheet> initGuideSheet() {
        GuideSheet[] guideSheets = {
                new GuideSheet(0, "问题类型", "必选项", true),
                new GuideSheet(1, "所属史诗", "非必选项，普通应用项目未加入项目群ART且问题类型为故事可选，否则不可选", true),
                new GuideSheet(2, "模块", "非必输项", false),
                new GuideSheet(3, "冲刺", "非必输项，任务/故事下的子任务冲刺默认和父级一致", false),
                new GuideSheet(4, "概要", "必输项，限制44个字符以内", true),
                new GuideSheet(5, "子任务概述", "非必输项，故事、任务类型下可创建子任务", false),
                new GuideSheet(6, "经办人", "非必选项", false),
                new GuideSheet(7, "优先级", "必选项", true),
                new GuideSheet(8, "预估时间", "非必输项，仅支持3位整数或者0.5，预估时间以小时为单位", false),
                new GuideSheet(9, "版本", "非必选项", false),
                new GuideSheet(10, "史诗名称", "如果问题类型选择史诗，此项必填, 限制10个字符", true),
                new GuideSheet(11, "故事点", "非必输，仅支持3位整数或者0.5，仅故事类型须填写，否则不生效", false),
                new GuideSheet(12, "描述", "非必输，仅支持填写纯文本", false),
        };
        return Arrays.asList(guideSheets);
    }

    public static void createGuideSheet(Workbook wb, List<GuideSheet> guideSheets, boolean withFeature) {
        if (withFeature) {
            GuideSheet guideSheet = new GuideSheet(1, "所属特性", "非必须项，普通应用项目加入项目群后且问题类型为故事可选，否则不可选", true);
            guideSheets.set(1, guideSheet);
        }
        Sheet sheet = wb.createSheet("要求");
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 17000);

        for (GuideSheet guideSheet : guideSheets) {
            initGuideSheetByRow(wb, sheet, guideSheet.rowNum(),
                    guideSheet.fieldName(), guideSheet.requestStr(), guideSheet.hasStyle());
        }

        sheet.setColumnWidth(2, 3000);
        initGuideSheetRemind(wb, sheet, "请至下一页，填写信息");

        initExample(wb, sheet, withFeature);
    }

    private static void initExample(Workbook wb, Sheet sheet, boolean withFeature) {
        sheet.setColumnWidth(4, 8000);
        sheet.setColumnWidth(5, 6000);
        sheet.setColumnWidth(8, 6000);
        sheet.setColumnWidth(10, 9000);
        sheet.setColumnWidth(12, 8000);

        Row row = sheet.createRow(17);
        row.createCell(0).setCellValue("示例：");

        CellStyle blueBackground = wb.createCellStyle();
        blueBackground.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        blueBackground.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle coralBackground = wb.createCellStyle();
        coralBackground.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        coralBackground.setFillPattern(CellStyle.SOLID_FOREGROUND);

        String[] data1 = {"问题类型*", "所属史诗", "模块", "冲刺", "概述*",
                "子任务概述(仅子任务生效)", "经办人", "优先级*", "预估时间(小时)",
                "版本", "史诗名称(仅问题类型为史诗时生效)", "故事点", "描述"};
        String secondColumnValue = "可以选择史诗";
        if (withFeature) {
            data1[1] = "所属特性";
            secondColumnValue = "可以选择特性";
        }
        int count = 19;
        createRow(sheet, count++, data1, blueBackground);

        String[] data2 = {"史诗", "", "敏捷管理", "", "请输入史诗的概述",
                "", "", "高", "", "", "导入问题", "", "请输入导入史诗类型的问题的描述信息"};
        createRow(sheet, count++, data2, null);

        String[] data3 = {"故事", secondColumnValue, "敏捷管理", "sprint-1", "这里输入故事的概述：故事1",
                "", "张三", "中", "8", "0.1", "", "2", "导入故事并且导入故事下的子任务"};
        createRow(sheet, count++, data3, coralBackground);

        String[] data4 = {"", "", "", "", "", "故事1的子任务1的概述", "李四", "高", "2", "", "", "", "请输入子任务1的描述信息"};
        createRow(sheet, count++, data4, coralBackground);

        String[] data5 = {"", "", "", "", "", "故事1的子任务2的概述", "王五", "中", "4", "", "", "", "请输入子任务2的描述信息"};
        createRow(sheet, count++, data5, coralBackground);

        String[] data6 = {"", "", "", "", "", "故事1的子任务3的概述……", "陈七", "低", "2", "", "", "", "请输入子任务3的描述信息"};
        createRow(sheet, count++, data6, coralBackground);

        String[] data7 = {"任务", secondColumnValue, "敏捷管理", "sprint-1", "请在此处输入任务的概述：任务1", "", "王五", "中", "5", "0.2", "", "", "请输入任务2的描述信息"};
        createRow(sheet, count++, data7, null);

        String[] data8 = {"", "", "", "", "", "任务1的子任务4的概述", "小六", "中", "2", "0.2", "", "", "请输入子任务4的描述信息"};
        createRow(sheet, count++, data8, null);

        String[] data9 = {"", "", "", "", "", "任务1的子任务5的概述", "初八", "中", "2", "0.2", "", "", "请输入子任务5的描述信息"};
        createRow(sheet, count++, data9, null);


        String[] data10 = {"故事", secondColumnValue, "敏捷管理", "sprint-1", "这里输入故事的概述：故事2", "", "张三", "中", "8", "0.1", "", "2", "仅导入故事"};
        createRow(sheet, count++, data10, coralBackground);

        String[] data11 = {"任务", secondColumnValue, "敏捷管理", "sprint-1", "请在此处输入任务的概述：任务2", "", "张三", "中", "8", "0.1", "", "", "请输入任务2的描述信息"};
        createRow(sheet, count++, data11, null);

        String[] data12 = {"缺陷", secondColumnValue, "敏捷管理", "sprint-1", "请在此处输入缺陷的概述：缺陷1", "", "李四", "低", "0.5", "0.1", "", "", "请输入缺陷2的描述信息"};
        createRow(sheet, count++, data12, coralBackground);
    }

    public static void createRow(Sheet sheet, int rowNum, String[] data, CellStyle background) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < data.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(data[i]);
            if (background != null) {
                cell.setCellStyle(background);
            }
        }
    }

    public static Workbook generateExcelAwesome(Workbook generateExcel,
                                                List<Integer> errorRows,
                                                Map<Integer, List<Integer>> errorMapList,
                                                String[] fieldsName,
                                                List<String> priorityList,
                                                List<String> issueTypeList,
                                                List<String> versionList,
                                                String sheetName,
                                                List<String> componentList,
                                                List<String> sprintList,
                                                List<String> users,
                                                Predefined theSecondColumnPredefined,
                                                boolean withFeature) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        // create guide sheet
        createGuideSheet(workbook, initGuideSheet(), withFeature);
        Sheet resultSheet = workbook.createSheet(sheetName);
        CellStyle style = CatalogExcelUtil.getHeadStyle(workbook);
        generateHeaders(resultSheet, style, Arrays.asList(fieldsName));

        List<Predefined> predefinedList = new ArrayList<>();
        predefinedList.add(new Predefined(priorityList, 1, 500, 7, 7, "hidden_priority", 2));
        predefinedList.add(new Predefined(issueTypeList, 1, 500, 0, 0, "hidden_issue_type", 3));
        predefinedList.add(new Predefined(versionList, 1, 500, 9, 9, "hidden_fix_version", 4));
        predefinedList.add(new Predefined(componentList, 1, 500, 2, 2, "hidden_component", 5));
        predefinedList.add(new Predefined(sprintList, 1, 500, 3, 3, "hidden_sprint", 6));
        predefinedList.add(new Predefined(users, 1, 500, 6, 6, "hidden_manager", 7));
        predefinedList.add(theSecondColumnPredefined);

        for (Predefined predefined : predefinedList) {
            workbook =
                    dropDownList2007(
                            workbook,
                            resultSheet,
                            predefined.values(),
                            predefined.startRow(),
                            predefined.endRow(),
                            predefined.startCol(),
                            predefined.endCol(),
                            predefined.hidden(),
                            predefined.hiddenSheetIndex());
        }

        Sheet sheet = generateExcel.getSheetAt(1);
        int size = sheet.getPhysicalNumberOfRows();
        XSSFCellStyle ztStyle = workbook.createCellStyle();
        Font ztFont = workbook.createFont();
        ztFont.setColor(Font.COLOR_RED);
        ztStyle.setFont(ztFont);
        int index = 1;
        for (int i = 1; i <= size; i++) {
            if (errorRows.contains(i)) {
                Row row = sheet.getRow(i);
                Row newRow = resultSheet.createRow(index++);
                for (int j = 0; j < fieldsName.length; j++) {
                    Cell cell = newRow.createCell(j);
                    if (row.getCell(j) != null) {
                        cell.setCellValue(row.getCell(j).toString());
                    }
                    if (errorMapList.get(i) != null) {
                        List<Integer> errList = errorMapList.get(i);
                        if (errList.contains(j)) {
                            cell.setCellStyle(ztStyle);
                        }
                    }
                }
            }
        }
        return workbook;
    }

    public static void generateHeaders(Sheet sheet, CellStyle style, List<String> headers) {
        Row row = sheet.createRow(0);
        int columnNum = headers.size();
        for (int i = 0; i < columnNum; i++) {
            int width = 3500;
            //子任务名称和史诗名称两列加宽
            if (i == 5 || i == 10) {
                width = 8000;
            }
            sheet.setColumnWidth(i, width);
        }

        for (int i = 0; i < headers.size(); i++) {
            CatalogExcelUtil.initCell(row.createCell(i), style, headers.get(i));
        }
    }


    public static <T> SXSSFWorkbook generateExcel(List<T> list, Class<T> clazz, String[] fieldsName, String[] fields, String sheetName) {
        //1、创建工作簿
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        if (list != null && !list.isEmpty()) {
            //1.3、列标题样式
            CellStyle style2 = createCellStyle(workbook, (short) 13, CellStyle.ALIGN_LEFT, true);
            //1.4、强制换行
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            //2、创建工作表
            SXSSFSheet sheet = workbook.createSheet(sheetName);
            //设置默认列宽
            sheet.setDefaultColumnWidth(13);
            SXSSFRow row2 = sheet.createRow(0);
            row2.setHeight((short) 260);
            for (int j = 0; j < list.size(); j++) {
                SXSSFRow row = sheet.createRow(j + 1);
                row.setHeight((short) 260);
                for (int i = 0; i < fieldsName.length; i++) {
                    //3.3设置列标题
                    SXSSFCell cell2 = row2.createCell(i);
                    //加载单元格样式
                    cell2.setCellStyle(style2);
                    cell2.setCellValue(fieldsName[i]);
                    //4、操作单元格；将数据写入excel
                    handleWriteCell(row, i, j, list, cellStyle, fields, clazz);
                }
            }
        }
        return workbook;
    }

    /**
     * 通过类导出
     */
    public static <T> void export(List<T> list, Class<T> clazz, String[] fieldsName, String[] fields, String sheetName, List<String> autoSizeColumn, HttpServletResponse response) {
        if (list != null && !list.isEmpty()) {
            //1、创建工作簿
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            //1.3、列标题样式
            CellStyle style2 = createCellStyle(workbook, (short) 13, CellStyle.ALIGN_LEFT, true);
            //1.4、强制换行
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            //2、创建工作表
            SXSSFSheet sheet = workbook.createSheet(sheetName);
            //设置默认列宽
            sheet.setDefaultColumnWidth(13);
            //创建标题列
            SXSSFRow row2 = sheet.createRow(0);
            row2.setHeight((short) 260);
            for (int i = 0; i < fieldsName.length; i++) {
                //3.3设置列标题
                SXSSFCell cell2 = row2.createCell(i);
                //加载单元格样式
                cell2.setCellStyle(style2);
                cell2.setCellValue(fieldsName[i]);
            }
            for (int j = 0; j < list.size(); j++) {
                SXSSFRow row = sheet.createRow(j + 1);
                row.setHeight((short) 260);
                for (int i = 0; i < fieldsName.length; i++) {
                    ;
                    //4、操作单元格；将数据写入excel
                    handleWriteCell(row, i, j, list, cellStyle, fields, clazz);
                }
            }
            sheet.trackAllColumnsForAutoSizing();
            for (int i = 0; i < fieldsName.length; i++) {
                //设置列宽度自适应
                if (autoSizeColumn.contains(fields[i])) {
                    sheet.autoSizeColumn(i);
                }
            }
            //5、输出
            try {
                String disposition = String.format("attachment;filename=\"%s-%s.xlsx\"", "Choerodon", System.currentTimeMillis());
                response.setContentType("application/vnd.ms-excel");
                response.setCharacterEncoding("utf-8");
                response.addHeader("Content-Disposition", disposition);
                workbook.write(response.getOutputStream());
            } catch (Exception e) {
                LOGGER.error(EXCEPTION, e);
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    LOGGER.error(EXCEPTION, e);
                }
            }
        }
    }

    private static <T> void handleWriteCell(SXSSFRow row, int i, int j, List<T> list, CellStyle cellStyle, String[] fields, Class<T> clazz) {
        SXSSFCell cell = row.createCell(i);
        cell.setCellStyle(cellStyle);
        if (list.get(j) != null) {
            Method method = null;
            try {
                method = clazz.getMethod(createGetter(fields[i]));
            } catch (NoSuchMethodException e) {
                LOGGER.debug(e.getMessage());
                try {
                    method = clazz.getMethod("getFoundationFieldValue");
                } catch (NoSuchMethodException e1) {
                    LOGGER.error(EXCEPTION, e1);
                }
            }
            Object invoke = new Object();
            try {
                invoke = method.invoke(list.get(j));
            } catch (InvocationTargetException | IllegalAccessException e) {
                LOGGER.error(EXCEPTION, e);
            }
            if (invoke instanceof Date) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                cell.setCellValue(formatter.format(invoke));
            } else if (invoke instanceof Map) {
                ObjectMapper m = new ObjectMapper();
                Map<String, String> foundationFieldValue = m.convertValue(invoke, Map.class);

                String str = foundationFieldValue.get(fields[i]) != null ? foundationFieldValue.get(fields[i]) : "";
                cell.setCellValue(str);
            } else {
                String str = invoke == null ? null : invoke.toString();
                cell.setCellValue(str);
            }
        } else {
            cell.setCellValue("");
        }

    }


    /**
     * 创建单元格样式
     *
     * @param workbook 工作簿
     * @param fontSize 字体大小
     * @return 单元格样式
     */
    private static CellStyle createCellStyle(SXSSFWorkbook workbook, short fontSize, short aligment, Boolean bold) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(aligment);
        //垂直居中
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        if (bold) {
            //加粗字体
            font.setBoldweight(org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
        }
        font.setFontHeightInPoints(fontSize);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 通过属性名称拼凑getter方法
     *
     * @param fieldName fieldName
     * @return String
     */
    private static String createGetter(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) {
            return null;
        }
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static Workbook getWorkbookFromMultipartFile(Mode mode, MultipartFile excelFile) {
        try {
            switch (mode) {
                case HSSF:
                    return new HSSFWorkbook(excelFile.getInputStream());
                case XSSF:
                    return new XSSFWorkbook(excelFile.getInputStream());
                default:
                    return null;
            }
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
    }


    public static byte[] getBytes(Workbook workbook) {
        try (ByteArrayOutputStream workbookOutputStream = new ByteArrayOutputStream()) {
            workbook.write(workbookOutputStream);
            return workbookOutputStream.toByteArray();
        } catch (IOException e) {
            throw new CommonException(ERROR_IO_WORKBOOK_WRITE_OUTPUTSTREAM, e);
        }
    }

    /**
     * @param wb               HSSFWorkbook对象
     * @param realSheet        需要操作的sheet对象
     * @param datas            下拉的列表数据
     * @param startRow         开始行
     * @param endRow           结束行
     * @param startCol         开始列
     * @param endCol           结束列
     * @param hiddenSheetName  隐藏的sheet名
     * @param hiddenSheetIndex 隐藏的sheet索引
     * @return
     * @throws Exception
     */
    public static XSSFWorkbook dropDownList2007(Workbook wb, Sheet realSheet, List<String> datas, int startRow, int endRow,
                                                int startCol, int endCol, String hiddenSheetName, int hiddenSheetIndex) {

        XSSFWorkbook workbook = (XSSFWorkbook) wb;
        // 创建一个数据源sheet
        XSSFSheet hidden = workbook.createSheet(hiddenSheetName);
        // 数据源sheet页不显示
        workbook.setSheetHidden(hiddenSheetIndex, true);
        if (datas == null || datas.isEmpty()) {
            return workbook;
        }
        // 将下拉列表的数据放在数据源sheet上
        XSSFRow row = null;
        XSSFCell cell = null;
        for (int i = 0; i < datas.size(); i++) {
            row = hidden.createRow(i);
            cell = row.createCell(0);
            cell.setCellValue(datas.get(i));
        }
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) realSheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(hiddenSheetName + "!$A$1:$A" + datas.size());
        CellRangeAddressList addressList = null;
        XSSFDataValidation validation = null;
        // 单元格样式
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 循环指定单元格下拉数据
        for (int i = startRow; i <= endRow; i++) {
            row = (XSSFRow) realSheet.createRow(i);
            cell = row.createCell(startCol);
            cell.setCellStyle(style);
            addressList = new CellRangeAddressList(i, i, startCol, endCol);
            validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
            realSheet.addValidationData(validation);
        }

        return workbook;
    }
}
