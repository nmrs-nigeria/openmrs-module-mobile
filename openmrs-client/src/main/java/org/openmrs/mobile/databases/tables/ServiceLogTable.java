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

package org.openmrs.mobile.databases.tables;

import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.models.ServiceLog;

public class ServiceLogTable extends Table<ServiceLog> {
    public static final String TABLE_NAME = "service_logs";

    /**
     * Number of columns without ID column
     * use as a param to
     *
     * @see Table#values(int)
     */
    private static final int INSERT_COLUMNS_COUNT = 6;

    @Override
    public String createTableDefinition() {
        return CREATE_TABLE + TABLE_NAME + "("
                + Column.PATIENT_ID + Column.Type.INT_TYPE_NOT_NULL
                + Column.PATIENT_UUID + Column.Type.TEXT_TYPE_NOT_NULL
                + Column.VOIDED + Column.Type.INT_TYPE_NOT_NULL
                + Column.DATE_CREATED + Column.Type.DATE_TYPE_NOT_NULL
                + Column.VISIT_DATE + Column.Type.DATE_TYPE_NOT_NULL
                + Column.FORM_NAME + Column.Type.TEXT_TYPE
                + ");";
    }

    @Override
    public String insertIntoTableDefinition() {
        return INSERT_INTO + TABLE_NAME + "("
                + Column.PATIENT_ID + Column.COMMA
                + Column.PATIENT_UUID + Column.COMMA
                + Column.VOIDED + Column.COMMA
                + Column.DATE_CREATED + Column.COMMA
                + Column.VISIT_DATE + Column.COMMA
                + Column.FORM_NAME + ")"
                + values(INSERT_COLUMNS_COUNT);
    }

    @Override
    public String dropTableDefinition() {
        return DROP_TABLE_IF_EXISTS + TABLE_NAME;
    }

    @Override
    public Long insert(ServiceLog tableObject) {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        return helper.insertServiceLog(helper.getWritableDatabase(), tableObject);
    }

    //Set voided status to true (1) for all records associated to the current patient UUID
    public int set_patient_void_status_on_PBS_completion(String patientId, String patientUUID, int voided) {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        return helper.voidPatientServiceLogs(helper.getWritableDatabase(),patientId, patientUUID,voided );
    }
    @Override
    public int update(long tableObjectID, ServiceLog tableObject) {
        return 0;
    }

    @Override
    public void delete(long tableObjectID) {
//         DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
//         openHelper.getWritableDatabase().delete(TABLE_NAME, MasterColumn.ID + MasterColumn.EQUALS + tableObjectID, null);
    }

//    public void deleteByPatientUUID(String patientUUID) {
//        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
//        openHelper.getWritableDatabase().delete(TABLE_NAME, Column.PATIENT_UUID + MasterColumn.EQUALS + patientUUID, null);
//    }


    public class Column extends MasterColumn {
        public static final String PATIENT_ID = "patient_id"; //
        public static final String PATIENT_UUID = "puuid"; //
        public static final String FORM_NAME = "form_name";
        public static final String VISIT_DATE = "visit_date";// selected date by the user during filling the form
        public static final String DATE_CREATED = "date_created";  // actual date the form was inputted
        public static final String VOIDED = "voided"; // '0' for a new visit that finger print recapture is not yet done
    }

    @Override
    public String toString() {
        return TABLE_NAME + createTableDefinition();
    }
}
