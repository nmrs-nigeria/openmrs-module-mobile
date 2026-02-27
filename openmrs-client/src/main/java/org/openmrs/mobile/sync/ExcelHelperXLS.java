package org.openmrs.mobile.sync;

import static org.openmrs.mobile.application.OpenMRSCustomHandler.createFolder;

import androidx.annotation.NonNull;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to handle Excel operations for PatientLog objects.
 */
public class ExcelHelperXLS {
    private HSSFWorkbook workbook;
    private HSSFSheet successSheet;
    private HSSFSheet failedSheet;

    public String getFilePath() {
        return filePath;
    }

    private final String filePath;


    private final String EXCEL_LOG = "line_list";


    /**
     * Constructor to open or create the workbook and sheets.
     *
     * @param pattern Time name format yyyyMMddHHmmss. File Path of the Excel file to create or update will generated with pattern.
     * @throws IOException If an I/O error occurs.
     */
    public ExcelHelperXLS(@NonNull String pattern) throws IOException {
        this.filePath = getFileName(pattern) + ".xls";
    }

    /**
     * Get file in xml folder without creating the file.
     *
     * @param pattern Time name format yyyyMMddHHmmss.
     * @return A String, Absolute Path for a file.
     */
    private String getFileName(String pattern) {
        createFolder();
        String fileNaming = new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date());
        File file = new File(createFolder(), EXCEL_LOG + fileNaming + ".xls");
        return file.getAbsolutePath();
    }

    public boolean open() throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            // Open existing workbook
            FileInputStream fileIn = new FileInputStream(file);
            workbook = new HSSFWorkbook(fileIn);
            successSheet = workbook.getSheet("SuccessSync");
            if (successSheet == null) {
                successSheet = workbook.createSheet("SuccessSync");
                createHeaderRow(successSheet);
            }
            failedSheet = workbook.getSheet("FailedSync");
            if (failedSheet == null) {
                failedSheet = workbook.createSheet("FailedSync");
                createHeaderRow(failedSheet);
            }
            return true;

        } else {
            // Create new workbook and sheets
            return openNew();
        }
    }

    public boolean openNew() {
        workbook = new HSSFWorkbook();
        successSheet = workbook.createSheet("SuccessSync");
        createHeaderRow(successSheet);
        failedSheet = workbook.createSheet("FailedSync");
        createHeaderRow(failedSheet);
        return true;
    }

    /**
     * Adds a single patient log to the appropriate sheet based on sync status.
     *
     * @param patientLog The patient log to add to the sheet.
     * @param syncStatus The sync status indicating which sheet to add to.
     * @return true if the patient log was added successfully, false otherwise.
     */
    public boolean addPatientLog(PatientLog patientLog, boolean syncStatus) {
        try {
            HSSFSheet targetSheet = syncStatus ? successSheet : failedSheet;
            int rowNum = targetSheet.getLastRowNum();
            if (rowNum == 0 && targetSheet.getRow(0) == null) {
                // No data in sheet yet
            } else {
                rowNum++; // Start from the next empty row
            }

            Row row = targetSheet.createRow(rowNum);
            createPatientLogRow(patientLog, row);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Saves the workbook to the file.
     *
     * @return true if the workbook was saved successfully, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public boolean save() throws Exception {
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            return true;
        } finally {
            workbook.close();
        }
    }

    /**
     * Retrieves the list of patient logs from the specified sheet.
     *
     * @param syncStatus The sync status indicating which sheet to retrieve from.
     * @return List of patient logs.
     */
    public List<PatientLog> getPatientLogs(boolean syncStatus) {
        List<PatientLog> patientLogs = new ArrayList<>();
        HSSFSheet targetSheet = syncStatus ? successSheet : failedSheet;
        try {
            int rowNum = targetSheet.getLastRowNum();
            for (int i = 1; i <= rowNum; i++) { // Skip header row
                Row row = targetSheet.getRow(i);
                if (row != null) {
                    PatientLog patientLog = createPatientLogFromRow(row);
                    patientLogs.add(patientLog);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patientLogs;
    }

    /**
     * Creates the header row for the sheet.
     *
     * @param sheet The sheet to add the header row to.
     */
    private static void createHeaderRow(HSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] newHeaders = {
                "Identifier",
                "Name",
                "Dob",
                "Gender",
                "Latitude",
                "Longitude",
                "Demography",
                "PBS",
                "Address updated",
                "Form",
                "Uuid",
                "Datetime",
                "Version Number",
                "Comment"
        };

        // Update the headers
        for (int i = 0; i < newHeaders.length; i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(newHeaders[i]);
        }
    }


    /**
     * Creates a row in the sheet with the patient log data.
     *
     * @param patientLog The patient log to write to the row.
     * @param row        The row to populate with patient log data.
     */
    private static void createPatientLogRow(PatientLog patientLog, Row row) {
         Cell cell1 = row.createCell(0);
        cell1.setCellValue(patientLog.getIdentifier());

        Cell cell2 = row.createCell(1);
        cell2.setCellValue(patientLog.getName());

        Cell cell3 = row.createCell(2);
        try {
            cell3.setCellValue(patientLog.getDob().split("T")[0]);
        }catch (Exception e){
            cell3.setCellValue(patientLog.getDob());
        }
        Cell cell4 = row.createCell(3);
        cell4.setCellValue(patientLog.getGender());

        Cell cell5 = row.createCell(4);
        cell5.setCellValue(patientLog.getLatitude());

        Cell cell6 = row.createCell(5);
        cell6.setCellValue(patientLog.getLongitude());

        Cell cell7 = row.createCell(6);
        cell7.setCellValue(patientLog.getBio());

        Cell cell8 = row.createCell(7);
        cell8.setCellValue(patientLog.getPbs());

        Cell cell9 = row.createCell(8);
        cell9.setCellValue(patientLog.isGeoCoordinateSync()?"Successful":"Unsuccessful");

        Cell cell10 = row.createCell(9);
        cell10.setCellValue(patientLog.getForms());

        Cell cell11 = row.createCell(10);
        cell11.setCellValue(patientLog.getUuid());

        Cell cell12 = row.createCell(11);
        cell12.setCellValue(patientLog.getDateTime());

        Cell cell13 = row.createCell(12);
        cell13.setCellValue(patientLog.getMobileVersion());

        Cell cell14 = row.createCell(13);
        cell14.setCellValue(patientLog.getErrorLog());

    }
    /**
     * Creates a PatientLog object from a row in the sheet.
     *
     * @param row The row to read data from.
     * @return A PatientLog object.
     */
    private static PatientLog createPatientLogFromRow(Row row) {
        String identifier = row.getCell(0).getStringCellValue();
        String name = row.getCell(1).getStringCellValue();
        String dob = row.getCell(2).getStringCellValue();
        String gender = row.getCell(3).getStringCellValue();
        String latitude = row.getCell(4).getStringCellValue();
        String longitude = row.getCell(5).getStringCellValue();
        String bio = row.getCell(6).getStringCellValue();
        String pbs =  row.getCell(7).getStringCellValue();
        boolean geoCord = row.getCell(8).getBooleanCellValue();
        String Forms = row.getCell(9).getStringCellValue();
        String uuid = row.getCell(10).getStringCellValue();
        String dateTime = row.getCell(11).getStringCellValue();
        String mobileVersion = row.getCell(12).getStringCellValue();
        String errorLog = row.getCell(13).getStringCellValue();

        return new PatientLog(identifier, name, dob, gender, latitude, longitude, bio,pbs, Forms, uuid,  mobileVersion, errorLog, dateTime, geoCord);
    }


}
