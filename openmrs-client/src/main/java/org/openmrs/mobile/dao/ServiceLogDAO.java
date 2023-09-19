package org.openmrs.mobile.dao;

import net.sqlcipher.Cursor;

import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.databases.tables.ServiceLogTable;
import org.openmrs.mobile.models.ServiceLog;
import org.openmrs.mobile.utilities.DateUtils;

import java.util.Date;

public class ServiceLogDAO {


    // save service, this will be done during forms submission
    public long saveServiceLog(ServiceLog serviceLog) {
        long id = new ServiceLogTable().insert(serviceLog);
        return id;
    }

    // get list of ServiceLog that are not voided and sort by descending using date created which is the time stamp
    public ServiceLog getServiceLogs(String patientUUID,String patientId) {

        String todayDate = DateUtils.convertTime(DateUtils.convertTime(DateUtils.getCurrentDateTime()),
                DateUtils.OPEN_MRS_PBS_DATE_FORMAT);

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        final Cursor cursor;
        String sqlQuery = "SELECT * FROM " + ServiceLogTable.TABLE_NAME
                + " WHERE (" + ServiceLogTable.Column.PATIENT_UUID + "=? OR "+ServiceLogTable.Column.PATIENT_ID+"=?) AND " + ServiceLogTable.Column.VOIDED
                + "=?   AND  " + ServiceLogTable.Column.DATE_CREATED + " <= ? ORDER BY " + ServiceLogTable.Column.DATE_CREATED + " DESC LIMIT ?";

       // Util.log(sqlQuery);
        String[] selectionArgs = {patientUUID, patientId, "0", todayDate, "1"};
        cursor = helper.getReadableDatabase().rawQuery(sqlQuery, selectionArgs);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    int visitDateIndex = cursor.getColumnIndex(ServiceLogTable.Column.VISIT_DATE);
                    int dateCreatedIndex = cursor.getColumnIndex(ServiceLogTable.Column.DATE_CREATED);
                    int formNameIndex = cursor.getColumnIndex(ServiceLogTable.Column.FORM_NAME);
                    int voidedIndex = cursor.getColumnIndex(ServiceLogTable.Column.VOIDED);
                    int patientUuidIndex = cursor.getColumnIndex(ServiceLogTable.Column.PATIENT_UUID);
                    int idIndex = cursor.getColumnIndex(ServiceLogTable.Column.PATIENT_ID);
                    return new ServiceLog(cursor.getInt(idIndex),
                            cursor.getString(formNameIndex),
                            cursor.getString(patientUuidIndex),
                            cursor.getInt(voidedIndex),
                            cursor.getString(dateCreatedIndex),
                            cursor.getString(visitDateIndex)
                    );
                }
            } catch (Exception e) {
                Util.log("Error" + e.getMessage());
                OpenMRSCustomHandler.writeLogToFile(e.getMessage());
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public int set_patient_PBS_void(String patientId, String patientUUID, int voided) {
        return new ServiceLogTable().set_patient_void_status_on_PBS_completion(patientId,patientUUID, voided);
    }


    public String getVisitDate(long patientId) {
        ServiceLog log;
        String patientUUID = new PatientDAO().getPatientUUID(String.valueOf(patientId));
        if (patientUUID != null) {
            log = getServiceLogs(patientUUID,String.valueOf(patientId) );
        }else {
            log = getServiceLogs("", String.valueOf(patientId));
        }
        if (log != null) {
            return log.getVisitDate();
        }
        return null;
    }
}
