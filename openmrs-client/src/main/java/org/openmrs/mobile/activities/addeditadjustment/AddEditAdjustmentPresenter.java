package org.openmrs.mobile.activities.addeditadjustment;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.AdjustmentRepository;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AddEditAdjustmentPresenter extends BasePresenter implements AddEditAdjustmentContract.Presenter {

    private final AddEditAdjustmentContract.View mAdjustmentInfoView;
    private AdjustmentRepository adjustmentRepository;
    private RestApi restApi;
    private Adjustment mAdjustment;
    private AdjustmentItem mAdjustmentItem;
    private String patientToUpdateId;
    private boolean registeringAdjustment = false;
    private List<Adjustment> multipleAdjustment;
    private long adjustmentToUpdateId;

    public AddEditAdjustmentPresenter(AddEditAdjustmentContract.View mAdjustmentInfoView,
                                      long adjustmentToUpdateId) {
        this.mAdjustmentInfoView = mAdjustmentInfoView;
        this.mAdjustmentInfoView.setPresenter(this);
        this.adjustmentRepository = new AdjustmentRepository();
        this.adjustmentToUpdateId = adjustmentToUpdateId;
    }


    @Override
    public void subscribe() {
        // This method is intentionally empty
    }

    @Override
    public Adjustment getAdjustmentToUpdate() {
        if(adjustmentToUpdateId == 0){
            return null;
        }else {
            Adjustment adjustment = new Select().from(Adjustment.class).where("id = ?", this.adjustmentToUpdateId).executeSingle();
            //get the AdjustmentItem
            AdjustmentItem adjustmentItem = new Select().from(AdjustmentItem.class).where("adjustmentId = ?", this.adjustmentToUpdateId).executeSingle();
            List<AdjustmentItem> adjustmentItemClassArray = new ArrayList<AdjustmentItem>();
            adjustmentItemClassArray.add(adjustmentItem);
            adjustment.setItems(adjustmentItemClassArray);
            return adjustment;
        }
    }



    @Override
    public void confirmRegister(List<Adjustment> adjustment, AdjustmentItem adjustmentItem) {
        if (!isRegisteringAdjustment()) {
            try {
                //mAdjustmentInfoView.setProgressBarVisibility(true);
                mAdjustmentInfoView.hideSoftKeys();
                registeringAdjustment= true;
                //declare the variable
                multipleAdjustment = adjustment;
                mAdjustmentItem = adjustmentItem;
                registerAdjustment();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mAdjustmentInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Adjustment adjustment, AdjustmentItem adjustmentItem) {
        if (!registeringAdjustment && validate(adjustment, adjustmentItem)) {
            mAdjustmentInfoView.setProgressBarVisibility(true);
            mAdjustmentInfoView.hideSoftKeys();
            registeringAdjustment = true;

            //Save the adjustment and consumption
            long lastid = adjustment.save();
            new Delete().from(AdjustmentItem.class).where("adjustmentId = ?", lastid).execute();
            adjustmentItem.setAdjustmentId(lastid);
            adjustmentItem.save();
            finishAdjustmentInfoActivity();

        } else {
            mAdjustmentInfoView.scrollToTop();
        }
    }

    @Override
    public void finishAdjustmentInfoActivity() {
        mAdjustmentInfoView.finishAdjustmentInfoActivity();
    }

    private boolean validate (Adjustment adjustment, AdjustmentItem adjustmentItem) {

        boolean adjustmentError = false;

        mAdjustmentInfoView.setErrorsVisibility(adjustmentError);



        // Validate gender
        if (StringUtils.isBlank(adjustment.getOperationDate())) {
            adjustmentError = true;
        }

        mAdjustmentItem = adjustmentItem;

        boolean result = !adjustmentError;
        if (result) {
            mAdjustment = adjustment;
            return true;
        } else {
            mAdjustmentInfoView.setErrorsVisibility(adjustmentError);
            return false;
        }

    }

    @Override
    public void registerAdjustment() {
        if(multipleAdjustment.size() > 0) {
            for (Adjustment adjustment : multipleAdjustment) {
                long lastInsertAdjustment = adjustment.save();
                saveAdjustmentItem(lastInsertAdjustment, adjustment.getItems());
            }
            finishAdjustmentInfoActivity();
        }
    }

    private void saveAdjustmentItem(long adjustmentId, List<AdjustmentItem> aItem){
        for(AdjustmentItem adjustmentItem : aItem){
            adjustmentItem.setItem(adjustmentItem.getItem());
            adjustmentItem.setExpiration(adjustmentItem.getExpiration());
            adjustmentItem.setCalculatedExpiration(adjustmentItem.getCalculatedExpiration());
            adjustmentItem.setItemDrugType(adjustmentItem.getItemDrugType());
            adjustmentItem.setItemBatch(adjustmentItem.getItemBatch());
            adjustmentItem.setQuantity(adjustmentItem.getQuantity());
            adjustmentItem.setAdjustmentId(adjustmentId);
            adjustmentItem.save();
        }
    }

    @Override
    public void deleteCommodity() {
        new Delete().from(Adjustment.class).where("id = ?", this.adjustmentToUpdateId).execute();
        new Delete().from(AdjustmentItem.class).where("adjustmentId = ?", this.adjustmentToUpdateId).execute();
        finishAdjustmentInfoActivity();
    }

    @Override
    public boolean isRegisteringAdjustment() {
        return registeringAdjustment;
    }


}

