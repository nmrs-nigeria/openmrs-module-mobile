package org.openmrs.mobile.sync;

import androidx.annotation.NonNull;

import com.activeandroid.query.Select;

import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.repository.LocationRepository;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.IdGenPatientIdentifiers;
import org.openmrs.mobile.models.IdentifierType;
import org.openmrs.mobile.models.Location;
import org.openmrs.mobile.models.Module;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PatientDto;
import org.openmrs.mobile.models.PatientIdentifier;
import org.openmrs.mobile.models.PatientPhoto;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.ModuleUtils;
import org.openmrs.mobile.utilities.PatientAndMatchesWrapper;
import org.openmrs.mobile.utilities.PatientAndMatchingPatients;
import org.openmrs.mobile.utilities.PatientComparator;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PatientSync {
    private RestApi restApi;

    public LogResponse getSyncResponse() {
        return logResponse;
    }

    private LogResponse logResponse;

    public PatientSync(RestApi restApi) {
        this.restApi = restApi;
    }

    private boolean calculatedLocally = false;

    protected  boolean syncPatient(@NonNull String identifier,@NonNull Patient patient,
                  PatientAndMatchesWrapper patientAndMatchesWrapper) {
       logResponse =  new LogResponse( identifier);
        try {
            if (!patient.isSynced()) {
                fetchSimilarPatients(patient, patientAndMatchesWrapper, logResponse);
            } else {
               new PatientRepository().syncPatient(patient, logResponse);
            }
        } catch (Exception e) {
 logResponse.appendLogs(e.getMessage() ,"","syncPatient ");
        }

 return calculatedLocally;
    }

    private void fetchSimilarPatients(final Patient patient, final PatientAndMatchesWrapper patientAndMatchesWrapper, LogResponse logResponse) throws IOException {
        RestApi restApi = RestServiceBuilder.createService(RestApi.class);
        Call<Results<Module>> moduleCall = restApi.getModules(ApplicationConstants.API.FULL);
        Response<Results<Module>> moduleResp = moduleCall.execute();
        if (moduleResp.isSuccessful()) {
            if (ModuleUtils.isRegistrationCore1_7orAbove(moduleResp.body().getResults())) {
                fetchSimilarPatientsFromServer(patient, patientAndMatchesWrapper, logResponse);
            } else {
                fetchPatientsAndCalculateLocally(patient, patientAndMatchesWrapper, logResponse);
            }
        } else {
            fetchPatientsAndCalculateLocally(patient, patientAndMatchesWrapper, logResponse);
        }
    }


    private void fetchPatientsAndCalculateLocally(Patient patient, PatientAndMatchesWrapper patientAndMatchesWrapper, LogResponse logResponse) throws IOException {
        calculatedLocally = true;
        RestApi restApi = RestServiceBuilder.createService(RestApi.class);
        Call<Results<Patient>> patientCall = restApi.getPatients(patient.getName().getGivenName(), ApplicationConstants.API.FULL);
        Response<Results<Patient>> resp = patientCall.execute();
        if (resp.isSuccessful()) {
            List<Patient> similarPatient = new PatientComparator().findSimilarPatient(resp.body().getResults(), patient);
            if (!similarPatient.isEmpty()) {
                patientAndMatchesWrapper.addToList(new PatientAndMatchingPatients(patient, similarPatient));
            logResponse.appendLogs("Found similar patient","","Bio->PatientsAndCalculateLocally");
            } else {
              new PatientRepository().syncPatient(patient, logResponse);
            }
        }
    }

    private void fetchSimilarPatientsFromServer(Patient patient, PatientAndMatchesWrapper patientAndMatchesWrapper, LogResponse logResponse) throws IOException {
        calculatedLocally = false;
        RestApi restApi = RestServiceBuilder.createService(RestApi.class);
        Call<Results<Patient>> patientCall = restApi.getSimilarPatients(patient.toMap());
        Response<Results<Patient>> patientsResp = patientCall.execute();

        if (patientsResp.isSuccessful()) {
            List<Patient> patientList = patientsResp.body().getResults();
            if (!patientList.isEmpty()) {
                List<Patient> similarPatient = new PatientComparator().findSimilarServePatient(patientList, patient);
                if (!similarPatient.isEmpty()) {
                    patientAndMatchesWrapper.addToList(new PatientAndMatchingPatients(patient, patientList));
                    logResponse.appendLogs("Similar patient found","","Bio->SimilarPatientsFromServer");
                } else {
                   new PatientRepository().syncPatient(patient, logResponse);
                }
            } else {
                new PatientRepository().syncPatient(patient, logResponse);
            }
        }
    }


    class PatientRepository {

        OpenMRS openMrs = OpenMRS.getInstance();

        private OpenMRSLogger logger;
        private PatientDAO patientDao;
        private LocationRepository locationRepository;
        private RestApi restApi;
        PatientIdentifier identifierHts;

        public PatientRepository() {
            this.logger = new OpenMRSLogger();
            this.patientDao = new PatientDAO();
            this.locationRepository = new LocationRepository();
            this.restApi = RestServiceBuilder.createService(RestApi.class);
        }

        public PatientRepository(OpenMRS openMRS, OpenMRSLogger logger, PatientDAO patientDao, RestApi restApi, LocationRepository locationRepository) {
            this.logger = logger;
            this.patientDao = patientDao;
            this.restApi = restApi;
            this.locationRepository = locationRepository;
            this.openMrs = openMRS;
        }

        /**
         * Sync Patient
         */


        private LogResponse syncPatient(final Patient patient, LogResponse logResponse) {
            try {
                    /*
                      AndroidDeferredManager dm = new AndroidDeferredManager();
                        dm.when( getLocationUuid(), getIdGenPatientIdentifier(), getPatientIdentifierTypeUuid())
                     .done(results ->
                 result index 0 location // result  index 1 IdGenPatientIdentifier   // result index 2  PatientIdentifierTypeUuid

            Calls to when with multiple arguments results in a Promise that signals fail on the first rejection or signals done with all computed values.

                    All these variable request from server must not be null;
                     */
                Location location = getLocation(logResponse);
                if (location == null) return logResponse;
                String mIdentifier = getIdGenPatientIdentifier(logResponse);
                if (mIdentifier == null) return logResponse;
                Results<IdentifierType> mIdentifierType = getPatientIdentifierTypeUuid(logResponse);
                if (mIdentifierType == null) return logResponse;
                // prepare content for sync
                if (location != null && mIdentifier != null && mIdentifierType!=null) {
                    {


                            final List<PatientIdentifier> identifiers = new ArrayList<>();
                            IdentifierType openmrsType = new IdentifierType();
                            List<PatientIdentifier> identifiersPatients = patient.getIdentifiers();
                        for (PatientIdentifier p : identifiersPatients) {
                                for (IdentifierType resultIdentifiertype : mIdentifierType.getResults()) {
                                    if (resultIdentifiertype.getDisplay().equals(p.getDisplay())) {
                                        final PatientIdentifier identifier = new PatientIdentifier();
                                        identifier.setLocation(location);
                                        identifier.setIdentifier(p.getIdentifier());
                                        identifier.setIdentifierType(resultIdentifiertype);
                                        identifiers.add(identifier);
                                    }
                                    if (resultIdentifiertype.getDisplay().equals("OpenMRS ID")) {
                                        openmrsType = resultIdentifiertype;
                                    }
                                }
                            }

                            final PatientIdentifier identifier = new PatientIdentifier();
                            identifier.setLocation(location);
                            identifier.setIdentifier(mIdentifier);
                            identifier.setIdentifierType(openmrsType);
                            identifiers.add(identifier);

                            patient.setIdentifiers(identifiers);
                            Call<PatientDto> call;
                            if (patient.getUuid().trim().isEmpty()) {
                                patient.setUuid(null);
                                PatientDto patientDto = patient.getPatientDto();
                                call = restApi.createPatient(patientDto);
                            } else {
                                PatientDto patientDto = patient.getPatientDto();
                                call = restApi.updatePatient(patientDto, patient.getUuid(), "full");
                               }


                        Response<PatientDto> res = call.execute();
                            if(res.isSuccessful()){
                                PatientDto newPatient = res.body();
                                        patient.setUuid(newPatient.getUuid());
                                        if (patient.getPhoto() != null)
                                            uploadPatientPhoto(patient);

                                        new PatientDAO().updatePatient(patient.getId(), patient);
                                        if (!patient.getEncounters().equals("")) {
                                            addEncounters(patient, logResponse);
                                        }
                                logResponse.setSuccess(true);

                            } else {
                                String err = "ErrorBody:" + res.errorBody().string() +
                                        "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                                logResponse.appendLogs(err, "Contact HI", "Sync bio - getPatientIdentifierTypeUuid");

                            }
                        }

            }

            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "Contact HI", "sync patient update biography");
            }

            return null;
        }

        private Response<PatientPhoto> uploadPatientPhoto(final Patient patient) throws IOException {
            PatientPhoto patientPhoto = new PatientPhoto();
            patientPhoto.setPhoto(patient.getPhoto());
            patientPhoto.setPerson(patient);
            Call<PatientPhoto> personPhotoCall =
                    restApi.uploadPatientPhoto(patient.getUuid(), patientPhoto);
            return personPhotoCall.execute();
        }

        /**
         * Update Patient
         */
        public LogResponse updatePatient(final Patient patient, String refPatient) {
            LogResponse logResponse = new LogResponse(refPatient);
                try {
                    /*
                      AndroidDeferredManager dm = new AndroidDeferredManager();
                        dm.when( getLocationUuid(), getIdGenPatientIdentifier(), getPatientIdentifierTypeUuid())
                     .done(results ->
                 result index 0 location // result  index 1 IdGenPatientIdentifier   // result index 2  PatientIdentifierTypeUuid

            Calls to when with multiple arguments results in a Promise that signals fail on the first rejection or signals done with all computed values.

                    All these variable request from server must not be null;
                     */
                    Location location = getLocation(logResponse);
                    if (location == null) return logResponse;
                    String mIdentifier = getIdGenPatientIdentifier(logResponse);
                    if (mIdentifier == null) return logResponse;
                    Results<IdentifierType> mIdentifierType = getPatientIdentifierTypeUuid(logResponse);
                    if (mIdentifierType == null) return logResponse;

                     // prepare content for sync
                    if (location != null && mIdentifier != null && mIdentifierType!=null) {
                        final List<PatientIdentifier> identifiers = new ArrayList<>();
                        IdentifierType openmrsType = new IdentifierType();
                        List<PatientIdentifier> identifiersPatients = patient.getIdentifiers();
                        boolean openmrs_code_exist = false;
                        for (PatientIdentifier pid : identifiersPatients) {
                            if (pid.getDisplay().equals("OpenMRS ID")) {
                                openmrs_code_exist = true;
                            }
                        }


                        for (PatientIdentifier p : identifiersPatients) {
                            for (IdentifierType resultIdentifiertype : mIdentifierType.getResults()) {
                                if (resultIdentifiertype.getDisplay().equals(p.getDisplay())) {
                                    final PatientIdentifier identifier = new PatientIdentifier();
                                    identifier.setLocation(location);
                                    identifier.setIdentifier(p.getIdentifier());
                                    identifier.setIdentifierType(resultIdentifiertype);
                                    identifiers.add(identifier);
                                    if (resultIdentifiertype.getDisplay().equals("HIV testing Id (Client Code)") || resultIdentifiertype.getDisplay().equals("ART Number") || resultIdentifiertype.getDisplay().equals("ANC Number")) {
                                        identifierHts = identifier;
                                        openmrs_code_exist = true;
                                    }
                                }
                                if (resultIdentifiertype.getDisplay().equals("OpenMRS ID")) {
                                    openmrsType = resultIdentifiertype;
                                }

                            }
                        }

                        final PatientIdentifier identifier = new PatientIdentifier();
                        identifier.setLocation(location);
                        identifier.setIdentifier(mIdentifier);
                        if (!openmrs_code_exist) {
                            identifier.setIdentifierType(openmrsType);
                            identifiers.add(identifier);
                            patient.setIdentifiers(identifiers);
                        }

                         PatientDto patientDto = patient.getPatientDto();
                        if (patient.getUuid() != null) {
                            Call<PatientDto> call = restApi.updatePatient(patientDto, patient.getUuid(), "full");
                            Response<PatientDto> res = call.execute();

                            if (res.isSuccessful()) {
                                PatientDto patientDtoNew = res.body();
                                patient.setBirthdate(patientDtoNew.getPerson().getBirthdate());
                                patient.setUuid(patientDtoNew.getUuid());
                                if (patient.getPhoto() != null)
                                    uploadPatientPhoto(patient);
                                // update local data
                                patientDao.updatePatient(patient.getId(), patient);

                                ToastUtil.success("Patient " + patient.getPerson().getName().getNameString()
                                        + " updated");
                            } else {
                                String err = "ErrorBody:" + res.errorBody().string() +
                                        "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                                logResponse.appendLogs(err, "Contact HI", "Sync bio - getPatientIdentifierTypeUuid");

                            }
                            if (identifierHts != null) {
                                Call<PatientDto> callIdentifier = restApi.updatePatientIdentifier(patient.getUuid(), identifierHts, "full");
                          try{
                            Response<PatientDto>   resIdentifier=  callIdentifier.execute();
                                        if(resIdentifier.isSuccessful()){
                                            ToastUtil.success("Patient new identifier added successfully");
                                        } else {
                                            String err = "ErrorBody:" + res.errorBody().string() +
                                                    "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                                            logResponse.appendLogs(err, "Contact HI", "Sync bio - getPatientIdentifierTypeUuid");

                                        }
                            } catch (Exception e) {
                                logResponse.appendLogs(e.getMessage(), "", "Sync bio - getPatientIdentifierTypeUuid");

                            }


                            }
                        }
                        {
                            syncPatient(patient, logResponse);
                        }


                    }


                    //
                } catch (Exception e) {
                    logResponse.appendLogs(e.getMessage(), "Contact HI", "sync patient update biography");
                }



           return logResponse;
        }


        private void addEncounters(Patient patient, LogResponse logResponse) {
            String enc = patient.getEncounters();
            List<Long> list = new ArrayList<>();
            for (String s : enc.split(","))
                list.add(Long.parseLong(s));
            Visit visit =null;
            for (long id : list) {
                Encountercreate encountercreate = new Select()
                        .from(Encountercreate.class)
                        .where("id = ?", id)
                        .executeSingle();
                if (encountercreate != null) {
                    encountercreate.setPatient(patient.getUuid());
                    encountercreate.save();
                   //  todo This syncing have been move
                    // added because of encouterdate for new patients who
               Visit v=  new EncounterSync().addEncounter(encountercreate, DateUtils.convertTime(System.currentTimeMillis(), DateUtils.OPEN_MRS_REQUEST_FORMAT), logResponse);
              if(v!=null){
                  visit =v;
              }
                }
            }
            if(visit!=null){
                 new EncounterSync().endVisit(visit, logResponse);
            }
        }




        private Location getLocation(@NonNull LogResponse logResponse) {
            Location location = null;
            RestApi apiService =
                    RestServiceBuilder.createService(RestApi.class);
            Call<Results<Location>> call = apiService.getLocations(null);

            try {
                Response<Results<Location>> res = call.execute();
                if (res.isSuccessful()) {
                    Results<Location> locationList = res.body();
                    if (locationList != null) {
                        int count = 0;
                        for (Location result : locationList.getResults()) {
                            if ((result.getDisplay().trim()).equalsIgnoreCase((openMrs.getLocation().trim()))) {
                                count++;
                                location = result;
                            }}
                            // validate if the location have unique name;
                            if (count > 1) {
                                location = null;
                                logResponse.appendLogs("Two identical location found", "Contact HIs", "Select sync location");
                            } else if (locationList.getResults().size() > 1 && count == 0) {
                                logResponse.appendLogs("Locations Available but found no ,match",
                                        "Make sure your syncing to same server you downloaded the patient", "Select sync location");
                            }
//                            else {
//                                // location search is fine. No need for log or success, since it is an intermediate request
//
//                            }

                    }
                } else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "Select sync location");

                }
            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "", "Select sync location");

            }
            return location;
        }

        private String getIdGenPatientIdentifier(@NonNull LogResponse logResponse) {

            RestApi apiService = RestServiceBuilder.createServiceForPatientIdentifier(RestApi.class);
            try {
                Call<IdGenPatientIdentifiers> call = apiService.getPatientIdentifiers(openMrs.getUsername(), openMrs.getPassword());

                Response<IdGenPatientIdentifiers> res = call.execute();
                if (res.isSuccessful()) {
                    IdGenPatientIdentifiers idList = res.body();
                    if (idList.getIdentifiers().size() > 0)
                        return idList.getIdentifiers().get(0);
                    else logResponse.appendLogs("Identifier size ", "Contact HI",
                            "sync bio getIdGenPatientIdentifier");

                }else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "sync bio getIdGenPatientIdentifier");

                }
            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "", "sync bio getIdGenPatientIdentifier");

            }


            return null;
        }


        private Results<IdentifierType> getPatientIdentifierTypeUuid(LogResponse logResponse) {
            Call<Results<IdentifierType>> call = restApi.getIdentifierTypes();
            try {
                Response<Results<IdentifierType>> res = call.execute();
                if (res.isSuccessful()) {
                    Results<IdentifierType> idresList = res.body();
                    return idresList;
                } else {
                    String err = "ErrorBody:" + res.errorBody().string() +
                            "  Message:" + res.message() + "  Code:" + res.code() + "  Body:" + res.body();
                    logResponse.appendLogs(err, "Contact HI", "Sync bio - getPatientIdentifierTypeUuid");

                }
            } catch (Exception e) {
                logResponse.appendLogs(e.getMessage(), "", "Sync bio - getPatientIdentifierTypeUuid");

            }

            return null;
        }


    }


}
