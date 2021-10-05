package org.openmrs.mobile.activities.addeditdistribution;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionContract;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.DistributionRepository;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.List;

public class AddEditDistributionPresenter extends BasePresenter implements AddEditDistributionContract.Presenter {

    private final AddEditDistributionContract.View mDistributionInfoView;
    private DistributionRepository distributionRepository;
    private RestApi restApi;
    private Distribution mDistribution;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringDistribution = false;
    public AddEditDistributionPresenter(AddEditDistributionContract.View mDistributionInfoView,
                                        String distributionToUpdateId) {
        this.mDistributionInfoView = mDistributionInfoView;
        this.mDistributionInfoView.setPresenter(this);
        this.distributionRepository = new DistributionRepository();
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
    public Distribution getDistributionToUpdate() {
//        return new PatientDAO().findPatientByID(patientToUpdateId);
        return null;
    }



    @Override
    public void confirmRegister(Distribution distribution) {
        if (!isRegisteringDistribution() && validate(distribution)) {
            try {
                mDistributionInfoView.setProgressBarVisibility(true);
                mDistributionInfoView.hideSoftKeys();
                registeringDistribution= true;
                registerDistribution();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mDistributionInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Distribution distribution) {
        if (!registeringDistribution&& validate(distribution)) {
            mDistributionInfoView.setProgressBarVisibility(true);
            mDistributionInfoView.hideSoftKeys();
            registeringDistribution = true;

//            new PatientDAO().updatePatient(distribution.getId(), distribution);
//            updatePatient(distribution);
        } else {
            mDistributionInfoView.scrollToTop();
        }
    }

    @Override
    public void finishDistributionInfoActivity() {
        mDistributionInfoView.finishDistributionInfoActivity();
    }

    private boolean validate (Distribution distribution) {

        boolean distributionError = false;

        mDistributionInfoView.setErrorsVisibility(distributionError);



        // Validate gender
        if (StringUtils.isBlank(distribution.getOperationDate())) {
            distributionError = true;
        }


        boolean result = !distributionError;
        if (result) {
            mDistribution = distribution;
            return true;
        } else {
            mDistributionInfoView.setErrorsVisibility(distributionError);
            return false;
        }
    }

    @Override
    public void registerDistribution() {

        distributionRepository.syncDistribution(mDistribution, new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {
                mDistributionInfoView.startCommodityDashboardActivity();
                mDistributionInfoView.finishDistributionInfoActivity();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                registeringDistribution = false;
                mDistributionInfoView.setProgressBarVisibility(false);
            }
        });
    }

    @Override
    public void updateDistribution(Distribution distribution) {
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
    public boolean isRegisteringDistribution() {
        return registeringDistribution;
    }

}

