package org.openmrs.mobile.dao;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.util.Log;

import net.sqlcipher.Cursor;

import org.openmrs.mobile.activities.pbs.FingerPositions;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.tables.FingerPrintTable;
import org.openmrs.mobile.databases.tables.FingerPrintVerificationTable;
import org.openmrs.mobile.databases.tables.Table;

import java.util.ArrayList;
import java.util.List;

public class FingerPrintVerificationDAO {

    public void saveFingerPrint(List<PatientBiometricVerificationContract> pbs) {
        for (int i = 0; i < pbs.size(); i++) {

            if (pbs.get(i).getFingerPositions() != null) {
                long id = new FingerPrintVerificationTable().insert(pbs.get(i));
                Log.e(TAG, "return id: " + id);
            }
            else{
                Log.e(TAG, "skipping empty finger print: ");
            }
        }
    }

  public   int updateSync(Long patientID, int syncValue){
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        return   openHelper.updateVerificationSync(openHelper.getWritableDatabase(),patientID,syncValue);
    }

    public long saveFingerPrint(PatientBiometricVerificationContract pbs) {
        deletePrintPosition((long) pbs.getPatienId(), pbs.getFingerPositions());
        long id =  new FingerPrintVerificationTable().insert(pbs);
        Log.e(TAG, "return id: " +id);
        return id;
    }

    public long deletePrintPosition(Long patientId, FingerPositions fingerPosition) {
       return new FingerPrintVerificationTable().deleteFingerPrintCapture(patientId, fingerPosition);
    }


    public void deleteAllPrints() {
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        openHelper.getWritableDatabase().execSQL(new FingerPrintVerificationTable().dropTableDefinition());
        openHelper.getWritableDatabase().execSQL(new FingerPrintVerificationTable().createTableDefinition());
        OpenMRS.getInstance().getOpenMRSLogger().d("All Finger Print deleted");
    }

    public void deletePrint(Long patientId) {
        new FingerPrintVerificationTable().delete(patientId);
    }


