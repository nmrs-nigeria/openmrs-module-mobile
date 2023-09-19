package org.openmrs.mobile.activities.covidtbintegrator;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.CovidTBIntegratorRepository;
import org.openmrs.mobile.models.CovidTBIntegrator;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;


public class CovidTBIntegratorPresenter extends BasePresenter implements CovidTBIntegratorContract.Presenter {

    private final CovidTBIntegratorContract.View mCovidTBIntegratorInfoView;
    private CovidTBIntegratorRepository covidTBIntegratorRepository;
    private RestApi restApi;
    private CovidTBIntegrator mCovidTBIntegrator;
    private String patientToUpdateId;
    private boolean registeringCovidTBIntegrator = false;
    private List<CovidTBIntegrator> multipleCovidTBIntegrator;
    private long covidTBIntegratorToUpdateId;

    public CovidTBIntegratorPresenter(CovidTBIntegratorContract.View mCovidTBIntegratorInfoView,
                                      long covidTBIntegratorToUpdateId) {
        this.mCovidTBIntegratorInfoView = mCovidTBIntegratorInfoView;
        this.mCovidTBIntegratorInfoView.setPresenter(this);
        this.covidTBIntegratorRepository = new CovidTBIntegratorRepository();
        this.covidTBIntegratorToUpdateId = covidTBIntegratorToUpdateId;
    }


    @Override
    public void subscribe() {
        // This method is intentionally empty
    }

    @Override
    public CovidTBIntegrator getCovidTBIntegratorToUpdate() {
        if(covidTBIntegratorToUpdateId == 0){
            return null;
        }else {
            CovidTBIntegrator covidTBIntegrator = new Select().from(CovidTBIntegrator.class).where("id = ?", this.covidTBIntegratorToUpdateId).executeSingle();
            return covidTBIntegrator;
        }
    }



    @Override
    public void confirmRegister(List<CovidTBIntegrator> covidTBIntegrators) {
        if (!isRegisteringCovidTBIntegrator()) {
            try {
                //mCovidTBIntegratorInfoView.setProgressBarVisibility(true);
                mCovidTBIntegratorInfoView.hideSoftKeys();
                registeringCovidTBIntegrator= true;
                //declare the variable
                multipleCovidTBIntegrator = covidTBIntegrators;
                registerCovidTBIntegrator();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mCovidTBIntegratorInfoView.scrollToTop();
        }
    }


    @Override
    public void confirmUpdate(CovidTBIntegrator covidTBIntegrator) {
        if (!registeringCovidTBIntegrator && validate(covidTBIntegrator)) {
            mCovidTBIntegratorInfoView.setProgressBarVisibility(true);
            mCovidTBIntegratorInfoView.hideSoftKeys();
            registeringCovidTBIntegrator = true;

            //Save the covidTBIntegrator and consumption
            long lastid = covidTBIntegrator.save();
            finishCovidTBIntegratorInfoActivity();
        } else {
            mCovidTBIntegratorInfoView.scrollToTop();
        }
    }

    @Override
    public void finishCovidTBIntegratorInfoActivity() {
        mCovidTBIntegratorInfoView.finishCovidTBIntegratorInfoActivity();
    }

    private boolean validate (CovidTBIntegrator covidTBIntegrator) {

        boolean covidTBIntegratorError = false;

        mCovidTBIntegratorInfoView.setErrorsVisibility(covidTBIntegratorError);



        // Validate gender

        boolean result = !covidTBIntegratorError;
        if (result) {
           // mCovidTBIntegrator = covidTBIntegrator;
            return true;
        } else {
            mCovidTBIntegratorInfoView.setErrorsVisibility(covidTBIntegratorError);
            return false;
        }

    }

    @Override
    public void registerCovidTBIntegrator() {
        if(multipleCovidTBIntegrator.size() > 0) {
            for (CovidTBIntegrator covidTBIntegrator : multipleCovidTBIntegrator) {
                long lastInsertCovidTBIntegrator = covidTBIntegrator.save();
            }
            finishCovidTBIntegratorInfoActivity();
        }
    }

    private void saveCovidTBIntegratorItem(long covidTBIntegratorId){
//        for(CovidTBIntegratorItem covidTBIntegratorItem : aItem){
//            covidTBIntegratorItem.setItem(covidTBIntegratorItem.getItem());
//            covidTBIntegratorItem.setExpiration(covidTBIntegratorItem.getExpiration());
//            covidTBIntegratorItem.setCalculatedExpiration(covidTBIntegratorItem.getCalculatedExpiration());
//            covidTBIntegratorItem.setItemDrugType(covidTBIntegratorItem.getItemDrugType());
//            covidTBIntegratorItem.setItemBatch(covidTBIntegratorItem.getItemBatch());
//            covidTBIntegratorItem.setQuantity(covidTBIntegratorItem.getQuantity());
//            covidTBIntegratorItem.setCovidTBIntegratorId(covidTBIntegratorId);
//            covidTBIntegratorItem.save();
//        }
    }

    @Override
    public void deleteCovidTBIntegrator() {
        new Delete().from(CovidTBIntegrator.class).where("id = ?", this.covidTBIntegratorToUpdateId).execute();
        finishCovidTBIntegratorInfoActivity();
    }

    @Override
    public boolean isRegisteringCovidTBIntegrator() {
        return registeringCovidTBIntegrator;
    }


}

