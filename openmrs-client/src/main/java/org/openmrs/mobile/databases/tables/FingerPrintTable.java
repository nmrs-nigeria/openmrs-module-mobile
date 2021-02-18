package org.openmrs.mobile.databases.tables;

import org.openmrs.mobile.activities.pbs.FingerPositions;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;


public class FingerPrintTable extends Table<PatientBiometricContract> {

    public static final String TABLE_NAME = "biometricinfo";

    /**
     * Number of columns without ID column
     * use as a param to
     *
     * @see org.openmrs.mobile.databases.tables.Table#values(int)
     */
    private static final int INSERT_COLUMNS_COUNT = 13;

    public class Column extends MasterColumn {
        public static final String biometricInfo_Id = "biometricInfo_Id";
        public static final String patient_id = "patient_id";
        public static final String template = "template";
        public static final String imageWidth = "imageWidth";
        public static final String imageHeight = "imageHeight";

        public static final String imageDPI = "imageDPI";
        public static final String imageQuality = "imageQuality";
        public static final String fingerPosition = "fingerPosition";
        public static final String serialNumber = "serialNumber";
        public static final String model = "model";

        public static final String manufacturer = "manufacturer";
        public static final String creator = "creator";
        public static final String SyncStatus = "syncStatus";
    }

    @Override
    public String createTableDefinition() {

        return CREATE_TABLE + TABLE_NAME + "("
                + Column.biometricInfo_Id + Column.Type.TEXT_TYPE_WITH_COMMA
                + Column.patient_id + Column.Type.INT_TYPE_WITH_COMMA
                + Column.template + Column.Type.TEXT_TYPE_WITH_COMMA
                + Column.imageWidth + Column.Type.INT_TYPE_WITH_COMMA
                + Column.imageHeight + Column.Type.INT_TYPE_WITH_COMMA
                + Column.imageDPI + Column.Type.INT_TYPE_WITH_COMMA
                + Column.imageQuality + Column.Type.INT_TYPE_WITH_COMMA
                + Column.fingerPosition + Column.Type.TEXT_TYPE_WITH_COMMA
                + Column.serialNumber + Column.Type.TEXT_TYPE_WITH_COMMA
                + Column.model + Column.Type.TEXT_TYPE_WITH_COMMA
                + Column.manufacturer + Column.Type.TEXT_TYPE_WITH_COMMA
                + Column.SyncStatus + Column.Type.INT_TYPE_WITH_COMMA
                + Column.creator + Column.Type.INT_TYPE
                + ");";
    }

    @Override
    public String insertIntoTableDefinition() {
        return INSERT_INTO + TABLE_NAME + "("
                + Column.biometricInfo_Id + Column.COMMA
                + Column.patient_id + Column.COMMA
                + Column.template + Column.COMMA
                + Column.imageWidth + Column.COMMA
                + Column.imageHeight + Column.COMMA
                + Column.imageDPI + Column.COMMA
                + Column.imageQuality + Column.COMMA
                + Column.fingerPosition + Column.COMMA
                + Column.serialNumber + Column.COMMA
                + Column.model + Column.COMMA
                + Column.manufacturer + Column.COMMA
                + Column.SyncStatus + Column.COMMA
                + Column.creator + ")"
                + values(INSERT_COLUMNS_COUNT);
    }

    @Override
    public String dropTableDefinition() {
        return DROP_TABLE_IF_EXISTS + TABLE_NAME;
    }

    @Override
    public Long insert(PatientBiometricContract tableObject) {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        return helper.insertFingerPrint(helper.getWritableDatabase(), tableObject);
    }

    @Override
    public int update(long tableObjectID, PatientBiometricContract tableObject) {
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        return openHelper.updateFingerPrint(openHelper.getWritableDatabase(), tableObject);
    }

    @Override
    public void delete(long patient_id) {
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        openHelper.getWritableDatabase().delete(TABLE_NAME, Column.patient_id + Table.MasterColumn.EQUALS + patient_id, null);
    }

    public void deleteFingerPrintCapture(long tableObjectID, FingerPositions fingerPosition) {
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        openHelper.getWritableDatabase().delete(TABLE_NAME, Column.patient_id + Table.MasterColumn.EQUALS + tableObjectID + MasterColumn.AND + Column.fingerPosition+ MasterColumn.EQUALS+ "'"+ fingerPosition + "'", null);
    }

    @Override
    public String toString() {
        return TABLE_NAME + createTableDefinition();
    }
}