    public  List<PatientBiometricVerificationContract> getAll(boolean IncludeSyncRecord, String patient_Id) {
        List<PatientBiometricVerificationContract> pbsList = new ArrayList<>();
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = "";

        final Cursor cursor;
        if(!IncludeSyncRecord && patient_Id !=null) {
            where = FingerPrintVerificationTable.Column.patient_id + Table.MasterColumn.EQUALS + patient_Id + Table.MasterColumn.AND + FingerPrintVerificationTable.Column.SyncStatus + Table.MasterColumn.EQUALS + "0";
        }
       else if(patient_Id !=null) {
            where += FingerPrintVerificationTable.Column.patient_id + Table.MasterColumn.EQUALS + patient_Id;
        }
       else if(!IncludeSyncRecord) {
            where += FingerPrintVerificationTable.Column.SyncStatus + Table.MasterColumn.EQUALS + "0";
        }
        cursor = helper.getReadableDatabase().query(FingerPrintVerificationTable.TABLE_NAME, null, where, null, null, null, null);
//        if(!IncludeSyncRecord){
//            String where = String.format("%s = ?", FingerPrintVerificationTable.Column.SyncStatus);
//            String[] whereArgs = new String[]{"0"};
//            cursor = helper.getReadableDatabase().query(FingerPrintVerificationTable.TABLE_NAME, null, where, whereArgs, null, null, null);
//        } else{
//            cursor = helper.getReadableDatabase().query(FingerPrintVerificationTable.TABLE_NAME, null, null, null, null, null, null);
//        }

        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {

                    //create pbs table, then assign

                    int biometricInfo_Id_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.biometricInfo_Id);
                    int patientId_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.patient_id);
                    int template_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.template);
                    int imageWidth_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageWidth);
                    int imageHeight_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageHeight);
                    int imageDPI_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageDPI);
                    int imageQuality_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageQuality);
                    int fingerPosition_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.fingerPosition);
                    int serialNumber_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.serialNumber);
                    int model_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.model);
                    int manufacturer_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.manufacturer);
                    int creator_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.creator);
                    int syncStatus_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.SyncStatus);
                    int dateCreated_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.dateCreated);


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
                    String dateCreated = cursor.getString(dateCreated_CI);// visit date

                    PatientBiometricVerificationContract pbs = new PatientBiometricVerificationContract();
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
                    pbs.setDateCreated(dateCreated);

                    pbsList.add(pbs);
                }
            } finally {
                cursor.close();
            }
        }
        return pbsList;
    }

    public List<PatientBiometricVerificationContract> getAllFingerPrintsOnDevice(){
        List<PatientBiometricVerificationContract> pbsList = new ArrayList<>();
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        final Cursor cursor;
        cursor = helper.getReadableDatabase().rawQuery("SELECT " + FingerPrintVerificationTable.Column.template +", " + FingerPrintVerificationTable.Column.fingerPosition + ", " + FingerPrintVerificationTable.Column.patient_id + " FROM " + FingerPrintVerificationTable.TABLE_NAME, null);
        if(cursor != null){
            try {
                while (cursor.moveToNext()){
                    int template_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.template);
                    int patientId_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.patient_id);
                    int fingerPosition_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.fingerPosition);

                    String template = cursor.getString(template_CI);
                    String fingerPosition = cursor.getString(fingerPosition_CI);
                    int patientId = cursor.getInt(patientId_CI);



                    PatientBiometricVerificationContract pbs = new PatientBiometricVerificationContract();
                    pbs.setTemplate(template);
                    pbs.setFingerPositions(Enum.valueOf(FingerPositions.class, fingerPosition));
                    pbs.setPatienId(patientId);

                    pbsList.add(pbs);
                }
            }catch (Exception e){
                OpenMRSCustomHandler.writeLogToFile(e.getMessage());
            }finally {
                cursor.close();
            }
        }
        return  pbsList;
    }

    public boolean checkIfFingerPrintUptoSixFingers(String patientId) {

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = FingerPrintVerificationTable.Column.patient_id + Table.MasterColumn.EQUALS + patientId;
                //String.format("%s = ?", FingerPrintVerificationTable.Column.patient_id);
        String[] whereArgs = new String[]{patientId};
        String sql = "SELECT COUNT(*) as fingerprintCount FROM "+FingerPrintVerificationTable.TABLE_NAME + " where "+ where;
        final Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null);
                //.query(FingerPrintVerificationTable.TABLE_NAME, null, where, whereArgs, null, null, null);

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

    public int updatePatientFingerPrintSyncStatus(Long patientId, PatientBiometricVerificationContract tableObject) {
     return new FingerPrintVerificationTable().update(patientId, tableObject);
    }

    public List<PatientBiometricVerificationContract> getSinglePatientPBS(Long patient_Id){
        List<PatientBiometricVerificationContract> pbsList = new ArrayList<>();
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = FingerPrintVerificationTable.Column.patient_id + Table.MasterColumn.EQUALS + patient_Id;
        final Cursor cursor = helper.getReadableDatabase().query(FingerPrintVerificationTable.TABLE_NAME, null, where, null, null, null, null);

        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {

                    //create pbs table, then assign

                    int biometricInfo_Id_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.biometricInfo_Id);
                    int patientId_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.patient_id);
                    int template_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.template);
                    int imageWidth_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageWidth);
                    int imageHeight_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageHeight);
                    int imageDPI_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageDPI);
                    int imageQuality_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.imageQuality);
                    int fingerPosition_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.fingerPosition);
                    int serialNumber_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.serialNumber);
                    int model_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.model);
                    int manufacturer_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.manufacturer);
                    int creator_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.creator);
                    int syncStatus_CI = cursor.getColumnIndex(FingerPrintVerificationTable.Column.SyncStatus);


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

                    PatientBiometricVerificationContract pbs = new PatientBiometricVerificationContract();
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

                    Log.v("Baron", "Image Quality: " + imageQuality + " Finger Position:" + fingerPosition);
                }
            } finally {
                cursor.close();
            }
        }
        return pbsList;
    }

    public boolean safeToDelete(long patientId) {
        final String countCol="countCol";
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = FingerPrintVerificationTable.Column.patient_id + Table.MasterColumn.EQUALS + patientId +" AND "+
                FingerPrintVerificationTable.Column.SyncStatus + Table.MasterColumn.EQUALS+"0";
        String sql = "SELECT COUNT(*) as "+ countCol+" FROM "+FingerPrintVerificationTable.TABLE_NAME + " WHERE "+ where;
        final Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null);
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    int id_CI = cursor.getColumnIndex(countCol);
                    return cursor.getInt(id_CI) <1; // return   true if the sync any of the patient with the id have a sync 0
                }
            } finally {
                cursor.close();
            }
        }

        return  false ;
    }
}
