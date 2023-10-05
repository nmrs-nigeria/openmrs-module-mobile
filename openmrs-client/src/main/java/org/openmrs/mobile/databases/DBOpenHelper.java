/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.databases;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.databases.tables.ConceptTable;
import org.openmrs.mobile.databases.tables.EncounterTable;
import org.openmrs.mobile.databases.tables.FingerPrintTable;
import org.openmrs.mobile.databases.tables.FingerPrintVerificationTable;
import org.openmrs.mobile.databases.tables.LocationTable;
import org.openmrs.mobile.databases.tables.ObservationTable;
import org.openmrs.mobile.databases.tables.PatientTable;
import org.openmrs.mobile.databases.tables.ServiceLogTable;
import org.openmrs.mobile.databases.tables.Table;
import org.openmrs.mobile.databases.tables.VisitTable;
import org.openmrs.mobile.models.Concept;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Observation;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.ServiceLog;
import org.openmrs.mobile.models.Visit;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.schedulers.Schedulers;

public class DBOpenHelper extends OpenMRSSQLiteOpenHelper {
    private static int DATABASE_VERSION = OpenMRS.getInstance().
            getResources().getInteger(R.integer.dbversion);
    private static final String WHERE_ID_CLAUSE = String.format("%s = ?", Table.MasterColumn.ID);

    private PatientTable mPatientTable;
    private ConceptTable mConceptTable;
    private VisitTable mVisitTable;
    private EncounterTable mEncounterTable;
    private ObservationTable mObservationTable;
    private LocationTable mLocationTable;
    private ServiceLogTable mServiceLogTable;
    private FingerPrintTable mFingerPrintTable;
    private FingerPrintVerificationTable mFingerPrintVerificationTable;
    private OpenMRS mOpenMRS;

