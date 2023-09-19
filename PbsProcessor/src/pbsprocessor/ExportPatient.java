package pbsprocessor;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pbsprocessor.api.retrofit.RestApi;
import pbsprocessor.api.retrofit.RestServiceBuilder;
import pbsprocessor.util.ApplicationConstants;
import pbsprocessor.util.HashMethods;
import pbsprocessor.util.OpenMRSCustomHandler;
import retrofit2.Call;
import retrofit2.Response;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;

public class ExportPatient {


    public static void main(String[] a) {
        exportWithPatientId("3632");
    }

    public static void exportWithPatientId(String commaSeparatedPatientId) {
        if (commaSeparatedPatientId != null && !commaSeparatedPatientId.isEmpty()) {
            DbConnector dbConnector = new DbConnector();

            try {
                Connection connection = dbConnector.dbDriver();
                String[] patientsIds = commaSeparatedPatientId.split(",");
                JSONArray outputJSON = new JSONArray();
                for (String id : patientsIds) {
                    id = id.trim();
                    String puuid = "";
                    JSONObject mainJson = new JSONObject();
                    PreparedStatement ps = connection.prepareStatement(pSql);
                    ps.setString(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            ResultSetMetaData rsmd = rs.getMetaData();
                            JSONObject patient = new JSONObject();
                            patient.put("familyName", rs.getString("familyName"));
                            patient.put("givenName", rs.getString("givenName"));
                            patient.put("middleName", rs.getString("middleName"));
                            patient.put("gender", rs.getString("gender"));
                            patient.put("birthdate", rs.getString("birthdate"));
                            patient.put("telephone", rs.getString("telephone"));
                            patient.put("address1", rs.getString("address1"));
                            patient.put("address2", rs.getString("address2"));
                            patient.put("cityVillage", rs.getString("cityVillage"));
                            patient.put("state", rs.getString("state"));
                            patient.put("postalCode", rs.getString("postalCode"));
                            patient.put("patientUuid", rs.getString("patientUuid"));
                            patient.put("artNumber", rs.getString("artNumber"));
                            patient.put("hospitalNumber", rs.getString("hospitalNumber"));
              //add patient bio
                            mainJson.put("patient", patient);
                            puuid = patient.get("patientUuid").toString();

                        }

                    } catch (Exception e) {
                        OpenMRSCustomHandler.writeLogToFile("Data Patient  : " + e.getMessage() + e);
                        System.out.println("Data Patient  : " + e);
                    }

                    //pbs
                    ps = connection.prepareStatement(pbsSql);
                    ps.setString(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        JSONArray pbsArray = new JSONArray();
                        ResultSetMetaData rsmd = rs.getMetaData();
                        while (rs.next()) {
                            JSONObject pbs = new JSONObject();
                            for (int col = 1; col < rsmd.getColumnCount(); col++) {
                                    pbs.put(rsmd.getColumnName(col), rs.getString(col));
                            }
                            Blob blob = rs.getBlob("template");
                            String templateString = new String(blob.getBytes(1, (int) blob.length()), StandardCharsets.UTF_8);
                            pbs.put("template", templateString);
                            pbsArray.add(pbs);
                        }
                        mainJson.put("pbs", pbsArray);
                    } catch (Exception e) {
                        OpenMRSCustomHandler.writeLogToFile("Data PBS  : " + e.getMessage() + e);
                        System.out.println("Data PBS  : " + e);
                    }
                    // add Fingerprint log

       /*             ps = connection.prepareStatement(sqlFingerLog);
                    ps.setString(1, id);
                    ps.setString(2, id);
                    ps.setString(3, id);
                    ps.setString(4, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        JSONObject pbsLog = new JSONObject();
                        if (rs.next()) {
                            Date date = new Date();
                            Long time = date.getTime();
                            pbsLog.put("time", time);
                            pbsLog.put("recapture_count", rs.getInt("recapture_count"));
                            pbsLog.put("last_capture_date", rs.getString("last_capture_date"));
                            mainJson.put("pbsLog", pbsLog);
                            mainJson.put("hash", HashMethods.getPBSHashTime(puuid, time, pbsLog.get("last_capture_date").toString()));

                        }
                    } catch (Exception e) {
                        OpenMRSCustomHandler.writeLogToFile("Data Long  : " + e.getMessage() + e);
                        System.out.println("Data Long  : " + e);
                    }
*/
                    //others network
                    try {
                        RestApi restApi = RestServiceBuilder.createService(RestApi.class);

                        try {
                            // Visit
                            Call<JSONObject> callVisit = restApi.findVisitsByPatientUUID(puuid, "custom:(uuid,location:ref,visitType:ref,startDatetime,stopDatetime:full)");
                            Response<JSONObject> visit = callVisit.execute();
                            if (visit.isSuccessful()) {
                                if (visit.body() != null) {
                                    mainJson.put("visit", visit.body().toString());

                                } else {
                                    System.out.println("Error1" + visit.message());
                                }
                            } else {
                                System.out.println("Error2 v " + visit.message() + visit.body() + visit.errorBody().string());
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        try {
                            //Encounter
                            Call<JSONObject> callEncounter = restApi.getLastVitals(puuid, ApplicationConstants.EncounterTypes.VITALS, "full", 1, "desc");
                            Response<JSONObject> encounter = callEncounter.execute();
                            if (encounter.isSuccessful()) {
                                if (encounter.body() != null) {
                                    mainJson.put("encounter", encounter.body().toJSONString());

                                } else {
                                    System.out.println("Error1 e" + encounter.message());
                                }
                            } else {
                                System.out.println("Error2 e " + encounter.message() + encounter.body() + encounter.errorBody().string());
                            }

                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    //  System.out.println(mainJson);
                    outputJSON.add(mainJson);

                }
                System.out.println();
                System.out.println();
                System.out.println("Exported " + outputJSON.size() + " patient, Import this patients before 24 hours." +
                        " This has an OPT for validation");
                OpenMRSCustomHandler.writeLogToFile("Exported " + outputJSON.size() + " patient, Import this patients before 24 hours." +
                        " This has an OPT for validation");
                OpenMRSCustomHandler.EXPORT_PATIENTS_TO_JSON(outputJSON.toJSONString());
                connection.close();
            } catch (Exception e) {
                OpenMRSCustomHandler.writeLogToFile("Data Exception: " + e.getMessage() + e);

            }
        } else {
            OpenMRSCustomHandler.writeLogToFile("Invalid IDs provided , IDs is null oe Empty");

        }

    }

    static String pbsSql =//"SELECT * FROM  biometricinfo WHERE patient_Id = ?;";
            "SELECT  bio.patient_id,   bio.imageWidth ,    bio.imageHeight ,   bio.imageDPI,    bio.imageQuality,    bio.fingerPosition  ,    bio.serialNumber,    bio.model,     bio.date_created, bio.creator,  bio.manufacturer,   IFNULL(bio.new_template,bio.template)  as  'template'   FROM  biometricinfo bio WHERE bio.patient_Id = ?;";
    static String pSql = "SELECT distinct pn.family_name as familyName, pn.given_name as givenName, pn.middle_name as middleName, p.gender as gender, p.birthdate as birthdate, 	ifnull( (select value from person_attribute as pt where pt.person_attribute_type_id = 8 and pt.person_id = pi.patient_id limit 1), 0 ) as telephone,  pa.address1 as address1, pa.address2 as address2, pa.city_village as cityVillage, pa.state_province as state, pa.postal_code as postalCode, p.uuid as patientUuid, (select identifier from patient_identifier as pi2 where pi2.identifier_type = 4 and pi2.patient_id = pi.patient_id limit 1) as artNumber, (select identifier from patient_identifier as pi2 where pi2.identifier_type = 5 and pi2.patient_id = pi.patient_id limit 1) as hospitalNumber, pi.patient_id FROM patient_identifier as pi right join person as p on p.person_id = pi.patient_id right join person_name as pn on pn.person_id = pi.patient_id right join person_address as pa on pa.person_id = pi.patient_id right join visit as vt on p.person_id = vt.patient_id where (identifier_type=4 or identifier_type=5) and p.voided = 0 and pi.patient_id =? order by vt.date_created desc; ";
    static String sqlFingerLog =
            "SELECT   GREATEST(ifnull( (SELECT date_created FROM biometricinfo WHERE patient_id =? LIMIT 1),'1999-08-22 00:00:00'), ifnull( (SELECT date_created FROM biometricverificationinfo WHERE patient_id =? LIMIT 1),'1999-08-22 00:00:00')) AS last_capture_date, ifnull((SELECT  recapture_count FROM biometricverificationinfo where  patient_id=? order by  recapture_count DESC limit 1),0) AS  recapture_count, FROM openmrs.person where person_id =?;";
}
