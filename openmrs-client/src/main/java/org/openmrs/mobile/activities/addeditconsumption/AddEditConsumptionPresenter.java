package org.openmrs.mobile.activities.addeditconsumption;

import android.util.Log;

import androidx.annotation.NonNull;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientContract;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.api.repository.ConsumptionRepository;
import org.openmrs.mobile.api.repository.PatientRepository;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
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
    private List<Consumption> multipleConsumptions;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringConsumption = false;
    private long consumptionToUpdateId;

    public AddEditConsumptionPresenter(AddEditConsumptionContract.View mConsumptionInfoView,
                                   long consumptionToUpdateId) {
        this.mConsumptionInfoView = mConsumptionInfoView;
        this.mConsumptionInfoView.setPresenter(this);
        this.consumptionRepository = new ConsumptionRepository();
        this.consumptionToUpdateId = consumptionToUpdateId;
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
        return  new Select().from(Consumption.class).where("id = ?", this.consumptionToUpdateId).executeSingle();
    }


    @Override
    public void confirmRegister(List<Consumption> consumption) {
        if (!isRegisteringConsumption()) {
            try {
                mConsumptionInfoView.setProgressBarVisibility(true);
                mConsumptionInfoView.hideSoftKeys();
                registeringConsumption= true;
                //declare the variable
                multipleConsumptions = consumption;
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
        if (!registeringConsumption && validate(consumption)) {
            mConsumptionInfoView.setProgressBarVisibility(true);
            mConsumptionInfoView.hideSoftKeys();
            registeringConsumption = true;
            //Save the updated consumption in the db
            consumption.save();
            //Then close the Activity
            finishConsumptionInfoActivity();
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


        // Validate consumption date
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
        for (Consumption consumption : multipleConsumptions){
            consumption.save();
            //OpenMRSCustomHandler.showJson(consumption);
        }
        finishConsumptionInfoActivity();
    }

    @Override
    public void updateConsumption(Consumption consumption) {

    }

    @Override
    public void deleteCommodity() {
        new Delete().from(Consumption.class).where("id = ?", this.consumptionToUpdateId).execute();
        finishConsumptionInfoActivity();
    }

    @Override
    public boolean isRegisteringConsumption() {
        return registeringConsumption;
    }

    public long getConsumptionToUpdateId(){
        return this.consumptionToUpdateId;
    }

}

