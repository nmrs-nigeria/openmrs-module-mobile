package org.openmrs.mobile.activities.addeditconsumption;

import androidx.annotation.NonNull;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientContract;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.repository.ConsumptionRepository;
import org.openmrs.mobile.api.repository.PatientRepository;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Module;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.PersonName;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.ModuleUtils;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.PatientComparator;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditConsumptionPresenter  extends BasePresenter implements AddEditConsumptionContract.Presenter {

    private final AddEditConsumptionContract.View mConsumptionInfoView;
    private ConsumptionRepository consumptionRepository;
    private RestApi restApi;
    private Consumption mConsumption;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringConsumption = false;
    public AddEditConsumptionPresenter(AddEditConsumptionContract.View mConsumptionInfoView,
                                   String consumptionToUpdateId) {
        this.mConsumptionInfoView = mConsumptionInfoView;
        this.mConsumptionInfoView.setPresenter(this);
        this.consumptionRepository = new ConsumptionRepository();
//        this.mCountries = countries;
//        this.patientToUpdateId = patientToUpdateId;
//        this.patientRepository = new PatientRepository();
//        this.restApi = RestServiceBuilder.createService(RestApi.class);
    }


    @Override
    public void subscribe() {
        // This method is intentionally empty
    }

    @Override
    public Consumption getConsumptionToUpdate() {
//        return new PatientDAO().findPatientByID(patientToUpdateId);
        return null;
    }



    @Override
    public void confirmRegister(Consumption consumption) {
        if (!isRegisteringConsumption() && validate(consumption)) {
            try {
                mConsumptionInfoView.setProgressBarVisibility(true);
                mConsumptionInfoView.hideSoftKeys();
                registeringConsumption= true;
                registerConsumption();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mConsumptionInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Consumption consumption) {
        if (!registeringConsumption&& validate(consumption)) {
            mConsumptionInfoView.setProgressBarVisibility(true);
            mConsumptionInfoView.hideSoftKeys();
            registeringConsumption = true;

//            new PatientDAO().updatePatient(consumption.getId(), consumption);
//            updatePatient(consumption);
        } else {
            mConsumptionInfoView.scrollToTop();
        }
    }

    @Override
    public void finishConsumptionInfoActivity() {
        mConsumptionInfoView.finishConsumptionInfoActivity();
    }

    private boolean validate (Consumption consumption) {

        boolean consumptionError = false;

        mConsumptionInfoView.setErrorsVisibility(consumptionError);



        // Validate gender
        if (StringUtils.isBlank(consumption.getConsumptionDate())) {
            consumptionError = true;
        }


        boolean result = !consumptionError;
        if (result) {
            mConsumption = consumption;
            return true;
        } else {
            mConsumptionInfoView.setErrorsVisibility(consumptionError);
            return false;
        }
    }

    @Override
    public void registerConsumption() {

        consumptionRepository.syncConsumption(mConsumption, new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {
                mConsumptionInfoView.startCommodityDashboardActivity();
                mConsumptionInfoView.finishConsumptionInfoActivity();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                registeringConsumption = false;
                mConsumptionInfoView.setProgressBarVisibility(false);
            }
        });
    }

    @Override
    public void updateConsumption(Consumption consumption) {
//        patientRepository.updatePatient(patient, new DefaultResponseCallbackListener() {
//            @Override
//            public void onResponse() {
//                mPatientInfoView.finishPatientInfoActivity();
//            }
//
//            @Override
//            public void onErrorResponse(String errorMessage) {
//                registeringPatient = false;
//                mPatientInfoView.setProgressBarVisibility(false);
//            }
//        });
    }
//
//    public void findSimilarPatients(final Patient patient) {
//        if (NetworkUtils.isOnline()) {
//            List<Patient> similarPatient = new PatientComparator().findSimilarPatient(new PatientDAO().getAllPatients().toBlocking().first(), patient);
//            if (!similarPatient.isEmpty()) {
//                mPatientInfoView.showSimilarPatientDialog(similarPatient, patient);
//            } else {
//                Call<Results<Module>> moduleCall = restApi.getModules(ApplicationConstants.API.FULL);
//                moduleCall.enqueue(new Callback<Results<Module>>() {
//                    @Override
//                    public void onResponse(@NonNull Call<Results<Module>> call, @NonNull Response<Results<Module>> response) {
//                        if (response.isSuccessful()) {
//                            if (ModuleUtils.isRegistrationCore1_7orAbove(response.body().getResults())) {
//                                fetchSimilarPatientsFromServer(patient);
//                            } else {
//                                fetchSimilarPatientAndCalculateLocally(patient);
//                            }
//                        } else {
//                            fetchSimilarPatientAndCalculateLocally(patient);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<Results<Module>> call, @NonNull Throwable t) {
//                        registeringPatient = false;
//                        mPatientInfoView.setProgressBarVisibility(false);
//                        ToastUtil.error(t.getMessage());
//                    }
//                });
//            }
//        } else {
//            List<Patient> similarPatient = new PatientComparator().findSimilarPatient(new PatientDAO().getAllPatients().toBlocking().first(), patient);
//            if (!similarPatient.isEmpty()) {
//                mPatientInfoView.showSimilarPatientDialog(similarPatient, patient);
//            } else {
//                registerPatient();
//            }
//        }
//    }

//    private void fetchSimilarPatientAndCalculateLocally(final Patient patient) {
//        Call<Results<Patient>> call = restApi.getPatients(patient.getName().getGivenName(), ApplicationConstants.API.FULL);
//        call.enqueue(new Callback<Results<Patient>>() {
//            @Override
//            public void onResponse(@NonNull Call<Results<Patient>> call, @NonNull Response<Results<Patient>> response) {
//                registeringPatient = false;
//                if (response.isSuccessful()) {
//                    List<Patient> patientList = response.body().getResults();
//                    if (!patientList.isEmpty()) {
//                        List<Patient> similarPatient = new PatientComparator().findSimilarPatient(patientList, patient);
//                        if (!similarPatient.isEmpty()) {
//                            mPatientInfoView.showSimilarPatientDialog(similarPatient, patient);
//                            mPatientInfoView.showUpgradeRegistrationModuleInfo();
//                        } else {
//                            registerPatient();
//                        }
//                    } else {
//                        registerPatient();
//                    }
//                } else {
//                    mPatientInfoView.setProgressBarVisibility(false);
//                    ToastUtil.error(response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<Results<Patient>> call, @NonNull Throwable t) {
//                registeringPatient = false;
//                mPatientInfoView.setProgressBarVisibility(false);
//                ToastUtil.error(t.getMessage());
//            }
//        });
//    }
//
//    private void fetchSimilarPatientsFromServer(final Patient patient) {
//        Call<Results<Patient>> call = restApi.getSimilarPatients(patient.toMap());
//        call.enqueue(new Callback<Results<Patient>>() {
//            @Override
//            public void onResponse(@NonNull Call<Results<Patient>> call, @NonNull Response<Results<Patient>> response) {
//                registeringPatient = false;
//                if (response.isSuccessful()) {
//                    List<Patient> similarPatients = response.body().getResults();
//                    if (!similarPatients.isEmpty()) {
//                        List<Patient> similarPatient = new PatientComparator().findSimilarServePatient(similarPatients, patient);
//                        if (!similarPatient.isEmpty()) {
//                            mPatientInfoView.showSimilarPatientDialog(similarPatients, patient);
//                        }else{
//                            registerPatient();
//                        }
//                    } else {
//                        registerPatient();
//                    }
//                } else {
//                    mPatientInfoView.setProgressBarVisibility(false);
//                    ToastUtil.error(response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<Results<Patient>> call, @NonNull Throwable t) {
//                registeringPatient = false;
//                mPatientInfoView.setProgressBarVisibility(false);
//                ToastUtil.error(t.getMessage());
//            }
//        });
//    }

    @Override
    public boolean isRegisteringConsumption() {
        return registeringConsumption;
    }

}

