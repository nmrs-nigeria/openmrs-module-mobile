package org.openmrs.mobile.dao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.collections.map.LinkedMap;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.tables.FingerPrintTable;
import org.openmrs.mobile.databases.tables.FingerPrintVerificationTable;
import org.openmrs.mobile.databases.tables.PatientTable;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PersonAddress;
import org.openmrs.mobile.models.PersonAttribute;
import org.openmrs.mobile.models.PersonAttributeType;
import org.openmrs.mobile.models.PersonName;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*
Left JOIN patient for new patients variables
 finger_count
 sync_count

 */
public class PatientBiometricJoinDAO {
    private static String fingerCount= "finger_count";
    private static String syncCount= "sync_count";
    public  static  String queryCountBio=  "SELECT * FROM (SELECT * FROM "+ PatientTable.TABLE_NAME+")p  ,   " +
            " (SELECT "+  FingerPrintTable.Column.patient_id+
            ", COUNT("+FingerPrintTable.Column.patient_id+")   as   " +fingerCount+
             ", SUM("+FingerPrintTable.Column.SyncStatus+")   as   " +syncCount+
            " FROM "+ FingerPrintTable.TABLE_NAME
            +" GROUP BY "+FingerPrintTable.Column.patient_id+")pi WHERE _id  = "+FingerPrintTable.Column.patient_id;

// recapture equivalent
public  static  String queryCountBioVerification=  "SELECT * FROM (SELECT * FROM "+ PatientTable.TABLE_NAME+")p  ,   " +
        " (SELECT "+  FingerPrintVerificationTable.Column.patient_id+
        ", COUNT("+FingerPrintVerificationTable.Column.patient_id+")   as   " +fingerCount+
        ", SUM("+FingerPrintVerificationTable.Column.SyncStatus+")   as   " +syncCount+
        " FROM "+ FingerPrintVerificationTable.TABLE_NAME
        +" GROUP BY "+FingerPrintVerificationTable.Column.patient_id+")pi WHERE _id  = "
        +FingerPrintVerificationTable.Column.patient_id;



    // Return List of patients with biometric finger_count and  sync_count
    // use in auto sync utility
    public List<Patient> getPatientWithPBS(){
        List<Patient> patientList = new LinkedList<>();
        SQLiteDatabase wdb=  new DBOpenHelper(OpenMRS.getInstance()).getReadableDatabase();
        final Cursor cursor =   wdb.rawQuery(queryCountBio, null);
        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {
                        patientList.add(cursorToPatientBio(cursor));
                }
            } finally {
                cursor.close();
            }
        }
      // fetch verification PBS
        final Cursor cursor2 =   wdb.rawQuery(queryCountBioVerification, null);
        if (null != cursor2) {
            try {
                while (cursor2.moveToNext()) {
                    patientList.add(cursorToPatientBio(cursor2));
                }
            } finally {
                cursor2.close();
            }
        }
        wdb.close();
        return patientList;
    }
