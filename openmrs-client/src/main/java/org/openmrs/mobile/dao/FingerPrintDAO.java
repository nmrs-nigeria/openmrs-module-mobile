package org.openmrs.mobile.dao;

import android.util.Log;

import net.sqlcipher.Cursor;
import org.openmrs.mobile.activities.pbs.FingerPositions;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbs.PatientBiometricDTO;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.tables.FingerPrintTable;
import org.openmrs.mobile.databases.tables.LocationTable;
import org.openmrs.mobile.databases.tables.PatientTable;
import org.openmrs.mobile.databases.tables.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FingerPrintDAO {

    public long saveFingerPrint(List<PatientBiometricContract> pbs) {
        for (int i = 0; i < pbs.size(); i++) {
            long id = new FingerPrintTable().insert(pbs.get(i));
            Log.e(TAG, "return id: " + id);
        }
        return 0;
    }

    public long saveFingerPrint(PatientBiometricContract pbs) {
        deletePrintPosition((long) pbs.getPatienId(), pbs.getFingerPositions());
        long id =  new FingerPrintTable().insert(pbs);
        Log.e(TAG, "return id: " +id);
        return id;
    }

    public void deletePrintPosition(Long patientId, FingerPositions fingerPosition) {
        new FingerPrintTable().deleteFingerPrintCapture(patientId, fingerPosition);
    }


    public void deleteAllPrints() {
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        openHelper.getWritableDatabase().execSQL(new FingerPrintTable().dropTableDefinition());
        openHelper.getWritableDatabase().execSQL(new FingerPrintTable().createTableDefinition());
        OpenMRS.getInstance().getOpenMRSLogger().d("All Finger Print deleted");
    }

    public void deletePrint(Long patientId) {
        new FingerPrintTable().delete(patientId);
    }


    public  List<PatientBiometricContract> getAll(boolean IncludeSyncRecord, String patient_Id) {
        List<PatientBiometricContract> pbsList = new ArrayList<>();
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = "";

        final Cursor cursor;
        if(!IncludeSyncRecord && patient_Id !=null) {
            where = FingerPrintTable.Column.patient_id + Table.MasterColumn.EQUALS + patient_Id + Table.MasterColumn.AND + FingerPrintTable.Column.SyncStatus + Table.MasterColumn.EQUALS + "0";
        }
       else if(patient_Id !=null) {
            where += FingerPrintTable.Column.patient_id + Table.MasterColumn.EQUALS + patient_Id;
        }
       else if(!IncludeSyncRecord) {
            where += FingerPrintTable.Column.SyncStatus + Table.MasterColumn.EQUALS + "0";
        }
        cursor = helper.getReadableDatabase().query(FingerPrintTable.TABLE_NAME, null, where, null, null, null, null);
//        if(!IncludeSyncRecord){
//            String where = String.format("%s = ?", FingerPrintTable.Column.SyncStatus);
//            String[] whereArgs = new String[]{"0"};
//            cursor = helper.getReadableDatabase().query(FingerPrintTable.TABLE_NAME, null, where, whereArgs, null, null, null);
//        } else{
//            cursor = helper.getReadableDatabase().query(FingerPrintTable.TABLE_NAME, null, null, null, null, null, null);
//        }

        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {

                    //create pbs table, then assign

                    int biometricInfo_Id_CI = cursor.getColumnIndex(FingerPrintTable.Column.biometricInfo_Id);
                    int patientId_CI = cursor.getColumnIndex(FingerPrintTable.Column.patient_id);
                    int template_CI = cursor.getColumnIndex(FingerPrintTable.Column.template);
                    int imageWidth_CI = cursor.getColumnIndex(FingerPrintTable.Column.imageWidth);
                    int imageHeight_CI = cursor.getColumnIndex(FingerPrintTable.Column.imageHeight);
                    int imageDPI_CI = cursor.getColumnIndex(FingerPrintTable.Column.imageDPI);
                    int imageQuality_CI = cursor.getColumnIndex(FingerPrintTable.Column.imageQuality);
                    int fingerPosition_CI = cursor.getColumnIndex(FingerPrintTable.Column.fingerPosition);
                    int serialNumber_CI = cursor.getColumnIndex(FingerPrintTable.Column.serialNumber);
                    int model_CI = cursor.getColumnIndex(FingerPrintTable.Column.model);
                    int manufacturer_CI = cursor.getColumnIndex(FingerPrintTable.Column.manufacturer);
                    int creator_CI = cursor.getColumnIndex(FingerPrintTable.Column.creator);
                    int syncStatus_CI = cursor.getColumnIndex(FingerPrintTable.Column.SyncStatus);


                    String biometricInfo_Id = cursor.getString(biometricInfo_Id_CI);
                    int patientId = cursor.getInt(patientId_CI);
                    String template = cursor.getString(template_CI);
                    int imageWidth = cursor.getInt(imageWidth_CI);
                    int imageHeight = cursor.getInt(imageHeight_CI);
                    int imageDPI = cursor.getInt(imageDPI_CI);
                    int imageQuality = cursor.getInt(imageQuality_CI);
                    String fingerPosition = cursor.getString(fingerPosition_CI);
                    String serialNumber = cursor.getString(serialNumber_CI);
                    String model = cursor.getString(model_CI);
                    String manufacturer = cursor.getString(manufacturer_CI);
                    int creator = cursor.getInt(creator_CI);
                    int syncStatus = cursor.getInt(syncStatus_CI);

                    PatientBiometricContract pbs = new PatientBiometricContract();
                    pbs.setBiometricInfo_Id(biometricInfo_Id);
                    pbs.setPatienId(patientId);
                    pbs.setTemplate(template);
                    pbs.setImageWidth(imageWidth);
                    pbs.setImageHeight(imageHeight);
                    pbs.setImageDPI(imageDPI);
                    pbs.setImageQuality(imageQuality);
                    pbs.setFingerPositions(Enum.valueOf(FingerPositions.class, fingerPosition));
                    pbs.setSerialNumber(serialNumber);
                    pbs.setModel(model);
                    pbs.setManufacturer(manufacturer);
                    pbs.setCreator(creator);
                    pbs.setSyncStatus(syncStatus);

                    pbsList.add(pbs);
                }
            } finally {
                cursor.close();
            }
        }
        return pbsList;
    }

    public boolean checkIfFingerPrintUptoSixFingers(String patientId) {

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = FingerPrintTable.Column.patient_id + Table.MasterColumn.EQUALS + patientId;
                //String.format("%s = ?", FingerPrintTable.Column.patient_id);
        String[] whereArgs = new String[]{patientId};
        String sql = "SELECT COUNT(*) as fingerprintCount FROM "+FingerPrintTable.TABLE_NAME + " where "+ where;
        final Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null);
                //.query(FingerPrintTable.TABLE_NAME, null, where, whereArgs, null, null, null);

        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    int id_CI = cursor.getColumnIndex("fingerprintCount");
                    return cursor.getInt(id_CI) >=6;
                   //return Objects.equals(patientId, cursor.getString(id_CI));
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    public int updatePatientFingerPrintSyncStatus(Long patientId, PatientBiometricContract tableObject) {
     return new FingerPrintTable().update(patientId, tableObject);
    }

}
