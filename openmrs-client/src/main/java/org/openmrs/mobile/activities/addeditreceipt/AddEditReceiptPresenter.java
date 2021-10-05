package org.openmrs.mobile.activities.addeditreceipt;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.addeditreceipt.AddEditReceiptContract;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.ReceiptRepository;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.List;

public class AddEditReceiptPresenter extends BasePresenter implements AddEditReceiptContract.Presenter {

    private final AddEditReceiptContract.View mReceiptInfoView;
    private ReceiptRepository receiptRepository;
    private RestApi restApi;
    private Receipt mReceipt;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringReceipt = false;
    public AddEditReceiptPresenter(AddEditReceiptContract.View mReceiptInfoView,
                                   String receiptToUpdateId) {
        this.mReceiptInfoView = mReceiptInfoView;
        this.mReceiptInfoView.setPresenter(this);
        this.receiptRepository = new ReceiptRepository();
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
    public Receipt getReceiptToUpdate() {
//        return new PatientDAO().findPatientByID(patientToUpdateId);
        return null;
    }



    @Override
    public void confirmRegister(Receipt receipt) {
        if (!isRegisteringReceipt() && validate(receipt)) {
            try {
                mReceiptInfoView.setProgressBarVisibility(true);
                mReceiptInfoView.hideSoftKeys();
                registeringReceipt= true;
                registerReceipt();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mReceiptInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Receipt receipt) {
        if (!registeringReceipt&& validate(receipt)) {
            mReceiptInfoView.setProgressBarVisibility(true);
            mReceiptInfoView.hideSoftKeys();
            registeringReceipt = true;

//            new PatientDAO().updatePatient(receipt.getId(), receipt);
//            updatePatient(receipt);
        } else {
            mReceiptInfoView.scrollToTop();
        }
    }

    @Override
    public void finishReceiptInfoActivity() {
        mReceiptInfoView.finishReceiptInfoActivity();
    }

    private boolean validate (Receipt receipt) {

        boolean receiptError = false;

        mReceiptInfoView.setErrorsVisibility(receiptError);



        // Validate gender
        if (StringUtils.isBlank(receipt.getOperationDate())) {
            receiptError = true;
        }


        boolean result = !receiptError;
        if (result) {
            mReceipt = receipt;
            return true;
        } else {
            mReceiptInfoView.setErrorsVisibility(receiptError);
            return false;
        }
    }

    @Override
    public void registerReceipt() {

        receiptRepository.syncReceipt(mReceipt, new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {
                mReceiptInfoView.startCommodityDashboardActivity();
                mReceiptInfoView.finishReceiptInfoActivity();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                registeringReceipt = false;
                mReceiptInfoView.setProgressBarVisibility(false);
            }
        });
    }

    @Override
    public void updateReceipt(Receipt receipt) {
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
    public boolean isRegisteringReceipt() {
        return registeringReceipt;
    }

}