// Return Map of  patients with biometric finger_count and  sync_count
    // patient ID as the key
    public Map<Long,Patient> getPatientWithPBS_(){
        Map<Long,Patient> patientMap = new HashMap<>();
        SQLiteDatabase wdb=  new DBOpenHelper(OpenMRS.getInstance()).getReadableDatabase();
        final Cursor cursor =   wdb.rawQuery(queryCountBio, null);
        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {
                    patientMap.put(cursor.getLong(cursor.getColumnIndex(PatientTable.Column.ID)),
                            cursorToPatientBio(cursor));
                }
            } finally {
                cursor.close();
            }
        }
        // recapture
        // if already exist replace, this is used for displaying   patient  that is not sync
        final Cursor cursor2 =   wdb.rawQuery(queryCountBioVerification, null);
        if (null != cursor2) {
            try {
                while (cursor2.moveToNext()) {
                    patientMap.put(cursor2.getLong(cursor.getColumnIndex(PatientTable.Column.ID)),
                            cursorToPatientBio(cursor2));
                }
            } finally {
                cursor2.close();
            }
        }

        wdb.close();
        return patientMap;
    }

    // Extend cursorToPatient with biometric count and sync count
    private Patient cursorToPatientBio(Cursor cursor ) {
        Patient patient = new Patient();

        // Modify field for patient as result of join
      patient.setFingerprintCount( cursor.getInt(cursor.getColumnIndex(fingerCount)));
      patient.setSyncFingerprintCount(cursor.getInt(cursor.getColumnIndex(syncCount)));


      //move on save as patient
        patient.setId(cursor.getLong(cursor.getColumnIndex(PatientTable.Column.ID)));
        patient.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.DISPLAY)));
        patient.setUuid(cursor.getString(cursor.getColumnIndex(PatientTable.Column.UUID)));
        patient.setEncounters(cursor.getString(cursor.getColumnIndex(PatientTable.Column.ENCOUNTERS)));

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER)));
        patientIdentifier.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_TYPE_HOSPITAL)));
        patient.getIdentifiers().add(patientIdentifier);

        PatientIdentifier patientIdentifierHts = new PatientIdentifier();
        patientIdentifierHts.setIdentifier(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_HTS)));
        patientIdentifierHts.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_TYPE_HTS)));
        patient.getIdentifiers().add(patientIdentifierHts);

        PatientIdentifier patientIdentifierHei = new PatientIdentifier();
        patientIdentifierHei.setIdentifier(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_HEI)));
        patientIdentifierHei.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_TYPE_HEI)));
        patient.getIdentifiers().add(patientIdentifierHei);

        PatientIdentifier patientIdentifierArt = new PatientIdentifier();
        patientIdentifierArt.setIdentifier(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_ART)));
        patientIdentifierArt.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_TYPE_ART)));
        patient.getIdentifiers().add(patientIdentifierArt);

        PatientIdentifier patientIdentifierAnc = new PatientIdentifier();
        patientIdentifierAnc.setIdentifier(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_ANC)));
        patientIdentifierAnc.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_TYPE_ANC)));
        patient.getIdentifiers().add(patientIdentifierAnc);

        PatientIdentifier patientIdentifierOpenmrs = new PatientIdentifier();
        patientIdentifierOpenmrs.setIdentifier(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_OPENMRS)));
        patientIdentifierOpenmrs.setDisplay(cursor.getString(cursor.getColumnIndex(PatientTable.Column.IDENTIFIER_TYPE_OPENMRS)));
        patient.getIdentifiers().add(patientIdentifierOpenmrs);

        if (cursor.getString(cursor.getColumnIndex(PatientTable.Column.PHONE_NUMBER)) != null) {
            PersonAttribute personAttribute = new PersonAttribute();
            personAttribute.setValue(cursor.getString(cursor.getColumnIndex(PatientTable.Column.PHONE_NUMBER)));
            PersonAttributeType personAttributeType = new PersonAttributeType();
            personAttributeType.setDisplay("Telephone Number");
            personAttributeType.setUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
            personAttribute.setAttributeType(personAttributeType);


            List<PersonAttribute> pAttributes = new ArrayList<>();
            pAttributes.add(personAttribute);
            patient.setAttributes(pAttributes);
        }

        PersonName personName = new PersonName();
        personName.setGivenName(cursor.getString(cursor.getColumnIndex(PatientTable.Column.GIVEN_NAME)));
        personName.setMiddleName(cursor.getString(cursor.getColumnIndex(PatientTable.Column.MIDDLE_NAME)));
        personName.setFamilyName(cursor.getString(cursor.getColumnIndex(PatientTable.Column.FAMILY_NAME)));
        patient.getNames().add(personName);

        patient.setGender(cursor.getString(cursor.getColumnIndex(PatientTable.Column.GENDER)));
        patient.setBirthdate(cursor.getString(cursor.getColumnIndex(PatientTable.Column.BIRTH_DATE)));
        byte[] photoByteArray = cursor.getBlob(cursor.getColumnIndex(PatientTable.Column.PHOTO));
        if (photoByteArray != null)
            patient.setPhoto(byteArrayToBitmap(photoByteArray));
        patient.getAddresses().add(cursorToAddress(cursor));

        return   patient;
    }




    private PersonAddress cursorToAddress(Cursor cursor) {
        int address1ColumnIndex = cursor.getColumnIndex(PatientTable.Column.ADDRESS_1);
        int address2ColumnIndex = cursor.getColumnIndex(PatientTable.Column.ADDRESS_2);
        int postalColumnIndex = cursor.getColumnIndex(PatientTable.Column.POSTAL_CODE);
        int countryColumnIndex = cursor.getColumnIndex(PatientTable.Column.COUNTRY);
        int stateColumnIndex = cursor.getColumnIndex(PatientTable.Column.STATE);
        int cityColumnIndex = cursor.getColumnIndex(PatientTable.Column.CITY);
        int latitudeColumnIndex = cursor.getColumnIndex(PatientTable.Column.LATITUDE);
        int longitudeColumnIndex = cursor.getColumnIndex(PatientTable.Column.LONGITUDE);

        PersonAddress personAddress = new PersonAddress();
        personAddress.setAddress1(cursor.getString(address1ColumnIndex));
        personAddress.setAddress2(cursor.getString(address2ColumnIndex));
        personAddress.setPostalCode(cursor.getString(postalColumnIndex));
        personAddress.setCountry( cursor.getString(countryColumnIndex));
        personAddress.setStateProvince(cursor.getString(stateColumnIndex));
        personAddress.setCityVillage(cursor.getString(cityColumnIndex));
        personAddress.setLatitude(cursor.getString(latitudeColumnIndex));
        personAddress.setLongitude(cursor.getString(longitudeColumnIndex));

        return personAddress;
    }
    private Bitmap byteArrayToBitmap(byte[] imageByteArray) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageByteArray);
        return BitmapFactory.decodeStream(inputStream);
    }

}