    public DBOpenHelper(Context context) {
        super(context,null, DATABASE_VERSION);
        this.mPatientTable = new PatientTable();
        this.mConceptTable = new ConceptTable();
        this.mVisitTable = new VisitTable();
        this.mEncounterTable = new EncounterTable();
        this.mObservationTable = new ObservationTable();
        this.mLocationTable = new LocationTable();
        this.mFingerPrintTable = new FingerPrintTable();
        // verification table
        mFingerPrintVerificationTable = new FingerPrintVerificationTable();
        // service log table
        this.mServiceLogTable = new ServiceLogTable();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        mLogger.d("Database creating...");
        sqLiteDatabase.execSQL(mPatientTable.createTableDefinition());
        logOnCreate(mPatientTable.toString());
        sqLiteDatabase.execSQL(mConceptTable.createTableDefinition());
        logOnCreate(mConceptTable.toString());
        sqLiteDatabase.execSQL(mVisitTable.createTableDefinition());
        logOnCreate(mVisitTable.toString());
        sqLiteDatabase.execSQL(mEncounterTable.createTableDefinition());
        logOnCreate(mEncounterTable.toString());
        sqLiteDatabase.execSQL(mObservationTable.createTableDefinition());
        logOnCreate(mObservationTable.toString());
        sqLiteDatabase.execSQL(mLocationTable.createTableDefinition());
        logOnCreate(mLocationTable.toString());

        sqLiteDatabase.execSQL(mFingerPrintTable.createTableDefinition());
        logOnCreate(mFingerPrintTable.toString());

        // recapture  update
        sqLiteDatabase.execSQL(mFingerPrintVerificationTable.createTableDefinition());
        logOnCreate(mFingerPrintVerificationTable.toString());
        //service log
        sqLiteDatabase.execSQL(mServiceLogTable.createTableDefinition());
        logOnCreate(mServiceLogTable.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int currentVersion, int newVersion) {
        mLogger.d( "Updating table from " + currentVersion + " to " + newVersion);
        switch (currentVersion) {
            case 8:
                sqLiteDatabase.execSQL(new ConceptTable().createTableDefinition());
            case 9:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 10:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 11:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 12:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 13:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 14:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 15:
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
            case 16:

            case 18:

//                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
//                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
//            case 19:
//                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierOpenmrs TEXT");
//                sqLiteDatabase.execSQL("ALTER TABLE patients ADD COLUMN identifierTypeOpenmrs TEXT");
//            case 20:

                //and so on.. do not add breaks so that switch will
                //start at oldVersion, and run straight through to the latest
        }
    }

    private void logOnCreate(String tableToString) {
        mLogger.d("Table " + tableToString + " ver." + DATABASE_VERSION + " created");
    }

    public long insertPatient(SQLiteDatabase db, Patient patient) {
        long patientId;

        SQLiteStatement patientStatement = db.compileStatement(mPatientTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();

            bindString(1, patient.getName().getNameString(), patientStatement);
            bindString(2, Boolean.toString(patient.isSynced()),patientStatement);

            if (patient.getUuid() != null)
                bindString(3, patient.getUuid(), patientStatement);
            else
                bindString(3, null, patientStatement);

            if (patient.getIdentifier() != null) {
                for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
                    switch (patientIdentifier.getIdentifierType().getDisplay()){
                        case "Hospital Number":
                            bindString(4, patientIdentifier.getIdentifier(), patientStatement);
                            bindString(31, patientIdentifier.getIdentifierType().getDisplay(), patientStatement);
                            break;
                        case "ART Number":
                            bindString(23, patientIdentifier.getIdentifier(), patientStatement);
                            bindString(27, patientIdentifier.getIdentifierType().getDisplay(), patientStatement);
                            break;
                        case "ANC Number":
                            bindString(24, patientIdentifier.getIdentifier(), patientStatement);
                            bindString(28, patientIdentifier.getIdentifierType().getDisplay(), patientStatement);
                            break;
                        case "Exposed Infant Id":
                            bindString(26, patientIdentifier.getIdentifier(), patientStatement);
                            bindString(30, patientIdentifier.getIdentifierType().getDisplay(), patientStatement);
                            break;
                        case "HIV testing Id (Client Code)":
                            bindString(25, patientIdentifier.getIdentifier(), patientStatement);
                            bindString(29, patientIdentifier.getIdentifierType().getDisplay(), patientStatement);
                            break;
                        default:
                            // Do nothing
                            break;
                    }
                }
                bindString(33, null, patientStatement);
                bindString(34, null, patientStatement);
            }
            else
                bindString(4, null, patientStatement);

            bindString(5, patient.getName().getGivenName(), patientStatement);
            bindString(6, patient.getName().getMiddleName(), patientStatement);
            bindString(7, patient.getName().getFamilyName(), patientStatement);
            bindString(8, patient.getGender(), patientStatement);
            bindString(9, patient.getBirthdate(), patientStatement);
            bindLong(10, null, patientStatement);
            bindString(11, null, patientStatement);
            bindString(12, null, patientStatement);
            if (null != patient.getPhoto()) {
                bindBlob(13, bitmapToByteArray(patient.getPhoto()), patientStatement);
            }
            if (null != patient.getAddress()) {
                bindString(14, patient.getAddress().getAddress1(), patientStatement);
                bindString(15, patient.getAddress().getAddress2(), patientStatement);
                bindString(16, patient.getAddress().getPostalCode(), patientStatement);
                bindString(17, patient.getAddress().getCountry(), patientStatement);
                bindString(18, patient.getAddress().getStateProvince(), patientStatement);
                bindString(19, patient.getAddress().getCityVillage(), patientStatement);
                bindString(21, patient.getAddress().getLatitude(), patientStatement);
                bindString(22, patient.getAddress().getLongitude(), patientStatement);
            }
            bindString(20, patient.getEncounters(), patientStatement);
            if (null != patient.getAttribute()) {
                bindString(32, patient.getAttribute().getValue(), patientStatement);
            }
            bindString(35, null, patientStatement);
            patientId = patientStatement.executeInsert();
            patientStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            patientStatement.close();
        }

        patient.setId(patientId);

        return patientId;
    }

