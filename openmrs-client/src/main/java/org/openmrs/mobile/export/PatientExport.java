package org.openmrs.mobile.export;

import androidx.annotation.NonNull;

import com.activeandroid.query.Select;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.openmrs.mobile.sync.LogResponse;
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

public class PatientExport {

    protected  LogResponse exportBioData(JSONObject patientObject ,Patient patient,
                                       @NonNull String identifier    ) {
          LogResponse logResponse = new LogResponse(identifier);
        try {
            // prepare content for
//                    final List<PatientIdentifier> identifiers = new ArrayList<>();
//                    IdentifierType openmrsType = new IdentifierType();
//                    List<PatientIdentifier> identifiersPatients = patient.getIdentifiers();
//                    for (PatientIdentifier p : identifiersPatients) {
//                        for (IdentifierType resultIdentifiertype : mIdentifierType.getResults()) {
//                            if (resultIdentifiertype.getDisplay().equals(p.getDisplay())) {
//                                final PatientIdentifier identifier = new PatientIdentifier();
//                                identifier.setLocation(location);
//                                identifier.setIdentifier(p.getIdentifier());
//                                identifier.setIdentifierType(resultIdentifiertype);
//                                identifiers.add(identifier);
//                            }
//                            if (resultIdentifiertype.getDisplay().equals("OpenMRS ID")) {
//                                openmrsType = resultIdentifiertype;
//                            }
//                        }
//                    }

                   // final PatientIdentifier identifier = new PatientIdentifier();
                   // identifier.setLocation(location);
                   // identifier.setIdentifier(mIdentifier);
                   // identifier.setIdentifierType(openmrsType);
                   // identifiers.add(identifier);

//                    patient.setIdentifiers(identifiers);
            PatientDto patientDto = patient.getPatientDto();
            Gson gson = new Gson();
           patientObject.put("bio_data",gson.toJson(patient));
            logResponse.appendLogs(false, "","","exportBioData");

        } catch (Exception e) {
            logResponse.appendLogs(false, e.getMessage(),"","exportBioData");
        }
        return  logResponse;

    }











}
