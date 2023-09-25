package org.openmrs.mobile.activities.pbs;

import static org.openmrs.mobile.utilities.ApplicationConstants.MINIMUM_REQUIRED_FINGERPRINT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;


import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationDTO;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsActivity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.databases.tables.FingerPrintTable;
import org.openmrs.mobile.export.FullExport;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.FingerPrintLog;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PersonAddress;
import org.openmrs.mobile.models.PersonAttribute;
import org.openmrs.mobile.models.PersonAttributeType;
import org.openmrs.mobile.models.PersonName;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.security.HashMethods;
import org.openmrs.mobile.sync.LogResponse;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FileExportUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import rx.Observable;
import rx.schedulers.Schedulers;

public class ExportPBS extends ACBaseActivity {

    File openMRSFolder;
    int requestCode = 1;
    private int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;
    private String mLattitude = null;
    private String mLongitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_pbs);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        openMRSFolder = new File(Environment.getExternalStorageDirectory() + "/NMRS-PBS");
        if (!openMRSFolder.exists()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            if (openMRSFolder.mkdir()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("PBS Folder created successfully.").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        Log.d("TAG_NAME", "Folder Created");
    }

    private void setSyncState(boolean b) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pbs_sync", b);
        editor.apply();
    }

    private boolean getSyncState() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Sync",
                Activity.MODE_PRIVATE);
        return sharedPref.getBoolean("pbs_sync", false);
    }

    //
    /*
    @generate_PBS
    the entry point of export. call from the UI
     */
    public void generate_PBS(View v) {
        if (!openMRSFolder.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("PBS Folder not found. Create the Folder before you proceed?").setCancelable(false).setPositiveButton("Yes create the folder", (dialog, id) -> {
                ActivityCompat.requestPermissions(ExportPBS.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                openMRSFolder.mkdir();
                finish();
            }).setNegativeButton("Cancel", (dialogInterface, i) -> finish());
            AlertDialog alert = builder.create();
            alert.show();
        } else {

            new FullExport(getApplicationContext(), openMRSFolder).starExportingPatients();

            if(true)  return;

            //for all unsync patients

            setSyncState(true);

            List<Patient> patientList = new PatientDAO().getAllPatientsLocal();

            JSONArray dataJson = new JSONArray();
            int succes = 0;
            int fail = 0;
            for (Patient patient : patientList) {
                LogResponse logResponse = exportPatient(patient.getId(), patient.getUuid(), dataJson, "PBS_" + patient.getUuid() + " id" + patient.getId());
                if (logResponse.isSuccess()) {
                    succes++;
                } else {
                    fail++;
                    OpenMRSCustomHandler.writeLogToFile(logResponse.getFullMessage());
                }
            }
            OpenMRSCustomHandler.writeLogToFile(new LogResponse(
                    fail < 1, "Summary", "Synced:"+succes+"\t Failed:"+fail+"\tTotal"+(fail+succes),
                    "If failed grater than one check the upper log for the reason", "Export").getFullMessage());


            setSyncState(false);

            if (dataJson.length() > 0) {
                try {
                    Date date = new Date();
                    // your date
                    // Choose time zone in which you want to interpret your Date
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Lagos"));
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    Long tsLong = System.currentTimeMillis() / 1000;
                    String timestamp = tsLong.toString();

                    //Generate the file name for the day
                    String fileName = "PBS-NMRS-" + day + "-" + month + "-" + year + "-" + timestamp + ".txt";
                    File fileCreated = new File(openMRSFolder + "/" + fileName);
                    FileOutputStream fileout = new FileOutputStream(fileCreated);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                    outputWriter.write(dataJson.toString());
                    outputWriter.flush();
                    outputWriter.close();
                    //display file saved message
                    Toast.makeText(getBaseContext(), "File saved successfully! as " + fileName, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    OpenMRSCustomHandler.writeLogToFile("Fail to export  " + e.getMessage());

                }
                Log.d("TAG_NAME", dataJson.toString());

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("There is no recent Data captured on this device. Please capture and export.").setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
                    //do things
                });
                AlertDialog alert = builder.create();
                alert.show();
            }


        }
    }


    // Sync a patient whom ID and UUID are available. Return true for non network error
    private LogResponse exportPatient(Long patientId, String patientUUID, JSONArray dataJson, String identifier) {
        LogResponse logResponse = new LogResponse(identifier);

        if (patientUUID != null && patientUUID != "") {
            // export user with UUID
            try {
                List<PatientBiometricContract> pbs = new FingerPrintDAO().getAll(false, patientId.toString());
                List<PatientBiometricVerificationContract> pbsVerification = new FingerPrintVerificationDAO().getAll(false, patientId.toString());

                if (pbs.size() == 0 && pbsVerification.size() == 0) {
                    return new LogResponse(
                            true,
                            identifier,
                            "No fingerprints",
                            "",
                            "PBS Sync"
                    );
                }
// make patient whom it print as not save not to sync
                List<PatientBiometricVerificationContract> confirm = new FingerPrintVerificationDAO().getSinglePatientPBS(patientId);
                if (confirm.size() != pbsVerification.size()) {
                    return new LogResponse(
                            true,
                            identifier,
                            "Undecided prints",
                            "Open this patient and confirm him as recapture or replacement for base",
                            "PBS recapture Sync"
                    );
                }
                //minimum prints not reached for both base and recapture
                if (pbs.size() < MINIMUM_REQUIRED_FINGERPRINT && pbsVerification.size() < MINIMUM_REQUIRED_FINGERPRINT) {
                    return new LogResponse(
                            false,
                            identifier,
                            "Minimum prints not reached",
                            "Capture more prints, do a recapture",
                            "PBS Sync"
                    );

                } else {

// base capture
                    if (pbs.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                        PatientBiometricDTO dto = new PatientBiometricDTO();
                        dto.setFingerPrintList(new ArrayList<>(pbs));
                        dto.setPatientUUID(patientUUID);

                        //set hashing
                        for (int bioIndex = 0; bioIndex < dto.getFingerPrintList().size(); bioIndex++) {
                            PatientBiometricContract b = dto.getFingerPrintList().get(bioIndex);
                            b.setModel(ApplicationConstants.PBS_PASSWORD_VERSION);
                            b.setManufacturer(HashMethods.getPBSHash(dto.getPatientUUID(),
                                    b.getDateCreated(),
                                    b.getImageQuality(),
                                    b.getSerialNumber(),
                                    b.getFingerPositions().toString()
                            ));
                            dto.getFingerPrintList().set(bioIndex, b);
                        }


                        // set values to capture
                        JSONObject jsonObject = new JSONObject();
                        Gson gson = new Gson();
                        jsonObject.put("uuid", patientUUID);
                        jsonObject.put("base", true);
                        jsonObject.put("templates", gson.toJson(dto));
                        dataJson.put(jsonObject);
                        return new LogResponse(
                                true,
                                identifier,
                                "Capture save to server successfully",
                                "",
                                "PBS Sync capture"
                        );


                    } else if (pbsVerification.size() >= MINIMUM_REQUIRED_FINGERPRINT) {
                        PatientBiometricVerificationDTO dto = new PatientBiometricVerificationDTO();
                        dto.setFingerPrintList(new ArrayList<>(pbsVerification));
                        dto.setPatientUUID(patientUUID);

                        //set hashing
                        for (int bioIndex = 0; bioIndex < dto.getFingerPrintList().size(); bioIndex++) {
                            PatientBiometricVerificationContract b = dto.getFingerPrintList().get(bioIndex);
                            b.setModel(ApplicationConstants.PBS_PASSWORD_VERSION);
                            b.setManufacturer(HashMethods.getPBSHash(dto.getPatientUUID(),
                                    b.getDateCreated(),
                                    b.getImageQuality(),
                                    b.getSerialNumber(),
                                    b.getFingerPositions().toString()
                            ));
                            dto.getFingerPrintList().set(bioIndex, b);
                        }


                        /// add verification
                        Gson gson = new Gson();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("uuid", patientUUID);
                        jsonObject.put("base", false);
                        jsonObject.put("templates", gson.toJson(dto));
                        dataJson.put(jsonObject);

                    } else {
                        return new LogResponse(
                                false,
                                identifier,
                                "No prints found",
                                "Report this error",
                                "PBS Sync recapture"
                        );
                    }
                }
            } catch (Exception e) {
                return new LogResponse(
                        false,
                        identifier,
                        e.getMessage(),
                        "Report this error",
                        "PBS Sync recapture"
                );
            }
        } else {
            return new LogResponse(
                    false,
                    identifier,
                    "No UUID",
                    "Try again",
                    "PBS Sync");
        }

        return logResponse;
    }

    /*
    This method set the prints as sync prints
     */
    public int updateFingerPrint(SQLiteDatabase db, String patient_id) {
        ContentValues newValues = new ContentValues();
        newValues.put(FingerPrintTable.Column.SyncStatus, 1);

        String[] whereArgs = new String[]{String.valueOf(patient_id)};

        String _where_clause = String.format("%s = ?", FingerPrintTable.Column.patient_id);
        return db.update(FingerPrintTable.TABLE_NAME, newValues, _where_clause, whereArgs);
    }

    public void updateSyncStatus() {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String query = "SELECT patient_id  FROM " + FingerPrintTable.TABLE_NAME + " WHERE syncStatus = 0 ";
        final Cursor cursor = helper.getReadableDatabase().rawQuery(query, null);

        DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        SQLiteDatabase db = openHelper.getWritableDatabase();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            this.updateFingerPrint(db, String.valueOf(cursor.getString(i)));
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
    }


    /*
    set the base pbs sync to 1 and  and completely remove the recapture prints
     */
    public void deleteFingerPrints(View view) {
        int total = countTemplateData();
        if (total > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to delete and clear all the FingerPrints on this Device?").setCancelable(false).setPositiveButton("Yes", (dialog, id) -> {
                updateSyncStatus();
                Toast.makeText(getBaseContext(), "Fingerprint Templates cleared successfully from the device.", Toast.LENGTH_LONG).show();
                new FingerPrintVerificationDAO().deleteAllPrints();
                finish();
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //do things
                    finish();
                }

            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("There are no Fingerprint saved on this device.").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private int countTemplateData() {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String query = "SELECT patient_id FROM " + FingerPrintTable.TABLE_NAME + " WHERE syncStatus = 0 ";
        final Cursor cursor = helper.getReadableDatabase().rawQuery(query, null);
        return cursor.getCount();
    }

    /*
    @importCSVFile entry point for getting patient into the mobile
    onclick of action button
     */
    public void importCSVFile(View v) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/*");
        i = Intent.createChooser(i, "Choose a JSON File");
        startActivityForResult(i, requestCode);
    }

    /*
    Ii
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            String csvFileSelected = FileExportUtil.getPath(this, uri);
            new MyTask().execute(csvFileSelected);


        }
    }

    private PatientBiometricContract createPatientBiometricContractFromJson(JSONObject p, LogResponse logResponse) {
        try {
            PatientBiometricContract print = new PatientBiometricContract();

            print.setImageWidth(p.getInt("imageWidth"));
            print.setTemplate(p.get("template").toString());
            print.setImageDPI(p.getInt("imageDPI"));
            print.setSerialNumber(p.getString("serialNumber"));
            print.setCreator(p.getInt("creator"));
//                 "patient_Id"))
//                    reader.skipValue();
            print.setDateCreated(p.getString("date_created"));
            print.setDateCreated(p.getString("dateCreated"));
            print.setFingerPositions(Enum.valueOf(FingerPositions.class, p.getString("fingerPosition")));
            print.setModel(p.getString("model"));
            print.setImageQuality(p.getInt("imageQuality"));
            print.setImageHeight(p.getInt("imageHeight"));
            print.setManufacturer(p.getString("manufacturer"));
            return print;
        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(), "Extraction Key tempered", "createPatientBiometricContractFromReader");
            logResponse.appendLogs(false, "Error: "+e.getMessage(),
                    "Extraction Key tempered", "createPatientBiometricContractFromReader");
        }

        return null;
    }

    private PatientBiometricContract createPatientBiometricContractFromReader(JsonReader reader, String idString) {
        try {
            PatientBiometricContract print = new PatientBiometricContract();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("imageWidth")) {
                    print.setImageWidth(reader.nextInt());
                } else if (name.equals("template")) {
                    print.setTemplate(reader.nextString());
                } else if (name.equals("imageDPI")) {
                    print.setImageDPI(reader.nextInt());
                } else if (name.equals("serialNumber")) {
                    print.setSerialNumber(reader.nextString());
                } else if (name.equals("patient_Id")) {
                    reader.skipValue();
                } else if (name.equals("date_created")) {
                    print.setDateCreated(reader.nextString());
                } else if (name.equals("fingerPositions")) {
                    print.setFingerPositions(Enum.valueOf(FingerPositions.class, reader.nextString()));
                } else if (name.equals("model")) {
                    print.setModel(reader.nextString());
                } else if (name.equals("imageQuality")) {
                    print.setImageQuality(reader.nextInt());
                } else if (name.equals("imageHeight")) {
                    print.setImageHeight(reader.nextInt());
                } else if (name.equals("manufacturer")) {
                    print.setManufacturer(reader.nextString());
                } else {
                    reader.skipValue(); // Skip unknown fields
                    throw new Exception(" Skipping " + name + ". Cannot Skip values of finger print.");

                }
            }
            reader.endObject();
            print.setSyncStatus(1);


            return print;
        } catch (Exception e) {
            OpenMRSCustomHandler.writeLogToFile(new LogResponse(false, idString, e.getMessage(), "Extraction Key tempered", "createPatientBiometricContractFromReader").getFullMessage());
        }

        return null;
    }

    private boolean confirmHash(String hash, Patient patient, FingerPrintLog fingerPrintLog, LogResponse logResponse) {
        if (hash == null || patient == null || fingerPrintLog == null) {

            logResponse.appendLogs(false,
                    "Hash or patient or log is NUll", "", "confirmHash");
            return false;
        } else {
            if (!hash.equals(HashMethods.getPBSHashTime(patient.getUuid(), fingerPrintLog.getTime(), fingerPrintLog.getLastCapturedDate()))) {
                logResponse.appendLogs(false,
                        "Hash not valid", "", "confirmHash");
                return false;
            }
            try {
                Date currentDate = new Date();
                long timeDifference = fingerPrintLog.getTime()//.getTime()4
                        - currentDate.getTime();
                // Calculate the number of days in the time difference
                long daysDifference = timeDifference / (24L * 60L * 60L * 1000L);
                if (daysDifference < 1) {
                    logResponse.appendLogs(true,
                            "Result", "", "confirmHash");
                    return true;
                } else {
                    logResponse.appendLogs(false,
                            "Data stay too long", "", "confirmHash");
                    return false;
                }
            } catch (Exception e) {
                logResponse.appendLogs(false,
                        e.getMessage(), "", "confirmHash");
                return false;
            }
        }

    }


    private FingerPrintLog getPBSLogFromJsonElement(JsonElement jsonElement, LogResponse logResponse) {
        try {

            Gson gson = new Gson();
            return gson.fromJson(jsonElement, FingerPrintLog.class);
        } catch (Exception e) {
            logResponse.appendLogs(false,
                    "parse Exception  " + e, "", "getPBSLogFromJsonElement");
        }
        return null;

    }

    // Convert string to PatientBiometricContract class
    private PatientBiometricContract createPatientBiometricContractFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, PatientBiometricContract.class);
    }

    private Visit createVisitFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Visit.class);
    }

    private Encounter createEncounterFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Encounter.class);
    }

    // Convert string to PatientBiometricContract List
    private List<PatientBiometricContract> getPatientPrintsFromJson(JsonElement jsonElement, LogResponse logResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonElement.toString());
            List<PatientBiometricContract> result = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PatientBiometricContract patientBiometric =
                        createPatientBiometricContractFromJson(jsonObject, logResponse);

                if(patientBiometric!=null) {
                    result.add(patientBiometric);
                }
            }
            return result;
        } catch (Exception r) {
            logResponse.appendLogs(false,
                    "parse Exception  " + r, "", "getPatientPrintsFromJson");
            return null;
        }
    }

    private List<Encounter> getPatientEncountersFromJsonElement(JsonElement jsonElement, LogResponse logResponse) {
        List<Encounter> result = new ArrayList<>();
        try {
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    Encounter encounter =
                            createEncounterFromJson(element.getAsString());
                    result.add(encounter);
                }
            }
        } catch (Exception r) {
            logResponse.appendLogs(false,
                    "parse Exception  " + r, "", "getPatientEncoutersFromJson");
            return null;
        }
        return result;
    }

    private List<Visit> getPatientVisitsFromJsonElemment(JsonElement jsonElement, LogResponse logResponse) {
        List<Visit> result = new ArrayList<>();
        try {
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    Visit visit =
                            createVisitFromJson(element.getAsString());
                    result.add(visit);
                }
            }
        } catch (Exception r) {
            logResponse.appendLogs(false,
                    "parse Exception  " + r, "", "getPatientPrintsFromJsonString");
            return null;
        }
        return result;
    }

    Patient getPatientElement(JsonElement jsonElement, LogResponse logResponse) {

        try {


            JSONObject patientJSON = new JSONObject(jsonElement.toString()); //jsonElement.getAsJsonObject();

            Patient patient = new Patient();
            PersonName personName = new PersonName();

            PersonAddress address = new PersonAddress();
            address.setAddress1(patientJSON.get("address1").toString());
            address.setAddress2(patientJSON.get("address2").toString());
            address.setCityVillage(patientJSON.get("cityVillage").toString());
            address.setCountry("Nigeria");
            address.setStateProvince(patientJSON.get("state").toString());
            address.setPostalCode(patientJSON.get("postalCode").toString());
            //If no latitude and longitude is selected then automatically generate one from the App and apend
            address.setLatitude(mLattitude);
            address.setLongitude(mLongitude);
            address.setPreferred(true);

            List<PersonAddress> addresses = new ArrayList<>();
            addresses.add(address);
            patient.setAddresses(addresses);


            personName.setFamilyName(patientJSON.get("familyName").toString());
            personName.setGivenName(patientJSON.get("givenName").toString());
            personName.setMiddleName(patientJSON.get("middleName").toString());

            List<PersonName> names = new ArrayList<>();
            names.add(personName);
            patient.setNames(names);

            PersonAttribute personAttribute = new PersonAttribute();
            PersonAttributeType personAttributeType = new PersonAttributeType();
            personAttributeType.setDisplay("Telephone Number");
            personAttributeType.setUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
            personAttribute.setAttributeType(personAttributeType);
            personAttribute.setValue(addTrailingZeroForPhoneNumber(patientJSON.get("telephone").toString()));

            List<PersonAttribute> pAttributes = new ArrayList<>();
            pAttributes.add(personAttribute);
            patient.setAttributes(pAttributes);
            List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
            final PatientIdentifier patientIdentifier = new PatientIdentifier();
            patientIdentifier.setIdentifier(patientJSON.get("hospitalNumber").toString());
            IdentifierType identifierType = new IdentifierType("Hospital Number");
            patientIdentifier.setDisplay("Hospital Number");
            patientIdentifier.setIdentifierType(identifierType);
            patientIdentifier.setUuid("fd0df06c-fcd4-4625-89b2-6b72ca44b8ed");


            identifiers.add(patientIdentifier);

            patient.setIdentifiers(identifiers);

            //set gender
            patient.setGender(patientJSON.get("gender").toString());

            patient.setBirthdate(patientJSON.get("birthdate").toString());
            patient.setUuid(patientJSON.get("patientUuid").toString());
            //Save patient here

            return patient;
        } catch (Exception e) {
            logResponse.appendLogs(false,
                    "Parse Exception   " + e.getMessage(), "Types", "getPatient ");
            return null;
        }
        // nextLine[] is an array of values from the line
        //Log.v("Baron", nextLine[0] + nextLine[1] + nextLine[2] + nextLine[3] + nextLine[4] + nextLine[5] + nextLine[6] + nextLine[7] + nextLine[8] + nextLine[9] + nextLine[10] + nextLine[11] + nextLine[12] +  "etc...");
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        android.location.Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            mLattitude = location.getLatitude() + "";
                            mLongitude = location.getLongitude() + "";
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            mLattitude = mLastLocation.getLatitude() + "";
            mLongitude = mLastLocation.getLongitude() + "";
        }
    };

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

    }

    @NonNull
    private String addTrailingZeroForPhoneNumber(String telephone) {
        if (telephone.length() >= 11) {
            return telephone;
        } else {
            String firstChar = telephone.substring(0, 1);
            if (firstChar.equals("0")) {
                return "0" + telephone;
            }
        }
        return telephone;
    }
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private class MyTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create and show the progress dialog
            progressDialog = new ProgressDialog(ExportPBS.this);
            progressDialog.setMessage("Processing...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            // Simulate a time-consuming task


            try {
                JsonParser jsonParser = new JsonParser();
                JsonReader reader = new JsonReader((new FileReader(params[0])));
                JsonArray jsonArray = jsonParser.parse(reader).getAsJsonArray();
                // Read elements from the JSON array
                progressDialog.setMax(jsonArray.size());
                for (int index = 0; index < jsonArray.size(); index++) {
                    publishProgress(index);
                    JsonObject jsonPatientData = jsonArray.get(index).getAsJsonObject();
                    LogResponse logResponse = new LogResponse("Import Patient[" + index + "]");
                    Patient patient = getPatientElement(jsonPatientData.get("patient"), logResponse);
                    List<PatientBiometricContract> pbs = getPatientPrintsFromJson(jsonPatientData.get("pbs"), logResponse);
                    //  FingerPrintLog fingerPrintLog = getPBSLogFromJsonElement(jsonPatientData.get("pbsLog"), logResponse);
                    // String hash = jsonPatientData.get("hash").getAsString();

                    //process the data
//                    if ( confirmHash(hash, patient, fingerPrintLog, logResponse)) {
//                        Util.log("Comfirm ");
                    // save data
                    if (patient != null && !patient.getUuid().isEmpty() &&
                            !patient.getName().getFamilyName().isEmpty() &&
                            !patient.getName().getGivenName().isEmpty() &&
                            !patient.getGender().isEmpty() &&
                            !patient.getBirthdate().isEmpty()
                    ) {
                        PatientDAO patientDAO = new PatientDAO();
                        Patient checkPatientExists = patientDAO.findPatientByUUID(patient.getUuid());
                        if (checkPatientExists.getUuid() == null) {
                            if (pbs != null) {
                                // change save template to true when the matcher is to me implemented
                                Long pid = new PatientDAO().insertPatientFully(patient, pbs, true);
                                if (pid > -1) {
                                    logResponse.setSuccess(true);
                                    // save the log
                                    //  fingerPrintLog.setPid(String.valueOf(pid));
                                    //  fingerPrintLog.save();
                               /*
                                    try {

                                        VisitDAO visitDAO = new VisitDAO();
                                        EncounterDAO encounterDAO = new EncounterDAO();
                                        // visit
                                        List<Visit> visits = getPatientVisitsFromJsonElemment(jsonPatientData.get("visit"), logResponse);
                                        if (visits != null) {
                                            Observable.just(visits)
                                                    .flatMap(Observable::from)
                                                    .forEach(visit ->
                                                                    visitDAO.saveOrUpdate(visit, pid)
                                                                            .observeOn(Schedulers.io())
                                                                            .subscribe(),
                                                            error -> error.printStackTrace()
                                                    );

                                        }
                                        //Encounters
                                        List<Encounter> encounters = getPatientEncountersFromJsonElement(jsonPatientData.get("encounter")
                                                , logResponse);
                                        if (encounters != null) {
                                            if (encounters.size() > 0)
                                                encounterDAO.saveLastVitalsEncounter(encounters.get(0), patient.getUuid());
                                        }

                                    } catch (Exception e) {
                                        logResponse.appendLogs(e.getMessage(), "", "Import Validation");

                                    }
                                    */
                                }
                            } else {
                                // erorr failled to insert this patient
                                logResponse.appendLogs(false, "Patient with UUID " + patient.getUuid() + " Failed to insert due to pbs json form not correct ", "", "Import Validation");


                            }

                            //Modifield Import  end
                        } else {
                            logResponse.appendLogs(false, "Patient with UUID (" + checkPatientExists.getUuid() + ") already exists on this device", "", "Import Validation");

                        }

                        //OpenMRSCustomHandler.showJson(patient);
                    } else {
                        logResponse.appendLogs(false, "Patient Not Valid ", "", "Import Validation");

                    }
                    //   }
//                    else {
//                        Util.log("fail" + fingerPrintLog.getRecapturedCount());
//                    }
                    OpenMRSCustomHandler.writeLogToFile(logResponse.getFullMessage());
                    // end of a patient data ----- loop next
                }

            } catch (Exception e) {
                e.printStackTrace();
                OpenMRSCustomHandler.writeLogToFile(new LogResponse(false, "All", e.getMessage(),
                        "Report", "Main Import").getFullMessage());

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Update the progress bar
            progressDialog.setProgress(values[0]);


        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            progressDialog.dismiss();

            Intent i = new Intent(getApplicationContext(), SyncedPatientsActivity.class);
            startActivity(i);
        }
    }
}