    public int updatePatient(SQLiteDatabase db, long patientID, Patient patient) {
        ContentValues newValues = new ContentValues();
        newValues.put(PatientTable.Column.UUID, patient.getUuid());
        newValues.put(PatientTable.Column.SYNCED, patient.isSynced());
        newValues.put(PatientTable.Column.DISPLAY, patient.getDisplay());
        for (PatientIdentifier patientIdentifier : patient.getIdentifiers()){
            if(patientIdentifier.getDisplay() != null) {
                switch (patientIdentifier.getDisplay()) {
                    case "Hospital Number":
                        newValues.put(PatientTable.Column.IDENTIFIER, patientIdentifier.getIdentifier());
                        newValues.put(PatientTable.Column.IDENTIFIER_TYPE_HOSPITAL, patientIdentifier.getDisplay());
                        break;
                    case "ART Number":
                        newValues.put(PatientTable.Column.IDENTIFIER_ART, patientIdentifier.getIdentifier());
                        newValues.put(PatientTable.Column.IDENTIFIER_TYPE_ART, patientIdentifier.getDisplay());
                        break;
                    case "ANC Number":
                        newValues.put(PatientTable.Column.IDENTIFIER_ANC, patientIdentifier.getIdentifier());
                        newValues.put(PatientTable.Column.IDENTIFIER_TYPE_ANC, patientIdentifier.getDisplay());
                        break;
                    case "Exposed Infant Id":
                        newValues.put(PatientTable.Column.IDENTIFIER_HEI, patientIdentifier.getIdentifier());
                        newValues.put(PatientTable.Column.IDENTIFIER_TYPE_HEI, patientIdentifier.getDisplay());
                        break;
                    case "HIV testing Id (Client Code)":
                        newValues.put(PatientTable.Column.IDENTIFIER_HTS, patientIdentifier.getIdentifier());
                        newValues.put(PatientTable.Column.IDENTIFIER_TYPE_HTS, patientIdentifier.getDisplay());
                        break;
                    default:
                        // Do nothing
                        break;
                }
                if (patientIdentifier.getIdentifierType() != null && patientIdentifier.getIdentifierType().getDisplay().equals("OpenMRS ID")){
                    newValues.put(PatientTable.Column.IDENTIFIER_OPENMRS, patientIdentifier.getIdentifier());
                    newValues.put(PatientTable.Column.IDENTIFIER_TYPE_OPENMRS, patientIdentifier.getIdentifierType().getDisplay());
                }
            }

        }

        newValues.put(PatientTable.Column.GIVEN_NAME, patient.getName().getGivenName());
        newValues.put(PatientTable.Column.MIDDLE_NAME, patient.getName().getMiddleName());

        newValues.put(PatientTable.Column.FAMILY_NAME, patient.getName().getFamilyName());
        newValues.put(PatientTable.Column.GENDER, patient.getGender());
        newValues.put(PatientTable.Column.BIRTH_DATE, patient.getBirthdate());

        newValues.put(PatientTable.Column.DEATH_DATE, (Long) null);
        newValues.put(PatientTable.Column.CAUSE_OF_DEATH, (String) null);
        newValues.put(PatientTable.Column.AGE, (String) null);
        if (null != patient.getPhoto()) {
            mLogger.i("inserting into db");
            newValues.put(PatientTable.Column.PHOTO, bitmapToByteArray(patient.getPhoto()));
        }

        if (null != patient.getAddress()) {
            newValues.put(PatientTable.Column.ADDRESS_1, patient.getAddress().getAddress1());
            newValues.put(PatientTable.Column.ADDRESS_2, patient.getAddress().getAddress2());
            newValues.put(PatientTable.Column.POSTAL_CODE, patient.getAddress().getPostalCode());
            newValues.put(PatientTable.Column.COUNTRY, patient.getAddress().getCountry());
            newValues.put(PatientTable.Column.STATE, patient.getAddress().getStateProvince());
            newValues.put(PatientTable.Column.CITY, patient.getAddress().getCityVillage());
            newValues.put(PatientTable.Column.LATITUDE, patient.getAddress().getLatitude());
            newValues.put(PatientTable.Column.LONGITUDE, patient.getAddress().getLongitude());

        }
        newValues.put(PatientTable.Column.ENCOUNTERS, patient.getEncounters());
        if (null != patient.getAttribute()) {
            newValues.put(PatientTable.Column.PHONE_NUMBER, patient.getAttribute().getValue());
        }

        String[] whereArgs = new String[]{String.valueOf(patientID)};

        return db.update(PatientTable.TABLE_NAME, newValues, WHERE_ID_CLAUSE, whereArgs);
    }

