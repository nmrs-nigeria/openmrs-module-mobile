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

package org.openmrs.mobile.dao;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.activeandroid.query.Delete;

import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;

import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.tables.PatientTable;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PersonAddress;
import org.openmrs.mobile.models.PersonAttribute;
import org.openmrs.mobile.models.PersonAttributeType;
import org.openmrs.mobile.models.PersonName;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;

import static org.openmrs.mobile.databases.DBOpenHelper.createObservableIO;

public class PatientDAO {

    public Observable<Long> savePatient(Patient patient) {
        return createObservableIO(() -> new PatientTable().insert(patient));
    }

    public boolean updatePatient(long patientID, Patient patient) {
        return new PatientTable().update(patientID, patient) > 0;
    }

    public void deletePatient(long id) {
        OpenMRS.getInstance().getOpenMRSLogger().w("Patient deleted with id: " + id);
        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        openHelper.getReadableDatabase().delete(PatientTable.TABLE_NAME, PatientTable.Column.ID
                + " = " + id, null);
        // Delete its encounter too locally
        new Delete().from(Encountercreate.class).where("patientId = ?", id).execute();
    }

    public Observable<List<Patient>> getAllPatients() {
        return createObservableIO(() -> {
            List<Patient> patients = new ArrayList<>();
            DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            Cursor cursor = openHelper.getReadableDatabase().query(PatientTable.TABLE_NAME,
                    null, null, null, null, null, null);
            DatabaseUtils.dumpCursorToString(cursor);
            Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
            if (null != cursor) {
                try {
                    while (cursor.moveToNext()) {
                        Patient patient = cursorToPatient(cursor);
                        patients.add(patient);
                    }
                } finally {
                    cursor.close();
                }
            }
            return patients;
        });
    }


    public List<Patient> getAllPatientsLocal() {
            List<Patient> patients = new ArrayList<>();
            DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            Cursor cursor = openHelper.getReadableDatabase().query(PatientTable.TABLE_NAME,
                    null, null, null, null, null, null);

            if (null != cursor) {
                try {
                    while (cursor.moveToNext()) {
                        Patient patient = cursorToPatient(cursor);
                        patients.add(patient);
                    }
                } finally {
                    cursor.close();
                }
            }
            return patients;
    }

    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

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

        return patient;
    }

    public boolean isUserAlreadySaved(String uuid) {
        String where = String.format("%s = ?", PatientTable.Column.UUID);
        String[] whereArgs = new String[]{uuid};

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        final Cursor cursor = helper.getReadableDatabase().query(PatientTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        String patientUUID = "";
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    int uuidColumnIndex = cursor.getColumnIndex(PatientTable.Column.UUID);
                    patientUUID = cursor.getString(uuidColumnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return uuid.equalsIgnoreCase(patientUUID);
    }

    public boolean userDoesNotExist(String uuid) {
        return !isUserAlreadySaved(uuid);
    }

    public Patient findPatientByUUID(String uuid) {
        Patient patient = new Patient();
        String where = String.format("%s = ?", PatientTable.Column.UUID);
        String[] whereArgs = new String[]{uuid};

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        final Cursor cursor = helper.getReadableDatabase().query(PatientTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    patient = cursorToPatient(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return patient;
    }

    public List<Patient> getUnsyncedPatients(){
        List<Patient> patientList = new LinkedList<>();
        String where = String.format("%s = ? OR synced = 'false'", PatientTable.Column.SYNCED);
        String[] whereArgs = new String[]{"0"};

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        final Cursor cursor = helper.getReadableDatabase().query(PatientTable.TABLE_NAME, null , where, whereArgs, null, null, null);
        DatabaseUtils.dumpCursorToString(cursor);
        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {
                    Patient patient = cursorToPatient(cursor);
                    if(!patient.isSynced()){
                        patientList.add(patient);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return patientList;
    }

    public Patient findPatientByID(String id) {
        Patient patient = new Patient();
        String where = String.format("%s = ?", PatientTable.Column.ID);
        String[] whereArgs = new String[]{id};

        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        final Cursor cursor = helper.getReadableDatabase().query(PatientTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        DatabaseUtils.dumpCursorToString(cursor);
        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    patient = cursorToPatient(cursor);
                } else {
                    patient = null;
                }
            } finally {
                cursor.close();
            }
        }
        return patient;
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
