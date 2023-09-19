package org.openmrs.mobile.activities.addeditdistribution;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionContract;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.DistributionRepository;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AddEditDistributionPresenter extends BasePresenter implements AddEditDistributionContract.Presenter {

    private final AddEditDistributionContract.View mDistributionInfoView;
    private DistributionRepository distributionRepository;
    private RestApi restApi;
    private Distribution mDistribution;
    private DistributionItem mDistributionItem;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringDistribution = false;
    private List<Distribution> multipleDistribution;
    private long distributionToUpdateId;

    public AddEditDistributionPresenter(AddEditDistributionContract.View mDistributionInfoView,
                                        long distributionToUpdateId) {
        this.mDistributionInfoView = mDistributionInfoView;
        this.mDistributionInfoView.setPresenter(this);
        this.distributionRepository = new DistributionRepository();
        this.distributionToUpdateId = distributionToUpdateId;
    }


    @Override
    public void subscribe() {
        // This method is intentionally empty
    }

    @Override
    public Distribution getDistributionToUpdate() {
        if(distributionToUpdateId == 0){
            return null;
        }else {
            Distribution distribution = new Select().from(Distribution.class).where("id = ?", this.distributionToUpdateId).executeSingle();
            //get the DistributionItem
            DistributionItem distributionItem = new Select().from(DistributionItem.class).where("distributionId = ?", this.distributionToUpdateId).executeSingle();
            List<DistributionItem> distributionItemClassArray = new ArrayList<DistributionItem>();
            distributionItemClassArray.add(distributionItem);
            distribution.setItems(distributionItemClassArray);
            return distribution;
        }
    }



    @Override
    public void confirmRegister(List<Distribution> distribution, DistributionItem distributionItem) {
        if (!isRegisteringDistribution()) {
            try {
                mDistributionInfoView.setProgressBarVisibility(true);
                mDistributionInfoView.hideSoftKeys();
                registeringDistribution= true;
                //declare the variable
                multipleDistribution = distribution;
                mDistributionItem = distributionItem;
                registerDistribution();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mDistributionInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Distribution distribution, DistributionItem distributionItem) {
        if (!registeringDistribution&& validate(distribution, distributionItem)) {
            mDistributionInfoView.setProgressBarVisibility(true);
            mDistributionInfoView.hideSoftKeys();
            registeringDistribution = true;

            //Save the distribution and consumption
            long lastid = distribution.save();
            new Delete().from(DistributionItem.class).where("distributionId = ?", lastid).execute();
            distributionItem.setDistributionId(lastid);
            distributionItem.save();
            finishDistributionInfoActivity();
            
        } else {
            mDistributionInfoView.scrollToTop();
        }
    }

    @Override
    public void finishDistributionInfoActivity() {
        mDistributionInfoView.finishDistributionInfoActivity();
    }

    private boolean validate (Distribution distribution, DistributionItem distributionItem) {

        boolean distributionError = false;

        mDistributionInfoView.setErrorsVisibility(distributionError);

        // Validate gender
        if (StringUtils.isBlank(distribution.getOperationDate())) {
            distributionError = true;
        }

        mDistributionItem = distributionItem;

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
        if(multipleDistribution.size() > 0) {
            for (Distribution distribution : multipleDistribution) {
                long lastInsertDistribution = distribution.save();
                saveDistributionItem(lastInsertDistribution, distribution.getItems());
            }
            mDistributionInfoView.setProgressBarVisibility(false);
            finishDistributionInfoActivity();
        }
    }

    private void saveDistributionItem(long distributionId, List<DistributionItem> dItem){
        for(DistributionItem distributionItem : dItem){
            distributionItem.setItem(distributionItem.getItem());
            distributionItem.setExpiration(distributionItem.getExpiration());
            distributionItem.setCalculatedExpiration(distributionItem.getCalculatedExpiration());
            distributionItem.setItemDrugType(distributionItem.getItemDrugType());
            distributionItem.setItemBatch(distributionItem.getItemBatch());
            distributionItem.setQuantity(distributionItem.getQuantity());
            distributionItem.setDistributionId(distributionId);
            distributionItem.save();
        }
    }

    @Override
    public void deleteCommodity() {
        new Delete().from(Distribution.class).where("id = ?", this.distributionToUpdateId).execute();
        new Delete().from(DistributionItem.class).where("distributionId = ?", this.distributionToUpdateId).execute();
        finishDistributionInfoActivity();
    }

    @Override
    public boolean isRegisteringDistribution() {
        return registeringDistribution;
    }

}