    public long insertConcept(SQLiteDatabase db, Concept concept) {
        long conceptId;
        SQLiteStatement statement = db.compileStatement(mConceptTable.insertIntoTableDefinition());
        try {
            db.beginTransaction();
            bindString(1, concept.getUuid(), statement);
            bindString(2, concept.getDisplay(), statement);
            conceptId = statement.executeInsert();
            statement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            statement.close();
        }
        return conceptId;
    }

    public int updateConcept(SQLiteDatabase db, long conceptId, Concept concept) {
        ContentValues newValues = new ContentValues();
        newValues.put(ConceptTable.Column.UUID, concept.getUuid());
        newValues.put(ConceptTable.Column.DISPLAY, concept.getDisplay());

        String[] whereArgs = new String[]{String.valueOf(conceptId)};

        return db.update(ConceptTable.TABLE_NAME, newValues, WHERE_ID_CLAUSE, whereArgs);
    }

    public long insertVisit(SQLiteDatabase db, Visit visit) {
        long visitId;

        SQLiteStatement visitStatement = db.compileStatement(mVisitTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindString(1, visit.getUuid(), visitStatement);
            bindLong(2, visit.getPatient().getId(), visitStatement);
            bindString(3, visit.getVisitType().getDisplay(), visitStatement);
            if (visit.getLocation() != null) {
                bindString(4, visit.getLocation().getDisplay(), visitStatement);
            }
            bindString(5, visit.getStartDatetime(), visitStatement);
            bindString(6, visit.getStopDatetime(), visitStatement);
            visitId = visitStatement.executeInsert();
            visitStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            visitStatement.close();
        }
        return visitId;
    }

    public int updateVisit(SQLiteDatabase db, long visitID, Visit visit) {
        ContentValues newValues = new ContentValues();
        newValues.put(VisitTable.Column.UUID, visit.getUuid());
        newValues.put(VisitTable.Column.PATIENT_KEY_ID, visit.getPatient().getId());
        newValues.put(VisitTable.Column.VISIT_TYPE, visit.getVisitType().getDisplay());
        if (visit.getLocation() != null) {
            newValues.put(VisitTable.Column.VISIT_PLACE, visit.getLocation().getDisplay());
        }
        newValues.put(VisitTable.Column.START_DATE, visit.getStartDatetime());
        newValues.put(VisitTable.Column.STOP_DATE, visit.getStopDatetime());

        String[] whereArgs = new String[]{String.valueOf(visitID)};

        return db.update(VisitTable.TABLE_NAME, newValues, WHERE_ID_CLAUSE, whereArgs);
    }

    public long insertEncounter(SQLiteDatabase db, Encounter encounter) {
        long encounterId;

        SQLiteStatement encounterStatement = db.compileStatement(mEncounterTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindLong(1, encounter.getVisitID(), encounterStatement);
            bindString(2, encounter.getUuid(), encounterStatement);
            bindString(3, encounter.getDisplay(), encounterStatement);
            bindLong(4, encounter.getEncounterDatetime(), encounterStatement);
            bindString(5, encounter.getEncounterType().getDisplay(), encounterStatement);
            bindString(6, encounter.getPatientUUID(), encounterStatement);
            bindString(7, encounter.getFormUuid(), encounterStatement);
            encounterId = encounterStatement.executeInsert();
            encounterStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            encounterStatement.close();
        }
        return encounterId;
    }

