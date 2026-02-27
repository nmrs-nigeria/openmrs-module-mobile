package org.openmrs.mobile.utilities;

import static org.openmrs.mobile.utilities.VersioningUtil.compareVersions;
import static org.openmrs.mobile.utilities.VersioningUtil.extractVersion;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;

import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.response.PbsServerContract;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.FormResource;
import org.openmrs.mobile.models.Module;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.sync.LogResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FormVersionUtil {
    private final RestApi apiService = RestServiceBuilder.createService(RestApi.class);

    String mPbsVersion = "unknown";
    String mMetadataVersion = "Unknown";

    /* This method should not be run on the UI thread
     It returns true when all are valid else false with the errors appended
    */
    public LogResponse validateDependencies() {

        if (NetworkUtils.isOnline()) {
            LogResponse logResponse = new LogResponse("Load_resource_online");
            logResponse.setSuccess(true);//

               //Save the current modules
               try {
                    Call<Results<Module>> callModule = apiService.getModules(ApplicationConstants.API.FULL);
                    Response<Results<Module>> response = callModule.execute();
                    if (response.isSuccessful()) {
                        boolean moduleFound = false;
                        for (Module module : response.body().getResults()) {
                            if (ApplicationConstants.UserKeys.METADATA_VERSION_UUID.equals(module.getUuid())) {
                                OpenMRS.getInstance().setMetadataVersion(module.getVersion() + "/" + module.getStarted());
                                moduleFound = true;
                                break;
                            }
                        }
                        if (!moduleFound) {
                            logResponse.appendLogs(false, "Metadata not found", "Check web instance",
                                    "Load metadata|loadResourse");
                        }
                    } else {
                        logResponse.appendLogs(false, "Metadata Request fail. Response: " + response.message(), "Check network",
                                "Load metadata|loadResourse");
                    }
                } catch (Exception e){
                   logResponse.appendLogs(false, "Metadata Request fail. Exceptions: " + e.getMessage(), "Check if the server is running or Off your network completely to cache ",
                           "Load metadata|loadResourse");
               }

               //Save the pbs
               try {
                    String[] baseUrl = OpenMRS.getInstance().getServerUrl().split(":");
                    String url = baseUrl[0] + "://" + baseUrl[1].replaceAll("//", "") + ":2018/server";
                    Call<PbsServerContract> callPBSserver = apiService.checkServerStatus(url);
                    Response<PbsServerContract> response = callPBSserver.execute();
                    if (response.isSuccessful()) {
                        OpenMRS.getInstance().setPBSserverVersion(response.body().getAppVersion() + "/" + DateUtils.getCurrentDateTime());
                    } else {
                        logResponse.appendLogs(false, "PBS Request fail. Response: " + response.message(), "Check if the nmrs-biometric is running and the PC network port 2018 is open",
                                "Load PBS services|loadResourse");
                    }
                }catch (Exception e){
                       logResponse.appendLogs(false, "PBS Request fail. Exceptions: " + e.getMessage(), "Check if the nmrs-biometric is running and the PC network port 2018 is open",
                               "Load PBS services|loadResourse");
               }

                //  check if the version are up to date.
                if (logResponse.isSuccess()) {
                    return validateDependenciesFromStore();
                } else {
                    // This result must be false.
                    OpenMRSCustomHandler.writeLogToFile(logResponse.getFullMessage());
                    return new LogResponse(false, "loadResourse", "Loading dependencies failed. ", "Check logs for more details");
                }

        } else {
            return validateDependenciesFromStore();
        }

    }

    //Retrieve from store and valid  and call validate when not empty
    private LogResponse validateDependenciesFromStore() {
        String notStartedError = null;
        if (ApplicationConstants.EMPTY_STRING.equals(OpenMRS.getInstance().getMetadataVersion())) {
            notStartedError = "Start web instance.";
        }
        if (ApplicationConstants.EMPTY_STRING.equals(OpenMRS.getInstance().getPBSserverVersion())) {
            notStartedError = notStartedError == null ? "Start Biometric service."
                    : notStartedError + " and Start Biometric service.\nMake sure "+
                    OpenMRS.getInstance().getServerUrl()+" is valid";
        }
        if (notStartedError != null) {
            return new LogResponse(false, "validateDependenciesFromStore", notStartedError,
                    "Check log for more detail"
            );
        }
        LogResponse logResponseMetadata = extractMetadataVersion();
        LogResponse logResponsePbs = extractPbsServiceVersion();
        if (logResponsePbs.isSuccess() && logResponseMetadata.isSuccess()) {
            return new LogResponse(true, "validateDependenciesFromStore", "All dependencies are valid",
                    "Check setting to see versions");
        } else {
            String pbsServiceVersion = "Expected Biometric service version " + ApplicationConstants.CURRENT_PBS_SERVICE_VERSION + ".\n";
            String metadataServiceVersion = "Expected Metadata service version " + ApplicationConstants.CURRENT_METADATA_VERSION + ".\n";
            if (!logResponsePbs.isSuccess()) {
                OpenMRSCustomHandler.writeLogToFile(logResponsePbs.getFullMessage());
            }
            if (!logResponseMetadata.isSuccess()) {
                OpenMRSCustomHandler.writeLogToFile(logResponseMetadata.getFullMessage());
            }
            return new LogResponse(false, "validateDependenciesFromStore",
                    (logResponseMetadata.isSuccess() ? "Metadata is okay.\n\n" : metadataServiceVersion) +
                            (logResponsePbs.isSuccess() ? "Biometric service okay." : pbsServiceVersion),
                    "Check log for more detail."
            );

        }

    }


    //Extract and check if pbs version is valid using  a method.
    private LogResponse extractPbsServiceVersion() {
        String pbsValues = OpenMRS.getInstance().getPBSserverVersion();
        String version = "Unknown";
        String time = "Unknown";

        try {
            String[] arrayValues = pbsValues.split("/");
            version = arrayValues[0];
            time = arrayValues[1];
        } catch (Exception e) {
            return new LogResponse(false, "extractPbsServiceVersion", "Extraction failed " + e.getMessage());
        }
        mPbsVersion = version;
        return pbsVersionOkay(version);
    }

    // extract and check if the services is started  the call  metadata is okay
    private LogResponse extractMetadataVersion() {
        String metadataValues = OpenMRS.getInstance().getMetadataVersion();
        boolean started = false;
        String version = "Unknown";

        try {
            String[] arrayValues = metadataValues.split("/");
            version = arrayValues[0];
            if (arrayValues.length > 1) {
                if ("true".equals(arrayValues[1])) {
                    started = true;
                }
            }
        } catch (Exception e) {
            return new LogResponse(false, " extractMetadataVersion", "Extraction failed " + e.getMessage());

        }
        mMetadataVersion = version;
        if (started) {
            return metadataVersionOkay(version);
        } else {
            return new LogResponse(false, " extractMetadataVersion", "Metadata not started on web instance", "Start metadata module on the web");


        }

    }

    // validate the passed string for pbs version is okay to be use with the const store in the mobile
    private LogResponse pbsVersionOkay(String serverVersion) {
        LogResponse logResponse = new LogResponse("pbsVersionOkay");
        if (!ApplicationConstants.CURRENT_PBS_SERVICE_VERSION.equals(serverVersion)) {
            // Extract version numbers
            List<Integer> currentPbsVersion = extractVersion(ApplicationConstants.CURRENT_PBS_SERVICE_VERSION);
            List<Integer> serverPbsVersion = extractVersion(serverVersion);
            // Compare versions
            int result = compareVersions(currentPbsVersion, serverPbsVersion);
            if (result < 0) {
                logResponse.appendLogs(true, "Mobile PBS version " + ApplicationConstants.CURRENT_PBS_SERVICE_VERSION + " is lower than service instance " + serverVersion, "Report issue", "");
            } else if (result > 0) {
                logResponse.appendLogs(false, "Mobile PBS version " + ApplicationConstants.CURRENT_PBS_SERVICE_VERSION + " is higher than service instance " + serverVersion, "Update PBS service ", "");
            } else {
                logResponse.appendLogs(true, "Mobile PBS version  is the same  numbers " + ApplicationConstants.CURRENT_PBS_SERVICE_VERSION, "Report issue", "");

            }
        } else {
            logResponse.appendLogs(true, "Mobile PBS version  is the same  text " + ApplicationConstants.CURRENT_PBS_SERVICE_VERSION, "Report issue", "");
        }
        return logResponse;
    }

    // validate the passed string for metadata is okay to be use with the const store in the mobile
    private LogResponse metadataVersionOkay(String serverVersion) {
        LogResponse logResponse = new LogResponse("metadataVersionOkay");
        if (!ApplicationConstants.CURRENT_METADATA_VERSION.equals(serverVersion)) {
            // Extract version numbers
            List<Integer> currentMetadataVersion = extractVersion(ApplicationConstants.CURRENT_METADATA_VERSION);
            List<Integer> serverMetadataVersion = extractVersion(serverVersion);
            // Compare versions
            int result = compareVersions(currentMetadataVersion, serverMetadataVersion);
            if (result < 0) {  //  
                logResponse.appendLogs(true, "Mobile Metadata version " + ApplicationConstants.CURRENT_METADATA_VERSION + " is lower than web instance " + serverVersion, "Report issue", "");
            } else if (result > 0) {
                logResponse.appendLogs(false, "Mobile Metadata version " + ApplicationConstants.CURRENT_METADATA_VERSION + " is higher than  web instance" + serverVersion, "Update metadata", "");

            } else {
                logResponse.appendLogs(true, "Metadata version is the same  numbers " + ApplicationConstants.CURRENT_METADATA_VERSION, "Report issue", "");

            }
        } else {
            logResponse.appendLogs(true, "Metadata version is the same  text " + ApplicationConstants.CURRENT_METADATA_VERSION, "Report issue", "");
        }

        return logResponse;
    }


 public LogResponse metadataOkayOffline(){
     if (ApplicationConstants.EMPTY_STRING.equals(OpenMRS.getInstance().getMetadataVersion())) {
         return new LogResponse(false, "metadataOkayOffline", "No cache data found",
                 "Close App and stop again"  );   }
     return extractMetadataVersion();
 }
   private List<FormResource> formresourcelist;
/* This method should not be run on the UI thread*/
    public LogResponse loadFormsFromServer() {
        LogResponse logResponse = new LogResponse("Load_forms_online");
        logResponse.setSuccess(true);//
        // Load the actual form resourse
        if (NetworkUtils.isOnline()) {
            try {
                Call<Results<FormResource>> call = apiService.getForms();
                Response<Results<FormResource>> response = call.execute();
                if (response.isSuccessful()) {
                    new Delete().from(FormResource.class).execute();
                    formresourcelist = response.body().getResults();

                    int size = formresourcelist.size();
                    ActiveAndroid.beginTransaction();
                    try {
                        for (int i = 0; i < size; i++) {
                            formresourcelist.get(i).setResourcelist();
                            formresourcelist.get(i).save();
                        }
                        ActiveAndroid.setTransactionSuccessful();

                    } catch (Exception e) {
                        logResponse.appendLogs(false, "Save forms error " + e.getMessage(), "Restart app",
                                "Load Forms|loadFormsUtil");
                    } finally {
                        ActiveAndroid.endTransaction();
                    }

                } else {
                    logResponse.appendLogs(false, "Load forms error " + response.message(), "Check Network",
                            "Load Forms|loadFormsUtil");

                }
            } catch (Exception e) {
                logResponse.appendLogs(false, "Load forms error " + e.getMessage(), "Check Network",
                        "Load Forms|loadFormsUtil");
            }
            try {
                Call<Results<EncounterType>> call2 = apiService.getEncounterTypes();
                Response<Results<EncounterType>> response = call2.execute();
                if (response.isSuccessful()) {
                    new Delete().from(EncounterType.class).execute();
                    Results<EncounterType> encountertypelist = response.body();
                    for (EncounterType enctype : encountertypelist.getResults())
                        enctype.save();
                    Util.log("FORM_LIST_SERVICE:  Forms type loaded");

                } else {
                    logResponse.appendLogs(false, "Load Forms type error " + response.message(), "Check Network",
                            "Forms type|loadFormsUtil");

                }
            } catch (Exception e) {
                logResponse.appendLogs(false, "Load Forms type error " + e.getMessage(), "Check Network",
                        "Forms type|loadFormsUtil");
            }
            return logResponse;
        }else{
            logResponse.appendLogs(false, "Offline", "turn online","Dialog load forms");
        }
        return logResponse;
    }
}
