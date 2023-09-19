package org.openmrs.mobile.activities.addeditreceipt;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.ReceiptRepository;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AddEditReceiptPresenter extends BasePresenter implements AddEditReceiptContract.Presenter {

    private final AddEditReceiptContract.View mReceiptInfoView;
    private ReceiptRepository receiptRepository;
    private RestApi restApi;
    private Receipt mReceipt;
    private ReceiptItem mReceiptItem;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringReceipt = false;
    private List<Receipt> multipleReceipt;
    private long receiptToUpdateId;

    public AddEditReceiptPresenter(AddEditReceiptContract.View mReceiptInfoView,
                                   long receiptToUpdateId) {
        this.mReceiptInfoView = mReceiptInfoView;
        this.mReceiptInfoView.setPresenter(this);
        this.receiptRepository = new ReceiptRepository();
        this.receiptToUpdateId = receiptToUpdateId;
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
        if(receiptToUpdateId == 0){
            return null;
        }else {
            Receipt receipt = new Select().from(Receipt.class).where("id = ?", this.receiptToUpdateId).executeSingle();
            //get the ReceiptItem
            ReceiptItem receiptItem = new Select().from(ReceiptItem.class).where("receiptId = ?", this.receiptToUpdateId).executeSingle();
            List<ReceiptItem> receiptItemClassArray = new ArrayList<ReceiptItem>();
            receiptItemClassArray.add(receiptItem);
            receipt.setItems(receiptItemClassArray);
            return receipt;
        }
    }



    @Override
    public void confirmRegister(List<Receipt> receipt, ReceiptItem receiptItem) {
        if (!isRegisteringReceipt()) {
            try {
                mReceiptInfoView.setProgressBarVisibility(true);
                mReceiptInfoView.hideSoftKeys();
                registeringReceipt= true;
                //declare the variable
                multipleReceipt = receipt;
                mReceiptItem = receiptItem;
                registerReceipt();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mReceiptInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Receipt receipt, ReceiptItem receiptItem) {
        if (!registeringReceipt&& validate(receipt, receiptItem)) {
            mReceiptInfoView.setProgressBarVisibility(true);
            mReceiptInfoView.hideSoftKeys();
            registeringReceipt = true;
            //Save the receipt and consumption
            long lastid = receipt.save();
            new Delete().from(ReceiptItem.class).where("receiptId = ?", lastid).execute();
            receiptItem.setReceiptId(lastid);
            receiptItem.save();
            finishReceiptInfoActivity();
        } else {
            mReceiptInfoView.scrollToTop();
        }
    }

    @Override
    public void finishReceiptInfoActivity() {
        mReceiptInfoView.finishReceiptInfoActivity();
    }

    private boolean validate (Receipt receipt, ReceiptItem receiptItem) {
        //OpenMRSCustomHandler.showJson(receiptItem);
        OpenMRSCustomHandler.showJson(receipt);
        boolean receiptError = false;

        mReceiptInfoView.setErrorsVisibility(receiptError);


        if (StringUtils.isBlank(receipt.getOperationDate())) {
            receiptError = true;
        }

        boolean result = !receiptError;

        mReceiptItem = receiptItem;

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
        if(multipleReceipt.size() > 0) {
            for (Receipt receipt : multipleReceipt) {
                long lastInsertReceipt = receipt.save();
                //Save the Receipt item separately
                saveReceiptItem(lastInsertReceipt, receipt.getItems());
            }
            finishReceiptInfoActivity();
        }
    }

    private void saveReceiptItem(long receiptId, List<ReceiptItem> rItem){
        for(ReceiptItem receiptItem : rItem){
            receiptItem.setItem(receiptItem.getItem());
            receiptItem.setExpiration(receiptItem.getExpiration());
            receiptItem.setCalculatedExpiration(receiptItem.getCalculatedExpiration());
            receiptItem.setItemDrugType(receiptItem.getItemDrugType());
            receiptItem.setItemBatch(receiptItem.getItemBatch());
            receiptItem.setQuantity(receiptItem.getQuantity());
            receiptItem.setReceiptId(receiptId);
            receiptItem.save();
        }
    }

    @Override
    public void deleteCommodity() {
        new Delete().from(Receipt.class).where("id = ?", this.receiptToUpdateId).execute();
        new Delete().from(ReceiptItem.class).where("receiptId = ?", this.receiptToUpdateId).execute();
        finishReceiptInfoActivity();
    }


    @Override
    public boolean isRegisteringReceipt() {
        return registeringReceipt;
    }

}

