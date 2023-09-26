package pbsprocessor;

import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pbsprocessor.api.SyncService;
import pbsprocessor.listerner.PatientBiometricSyncResponseModel;
import pbsprocessor.listerner.PbsServerContract;
import pbsprocessor.listerner.ProgressListener;
import pbsprocessor.model.PatientBiometricDTO;
import pbsprocessor.util.OpenMRSCustomHandler;
import retrofit2.Response;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ImportPBS {
  /*  public static void main(String[] a) {
      final int[] patientSize = new int[1];
        startUpload("C:\\Users\\AGBENGE\\Documents\\project_code\\openmrs-module-mobile\\PbsProcessor\\test_file\\PBS-NMRS-19-9-2023-1695133827.txt",
                new ProgressListener() {
                    @Override
                    public void onError(String errorMessage) {
                        System.out.println(errorMessage);
                    }

                    @Override
                    public void onProgress(int progress, int size, String displayMessage, String errorMessage) {
                        System.out.println("P "+progress+" M "+displayMessage+" E "+errorMessage);
                    }



                    @Override
                    public void stop() {
                        System.out.println("Stop");
                    }

                    @Override
                    public void onStart(int size) {
                        System.out.println("Size "+size);
                        patientSize[0] =size;
                    }

                    @Override
                    public void serverOkay(boolean isRunning) {
                        System.out.println("Server is running  "+isRunning);
                    }
                }
        );
    }
*/
    public   void startUpload(String path, ProgressListener progressListerner) {
        System.out.println("Started ");

        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject objectData = (JSONObject) jsonParser.parse(new FileReader(path));
            System.out.println("JSON read as object ");
            JSONObject globalObject= (JSONObject) objectData.get("global");
            JSONArray patientListJson = (JSONArray) objectData.get("data");
            System.out.println("JSON read as data and global var ");
            // check server is running

            SyncService syncService = new SyncService();
            try {
                System.out.println("Testing server ");
                Response<PbsServerContract> response = syncService.CheckServiceStatus();
                if (response.isSuccessful()) {
                    progressListerner.serverOkay(true);
                    System.out.println("Testing Okay ");
                } else {
                    System.out.println("Server fail ");
                    progressListerner.serverOkay(false);
                    progressListerner.stop();

                    return;
                }
            }catch (Exception e){
                System.out.println("Server Error "+e);
                progressListerner.onError(e.getMessage());
                progressListerner.serverOkay(false);
                 OpenMRSCustomHandler.writeLogToFile(e.getMessage());
                return;
            }
/*
Perform sending of data
 */
            //Parsing the contents of the JSON file
            final int size = patientListJson.size();
            progressListerner.onStart(size);
            for (int i = 0; i < patientListJson.size(); i++) {
                Gson gson = new Gson();
                System.out.println("Sending "+i); 
                JSONObject patientObject = (JSONObject) patientListJson.get(i);
                JSONObject patientPBS = (JSONObject) patientObject.get("pbs");
                if ((boolean) patientPBS.get("base")) {
                    System.out.println("Sending base "+i);
                    // sending base capture prints
                    PatientBiometricDTO dto =   gson.fromJson(patientPBS.get("templates").toString(),
                                    PatientBiometricDTO.class
                            );
                    //System.out.println(dto.getFingerPrintList().size());
                    Response<PatientBiometricSyncResponseModel> res =
                            syncService.startSync(dto, OpenMRS.getInstance().getUrlCapture());

                    if (res.isSuccessful()) {
                        // System.out.println("Capture sync successfull");
                        progressListerner.onProgress(i + 1, size, "Capture Successful", "");

                    } else {
                        String err = patientPBS.get("uuid").toString() + " Capture  unsuccessfully " + res.errorBody().string() +
                                "  " + res.message() + "  " + res.code() + "  " + res.body();
                        OpenMRSCustomHandler.writeLogToFile(err);

                        progressListerner.onProgress(i + 1, size, "Capture sync unsuccessful", err);

//                          System.out.println("Capture  unsuccessfully1 "+res.errorBody().string());
//                          System.out.println("Capture  unsuccessfully2 "+res.message());
//                          System.out.println("Capture  unsuccessfully3 "+res.code());
//                          System.out.println("Capture  unsuccessfully4 "+res.body());
                    }

                } else {
                    // send recapture prints
                    System.out.println("recapture "+i);
                    PatientBiometricDTO dto = gson.fromJson(patientPBS.get("templates").toString(),
                            PatientBiometricDTO.class
                    );
                    // System.out.println(dto.getFingerPrintList().size());
                    Response<PatientBiometricSyncResponseModel> res = syncService.startSync(dto,
                            OpenMRS.getInstance().getUrlRecature());

                    if (res.isSuccessful()) {
                        progressListerner.onProgress(i + 1, size, "Recapture successful", "");
                        //   System.out.println("ReCapture Successfully");

                    } else {
                        String err = patientPBS.get("uuid").toString() + "Recapture  unsuccessfully1 " + res.errorBody().string() +
                                "  " + res.message() + "  " + res.code() + "  " + res.body();
                        OpenMRSCustomHandler.writeLogToFile(err);

                        progressListerner.onProgress(i + 1, size, "Capture sync unsuccessful", err);

//                          System.out.println("ReCapture  unsuccessfully1 "+res.errorBody().string());
//                          System.out.println("ReCapture  unsuccessfully2 "+res.message());
//                          System.out.println("ReCapture  unsuccessfully3 "+res.code());
//                          System.out.println("ReCapture  unsuccessfully4 "+res.body());
                    }
                }
            }


        } catch (Exception e) {
            System.out.println("Error "+e.getMessage()+e);
            progressListerner.onError(e.getMessage());
            OpenMRSCustomHandler.writeLogToFile(e.getMessage());
        }finally {
            progressListerner.stop();
        }

    }

}