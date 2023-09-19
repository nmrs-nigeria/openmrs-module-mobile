package org.openmrs.mobile.activities.addedittransfer;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.repository.TransferRepository;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AddEditTransferPresenter extends BasePresenter implements AddEditTransferContract.Presenter {

    private final AddEditTransferContract.View mTransferInfoView;
    private TransferRepository transferRepository;
    private RestApi restApi;
    private Transfer mTransfer;
    private TransferItem mTransferItem;
    private String patientToUpdateId;
    private List<String> mCountries;
    private boolean registeringTransfer = false;
    private List<Transfer> multipleTransfer;
    private long transferToUpdateId;

    public AddEditTransferPresenter(AddEditTransferContract.View mTransferInfoView,
                                    long transferToUpdateId) {
        this.mTransferInfoView = mTransferInfoView;
        this.mTransferInfoView.setPresenter(this);
        this.transferRepository = new TransferRepository();
        this.transferToUpdateId = transferToUpdateId;
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
    public Transfer getTransferToUpdate() {
        if(transferToUpdateId == 0){
            return null;
        }else {
            Transfer transfer = new Select().from(Transfer.class).where("id = ?", this.transferToUpdateId).executeSingle();
            //get the TransferItem
            TransferItem transferItem = new Select().from(TransferItem.class).where("transferId = ?", this.transferToUpdateId).executeSingle();
            List<TransferItem> transferItemClassArray = new ArrayList<TransferItem>();
            transferItemClassArray.add(transferItem);
            transfer.setItems(transferItemClassArray);
            return transfer;
        }
    }



    @Override
    public void confirmRegister(List<Transfer> transfer, TransferItem transferItem) {
        if (!isRegisteringTransfer()) {
            try {
                //mTransferInfoView.setProgressBarVisibility(true);
                mTransferInfoView.hideSoftKeys();
                registeringTransfer= true;
                //declare the variable
                multipleTransfer = transfer;
                mTransferItem = transferItem;
                registerTransfer();
            } catch (Exception e){
                ToastUtil.error(e.toString());
            }

        } else {
            mTransferInfoView.scrollToTop();
        }
    }

    @Override
    public void confirmUpdate(Transfer transfer, TransferItem transferItem) {
        if (!registeringTransfer&& validate(transfer, transferItem)) {
            mTransferInfoView.setProgressBarVisibility(true);
            mTransferInfoView.hideSoftKeys();
            registeringTransfer = true;

            //Save the transfer and consumption
            long lastid = transfer.save();
            new Delete().from(TransferItem.class).where("transferId = ?", lastid).execute();
            transferItem.setTransferId(lastid);
            transferItem.save();
            finishTransferInfoActivity();

        } else {
            mTransferInfoView.scrollToTop();
        }
    }

    @Override
    public void finishTransferInfoActivity() {
        mTransferInfoView.finishTransferInfoActivity();
    }

    private boolean validate (Transfer transfer, TransferItem transferItem) {

        boolean transferError = false;

        mTransferInfoView.setErrorsVisibility(transferError);



        // Validate gender
        if (StringUtils.isBlank(transfer.getOperationDate())) {
            transferError = true;
        }


        boolean result = !transferError;

        mTransferItem = transferItem;

        if (result) {
            mTransfer = transfer;
            return true;
        } else {
            mTransferInfoView.setErrorsVisibility(transferError);
            return false;
        }
    }

    @Override
    public void registerTransfer() {
        if(multipleTransfer.size() > 0) {
            for (Transfer transfer : multipleTransfer) {
                long lastInsertTransfer = transfer.save();
                saveTransferItem(lastInsertTransfer, transfer.getItems());
            }
            finishTransferInfoActivity();
        }
    }

    private void saveTransferItem(long transferId, List<TransferItem> tItem){
        for(TransferItem transferItem : tItem){
            transferItem.setItem(transferItem.getItem());
            transferItem.setExpiration(transferItem.getExpiration());
            transferItem.setCalculatedExpiration(transferItem.getCalculatedExpiration());
            transferItem.setItemDrugType(transferItem.getItemDrugType());
            transferItem.setItemBatch(transferItem.getItemBatch());
            transferItem.setQuantity(transferItem.getQuantity());
            transferItem.setTransferId(transferId);
            transferItem.save();
        }
    }

    @Override
    public void deleteCommodity() {
        new Delete().from(Transfer.class).where("id = ?", this.transferToUpdateId).execute();
        new Delete().from(TransferItem.class).where("transferId = ?", this.transferToUpdateId).execute();
        finishTransferInfoActivity();
    }

    @Override
    public boolean isRegisteringTransfer() {
        return registeringTransfer;
    }

}