    public int updateEncounter(SQLiteDatabase db, long encounterID, Encounter encounter) {
        ContentValues newValues = new ContentValues();
        newValues.put(EncounterTable.Column.UUID, encounter.getUuid());
        newValues.put(EncounterTable.Column.VISIT_KEY_ID, encounter.getVisitID());
        newValues.put(EncounterTable.Column.DISPLAY, encounter.getDisplay());
        newValues.put(EncounterTable.Column.ENCOUNTER_DATETIME, encounter.getEncounterDatetime());
        newValues.put(EncounterTable.Column.ENCOUNTER_TYPE, encounter.getEncounterType().getDisplay());

        String[] whereArgs = new String[]{String.valueOf(encounterID)};

        return db.update(EncounterTable.TABLE_NAME, newValues, WHERE_ID_CLAUSE, whereArgs);
    }

    public long insertObservation(SQLiteDatabase db, Observation obs) {
        long obsID;
        SQLiteStatement observationStatement = db.compileStatement(mObservationTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindLong(1, obs.getEncounterID(), observationStatement);
            bindString(2, obs.getUuid(), observationStatement);
            bindString(3, obs.getDisplay(), observationStatement);
            bindString(4, obs.getDisplayValue(), observationStatement);
            if (obs.getDiagnosisOrder() != null) {
                bindString(5, obs.getDiagnosisOrder(), observationStatement);
            }
            bindString(6, obs.getDiagnosisList(), observationStatement);
            if (obs.getDiagnosisCertainty() != null) {
                bindString(7, obs.getDiagnosisCertainty(), observationStatement);
            }
            bindString(8, obs.getDiagnosisNote(), observationStatement);
            if (obs.getConcept() != null) {
                bindString(9, obs.getConcept().getUuid(), observationStatement);
            }
            obsID = observationStatement.executeInsert();
            observationStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            observationStatement.close();
        }
        return obsID;
    }

    public int updateObservation(SQLiteDatabase db, long observationID, Observation observation) {
        ContentValues newValues = new ContentValues();
        newValues.put(ObservationTable.Column.UUID, observation.getUuid());
        newValues.put(ObservationTable.Column.ENCOUNTER_KEY_ID, observation.getEncounterID());
        newValues.put(ObservationTable.Column.DISPLAY, observation.getDisplay());
        newValues.put(ObservationTable.Column.DISPLAY_VALUE, observation.getDisplayValue());
        if (observation.getDiagnosisOrder() != null) {
            newValues.put(ObservationTable.Column.DIAGNOSIS_ORDER, observation.getDiagnosisOrder());
        }
        newValues.put(ObservationTable.Column.DIAGNOSIS_LIST, observation.getDiagnosisList());
        if (observation.getDiagnosisCertainty() != null) {
            newValues.put(ObservationTable.Column.DIAGNOSIS_CERTAINTY, observation.getDiagnosisCertainty());
        }
        newValues.put(ObservationTable.Column.DIAGNOSIS_NOTE, observation.getDiagnosisNote());

        String[] whereArgs = new String[]{String.valueOf(observationID)};

        return db.update(ObservationTable.TABLE_NAME, newValues, WHERE_ID_CLAUSE, whereArgs);
    }

    public Long insertLocation(SQLiteDatabase db, Location loc) {
        long locID;

        SQLiteStatement locationStatement = db.compileStatement(mLocationTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindString(1, loc.getUuid(), locationStatement);
            bindString(2, loc.getDisplay(), locationStatement);
            bindString(3, loc.getName(), locationStatement);
            bindString(4, loc.getDescription(), locationStatement);
            bindString(5, loc.getAddress1(), locationStatement);
            bindString(6, loc.getAddress2(), locationStatement);
            bindString(7, loc.getCityVillage(), locationStatement);
            bindString(8, loc.getStateProvince(), locationStatement);
            bindString(9, loc.getCountry(), locationStatement);
            bindString(10, loc.getPostalCode(), locationStatement);
            bindString(11, loc.getParentLocationUuid(), locationStatement);
            locID = locationStatement.executeInsert();
            locationStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            locationStatement.close();
        }
        return locID;
    }

