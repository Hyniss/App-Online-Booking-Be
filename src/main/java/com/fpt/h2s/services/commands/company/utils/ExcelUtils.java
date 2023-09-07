package com.fpt.h2s.services.commands.company.utils;

import com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserFailedResponse;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
public class ExcelUtils {

    public static final String URL = "https://h2s-s3.s3.ap-northeast-1.amazonaws.com/b0fec14bb02b4b8f_20230711101111551%2B0000.png";

    public static byte[] exportToExcel(final List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Failed Responses");
        createHeader(sheet);
        // Create data rows
        int rowIndex = 6;
        CellStyle dataCellStyle = sheet.getWorkbook().createCellStyle();
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        rowIndex = setFailedData(failedResponses, sheet, rowIndex, dataCellStyle);
        setBorder(sheet, --rowIndex);
        insertInstructorText(workbook, sheet);
        insertImage(workbook, sheet);
        //set page break preview mode
        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setFitHeight((short)0);
        printSetup.setFitWidth((short)1);
        XSSFSheet xssfSheet = (XSSFSheet) sheet;
        xssfSheet.lockSelectLockedCells(true);
        xssfSheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).setView(org.openxmlformats.schemas.spreadsheetml.x2006.main.STSheetViewType.PAGE_BREAK_PREVIEW);
        //Convert workbook to byte array
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export workbook as byte array.", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static int setFailedData(List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses, Sheet sheet, int rowIndex, CellStyle dataCellStyle) {
        for (BusinessAdminUploadBusinessUserFailedResponse response : failedResponses) {
            Row dataRow = sheet.createRow(rowIndex++);
            //Set row value
            dataRow.createCell(0).setCellValue(response.getEmail());
            dataRow.createCell(1).setCellValue(response.getRegion());
            dataRow.createCell(2).setCellValue(response.getPhone());
            dataRow.createCell(3).setCellValue(response.getUsername());
            createSystemRoleCell(dataRow, response.getRole(), new String[]{"BUSINESS_ADMIN", "BUSINESS_MEMBER"});
            // Set row height and apply cell style to each cell
            dataRow.setHeightInPoints(25);
            for (int columnIndex = 0; columnIndex <= 4; columnIndex++) {
                sheet.autoSizeColumn(columnIndex, true);
                int columnWidth = sheet.getColumnWidth(columnIndex);
                sheet.setColumnWidth(columnIndex, columnWidth + 10 * 312);
                dataCellStyle.setFillBackgroundColor(IndexedColors.WHITE.index);
                Cell cell = dataRow.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                dataCellStyle.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("@"));
                cell.setCellStyle(dataCellStyle);
            }
        }
        return rowIndex;
    }
    
    private static void createSystemRoleCell(Row row, String selectedValue, String[] roles) {
        Cell cell = row.createCell(4);
        // Remove data validation from the cell (if any)
        DataValidationHelper validationHelper = row.getSheet().getDataValidationHelper();
        DataValidationConstraint dvConstraint = validationHelper.createExplicitListConstraint(roles);
        CellRangeAddressList addressList = new CellRangeAddressList(row.getRowNum(), row.getRowNum(), 4, 4);
        DataValidation validation = validationHelper.createValidation(dvConstraint, addressList);
        validation.setShowErrorBox(true);
        row.getSheet().addValidationData(validation);
        cell.setCellValue(selectedValue);
    }

    private static void insertInstructorText(final Workbook workbook, final Sheet sheet) {
        int startRow = 0;
        int startColumn = 1;
        // Create a cell style with wrapped text and centered alignment
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);
        mergeCell(sheet, startColumn, 4);
        Row instructorRow = sheet.createRow(startRow);
        Cell instructorCell = instructorRow.createCell(startColumn);
        instructorCell.setCellValue(getInstructorText());
        instructorCell.setCellStyle(cellStyle);

    }

    private static void mergeCell(final Sheet sheet, final Integer firstCol, final Integer lastCol) {
        CellRangeAddress mergedRegion = new CellRangeAddress(0, 4, firstCol, lastCol);
        sheet.addMergedRegion(mergedRegion);
        //set height
        for (int rowNumber = 0; rowNumber <= 4; rowNumber++) {
            Row row = sheet.getRow(rowNumber);
            if (row == null) {
                row = sheet.createRow(rowNumber);
            }
            row.setHeightInPoints(30);
        }
        sheet.createFreezePane(5, 6);
    }

    private static void createHeader(final Sheet sheet) {
        Row headerRow = sheet.createRow(5);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerRow.setHeightInPoints(30);

        for (int columnIndex = 0; columnIndex <= 4; columnIndex++) {
            Cell headerCell = headerRow.createCell(columnIndex);
            // Set header cell value
            headerCell.setCellValue(getHeaderCellValue(columnIndex));
            //set font bold
            Font headerFont = sheet.getWorkbook().createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            // Set line thin
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            //Set alignment
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            //Set cell foreground color
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // Apply header cell style
            headerCell.setCellStyle(headerStyle);
        }
    }

    private static void setBorder(final Sheet sheet,
                                  final Integer rowIndex) {
        CellRangeAddress firstVerticalRegion = new CellRangeAddress(5, rowIndex, 0, 0);
        RegionUtil.setBorderLeft(BorderStyle.THIN, firstVerticalRegion, sheet);
        CellRangeAddress lastVerticalRegion = new CellRangeAddress(5, rowIndex, 4, 4);
        RegionUtil.setBorderRight(BorderStyle.THIN, lastVerticalRegion, sheet);
        CellRangeAddress lastHorizontalRegion = new CellRangeAddress(rowIndex, rowIndex, 0, 4);
        RegionUtil.setBorderBottom(BorderStyle.THIN, lastHorizontalRegion, sheet);
    }

    private static void insertImage(final Workbook workbook,
                                    final Sheet sheet) {
        mergeCell(sheet, 0, 0);
        try (InputStream imageStream = new URL(URL).openStream()) {
            byte[] imageBytes = IOUtils.toByteArray(imageStream);
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
            CreationHelper helper = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(1);
            anchor.setRow2(4);
            drawing.createPicture(anchor, pictureIdx);
        } catch (IOException e) {
            throw new RuntimeException("Failed to insert image into Excel sheet.", e);
        }
    }
    private static String getInstructorText() {
        return """
                Hướng dẫn điền thông tin vào file:
                
                Vui lòng điền toàn bộ thông tin vào trong file trước khi đưa lên hệ thống.
                Email: Vui lòng không để trống Email, không nhập Email không hợp lệ, không nhập trùng Email. Ví dụ Email hợp lệ: home2stayvn@gmail.com.
                Region: Vui lòng không để trống mã vùng điện thoại, không nhập mã vùng điện thoại không hợp lệ. Ví dụ với mã vùng điện thoại Việt Nam hợp lệ: 84.
                Phone: Vui lòng không để trống số điện thoại, không nhập số điện thoại không hơp lệ, không nhập trùng số điện thoại.
                Username: Vui lòng không để trống tên của người dùng, không để tên người dùng quá 32 ký tự.
                System role: Vui lòng không để trống quyền của người dùng trong hệ thống. Hiện nay chúng tôi mới cung cấp hai quyền là: BUSINESS_ADMIN và BUSINESS_MEMBER.
                """;
    }

    private static String getHeaderCellValue(final Integer columnIndex) {
        return switch (columnIndex) {
            case 0 -> "Email";
            case 1 -> "Region";
            case 2 -> "Phone";
            case 3 -> "Username";
            case 4 -> "System role";
            default -> "";
        };
    }


    public static List<String> getColumnNames(final Sheet sheet) {
        Row headerRow = sheet.getRow(5);
        if (headerRow == null || headerRow.getRowNum() != 5) {
            throw new IllegalArgumentException("Xin vui lòng kiểm tra lại thông tin và sử dụng đúng file mà chúng tôi cung cấp.");
        }
        return StreamSupport.stream(headerRow.spliterator(), false)
                .map(Cell::getStringCellValue)
                .collect(Collectors.toList());
    }

}
