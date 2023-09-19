package org.openmrs.mobile.databases;

import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.FormResource;
import org.openmrs.mobile.utilities.FormService;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*
This class contains utility  functions for logging databases table

 */
public class Util {
    public static String TAG = "DATA_BASE";

    public static void log(String tag_string) {
        Log.d(TAG, tag_string);
    }

    public static void logQuery(String query) {
        Log.d(TAG, "Started");
        SQLiteDatabase wdb = new DBOpenHelper(OpenMRS.getInstance()).getReadableDatabase();
        logCursor(wdb.rawQuery(query, null)
                , "\t");
        wdb.close();
    }

    public static void getValidForms() {
        ArrayList<FormResource> formResourceList = new ArrayList<>();
        List<String> formName = new ArrayList<>();
        List<FormResource> allFormResourcesList = FormService.getFormResourceList();
        int c = 0;
        for (FormResource formResource : allFormResourcesList) {
            List<FormResource> valueRef = formResource.getResourceList();
            String valueRefString = null;
            c++;
            Util.log(c + " Form names: " + formResource.getName());
            for (FormResource resource : valueRef) {
                if (resource.getName().equals("json")) {
                    valueRefString = resource.getValueReference();
                } else {
                }
            }
            // Get the forms (REMEMBER to set default if there is issue - get reference from the old)
            if (!StringUtils.isBlank(valueRefString)) {
                formResourceList.add(formResource);
            } else {
                //Log.e(TAG, "Form is blank ");
            }
        }

        int size = formResourceList.size();
        Log.d(TAG, "allFormResourcesList.size()  " + allFormResourcesList.size() + "    Pure form Size " + size);
        for (int i = 0; i < size; i++) {
            ;
            // Log.d(TAG,formResourceList.get(i).getName());
        }
    }

    public static void logTable(String tableName) {
        Log.d(TAG, "Started");
        SQLiteDatabase wdb = new DBOpenHelper(OpenMRS.getInstance()).getReadableDatabase();
        logCursor(wdb.rawQuery("select * from " + tableName, null)
                , ",\t");
        wdb.close();
        Log.d(TAG, "ended");
    }

    public static void logCursor(Cursor c, String separator) {
        // print
        String res = "";
        for (int h = 0; h < c.getColumnCount(); h++) {
            res += c.getColumnName(h);
            res += separator;
        }
        Log.d(TAG, res);
        res = "";
        // print data
        for (int r = 0; r < c.getCount(); r++) {
            for (int col = 0; col < c.getColumnCount(); col++) {
                if (c.moveToPosition(r)) {
                    String re = c.getString(col);
                    if (null != re) {
                        res = res + re + separator;
                    } else
                        res += "Null" + separator;
                }
            }

            Log.d(TAG, res);
            res += "\n";
            res = "";
        }

        c.close();
    }

    public static void logEncounter(Encountercreate encounter) {
        Util.log("Name " + encounter.getFormname());
        Util.log("Patient" + encounter.getPatient());
        Util.log("Encounter Type " + encounter.getEncounterType());
        Util.log("UUID or form " + encounter.getFormUuid());
        Util.log("obs " + encounter.getObservations().toString());
        Util.log("encounter date " + encounter.getEncounterDatetime());
        Util.log("obs " + encounter.getObservations().toString());
        Util.log("Location " + encounter.getLocation());
        Util.log("obs " + encounter.getEncounterProviders().toString());
    }
}