    public static <T> Observable<T> createObservableIO(final Callable<T> func) {
        return Observable.fromCallable(func)
                .subscribeOn(Schedulers.io());
    }

    private byte[] bitmapToByteArray(Bitmap image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public long insertFingerPrint(SQLiteDatabase db, PatientBiometricContract fingerPrintObj) {
        long id;

        if(!TableExists(db, FingerPrintTable.TABLE_NAME)){
            db.execSQL(mFingerPrintTable.createTableDefinition());
            logOnCreate(mFingerPrintTable.toString());
        }
        SQLiteStatement pbsStatement = db.compileStatement(mFingerPrintTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindString(1, fingerPrintObj.getBiometricInfo_Id(), pbsStatement);
            bindLong(2, (long) fingerPrintObj.getPatienId(), pbsStatement);
            bindString(3, fingerPrintObj.getTemplate(), pbsStatement);
            bindLong(4, (long) fingerPrintObj.getImageWidth(), pbsStatement);
            bindLong(5, (long) fingerPrintObj.getImageHeight(), pbsStatement);
            bindLong(6, (long) fingerPrintObj.getImageDPI(), pbsStatement);
            bindLong(7, (long) fingerPrintObj.getImageQuality(), pbsStatement);
            bindString(8, fingerPrintObj.getFingerPositions().toString(), pbsStatement);
            bindString(9, fingerPrintObj.getSerialNumber(), pbsStatement);
            bindString(10, fingerPrintObj.getModel(), pbsStatement);
            bindString(11, fingerPrintObj.getManufacturer(), pbsStatement);
            bindLong(12, (long) fingerPrintObj.getSyncStatus(), pbsStatement);
            bindString(13,fingerPrintObj.getDateCreated(),pbsStatement );
            bindLong(14, (long) fingerPrintObj.getCreator(), pbsStatement);

            id = pbsStatement.executeInsert();
            pbsStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            pbsStatement.close();
        }
        return id;
    }

    public int updateFingerPrint(SQLiteDatabase db, PatientBiometricContract fingerPrintObj) {
        ContentValues newValues = new ContentValues();
        newValues.put(FingerPrintTable.Column.template, "");
        newValues.put(FingerPrintTable.Column.SyncStatus, fingerPrintObj.getSyncStatus());

        String[] whereArgs = new String[]{String.valueOf(fingerPrintObj.getPatienId())};

        String _where_clause = String.format("%s = ?", FingerPrintTable.Column.patient_id);
        return db.update(FingerPrintTable.TABLE_NAME, newValues, _where_clause, whereArgs);
    }

    public int updateBaseSync(SQLiteDatabase db,Long patientID, int value,boolean saveTemplate) {
        ContentValues newValues = new ContentValues();
        newValues.put(FingerPrintTable.Column.SyncStatus,  value);
        if(!saveTemplate)
            newValues.put(FingerPrintTable.Column.template,  "");
        String[] whereArgs = new String[]{String.valueOf( patientID)};
        String _where_clause = String.format("%s = ?", FingerPrintTable.Column.patient_id);
        return db.update(FingerPrintTable.TABLE_NAME, newValues, _where_clause, whereArgs);
    }
    public int updateVerificationSync(SQLiteDatabase db,Long patientID, int value) {
        ContentValues newValues = new ContentValues();
        newValues.put(FingerPrintVerificationTable.Column.SyncStatus,  value);
        String[] whereArgs = new String[]{String.valueOf( patientID)};
        String _where_clause = String.format("%s = ?", FingerPrintVerificationTable.Column.patient_id);
        return db.update(FingerPrintVerificationTable.TABLE_NAME, newValues, _where_clause, whereArgs);
    }

    // Verification transactions
    public long insertFingerPrintVerification(SQLiteDatabase db, PatientBiometricVerificationContract fingerPrintObj) {
        long id;

        if(!TableExists(db, FingerPrintVerificationTable.TABLE_NAME)){
            db.execSQL(mFingerPrintVerificationTable.createTableDefinition());
            logOnCreate(mFingerPrintVerificationTable.toString());
        }
        SQLiteStatement pbsStatement = db.compileStatement(mFingerPrintVerificationTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindString(1, fingerPrintObj.getBiometricInfo_Id(), pbsStatement);
            bindLong(2, (long) fingerPrintObj.getPatienId(), pbsStatement);
            bindString(3, fingerPrintObj.getTemplate(), pbsStatement);
            bindLong(4, (long) fingerPrintObj.getImageWidth(), pbsStatement);
            bindLong(5, (long) fingerPrintObj.getImageHeight(), pbsStatement);
            bindLong(6, (long) fingerPrintObj.getImageDPI(), pbsStatement);
            bindLong(7, (long) fingerPrintObj.getImageQuality(), pbsStatement);
            bindString(8, fingerPrintObj.getFingerPositions().toString(), pbsStatement);
            bindString(9, fingerPrintObj.getSerialNumber(), pbsStatement);
            bindString(10, fingerPrintObj.getModel(), pbsStatement);
            bindString(11, fingerPrintObj.getManufacturer(), pbsStatement);
            bindLong(12, (long) fingerPrintObj.getSyncStatus(), pbsStatement);
            bindString(13,fingerPrintObj.getDateCreated(),pbsStatement );
            bindLong(14, (long) fingerPrintObj.getCreator(), pbsStatement);

            id = pbsStatement.executeInsert();
            pbsStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            pbsStatement.close();
        }
        return id;
    }

    public int updateFingerPrintVerification(SQLiteDatabase db, PatientBiometricVerificationContract fingerPrintObj) {
        ContentValues newValues = new ContentValues();
        newValues.put(FingerPrintVerificationTable.Column.SyncStatus, fingerPrintObj.getSyncStatus());

        String[] whereArgs = new String[]{String.valueOf(fingerPrintObj.getPatienId())};

        String _where_clause = String.format("%s = ?", FingerPrintVerificationTable.Column.patient_id);
        return db.update(FingerPrintVerificationTable.TABLE_NAME, newValues, _where_clause, whereArgs);
    }
// End of verification


    // service log start
    public long insertServiceLog(SQLiteDatabase db, ServiceLog serviceLog) {
        long id;

        if(!TableExists(db, ServiceLogTable.TABLE_NAME)){
            db.execSQL(mServiceLogTable.createTableDefinition());
            logOnCreate(mServiceLogTable.toString());
        }
        SQLiteStatement logStatement = db.compileStatement(mServiceLogTable.insertIntoTableDefinition());

        try {
            db.beginTransaction();
            bindLong(1,  serviceLog.getPatientId(), logStatement);
            bindString(2,  serviceLog.getPatientUUID(), logStatement);
            bindLong(3,(long) serviceLog.getVoided(), logStatement);
            bindString(4, serviceLog.getDateCreated(), logStatement);
            bindString(5, serviceLog.getVisitDate(), logStatement);
            bindString(6, serviceLog.getFormName(), logStatement);
            id = logStatement.executeInsert();
            logStatement.clearBindings();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            logStatement.close();
        }
        return id;
    }

    // patientUUID and voided must also be set  in the provided service log and update
    public int voidPatientServiceLogs(SQLiteDatabase db, String patientId, String patientUUID, long voided) {
        ContentValues newValues = new ContentValues();
        newValues.put(ServiceLogTable.Column.VOIDED, voided);
        String[] whereArgs = new String[]{String.valueOf(patientUUID), patientId};
        String _where_clause = String.format("%s = ? OR %s = ? ",
                ServiceLogTable.Column.PATIENT_UUID, ServiceLogTable.Column.PATIENT_ID);
        return db.update(ServiceLogTable.TABLE_NAME, newValues, _where_clause, whereArgs);
    }

    //service log end

























    public boolean TableExists(SQLiteDatabase db, String tableName) {

        String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            return false;
        }
    }
}
