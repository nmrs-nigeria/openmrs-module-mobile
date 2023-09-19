package org.openmrs.mobile.api;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.activeandroid.query.Update;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.openmrs.mobile.api.repository.AdjustmentRepository;
import org.openmrs.mobile.api.repository.ConsumptionRepository;
import org.openmrs.mobile.api.repository.DistributionRepository;
import org.openmrs.mobile.api.repository.InventoryStockRepository;
import org.openmrs.mobile.api.repository.ReceiptRepository;
import org.openmrs.mobile.api.repository.TransferRepository;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.CommodityDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;
import org.openmrs.mobile.utilities.NetworkUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Modifier.STATIC;
import static java.lang.reflect.Modifier.TRANSIENT;

public class CommodityService extends IntentService implements CustomApiCallback {

    private final RestApi apiService = RestServiceBuilder.createService(RestApi.class);
    private RestApi restApi;

    /***
     * Let us leave out this class for now since it is not called from the service intent
     *
     * **/
    public CommodityService() {
        super("Sync Commodity");
        restApi = RestServiceBuilder.createService(RestApi.class);
    }

    public void addConsumption(final Consumption consumption, @Nullable DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            new ConsumptionRepository().syncConsumption(consumption, callbackListener);
        } else {
            ToastUtil.warning("No internet connection. Please reconnect and try again ");
        }
    }

    /**
     * The role of this function is to sync all the commodity available on the Sqlite database.
     * It pulls out all data from the sqlite and pushes them over the restApi to the server
     */
    public void syncCommodity() {
        syncConsumption();
        syncReceipt();
        syncDistribution();
        syncTransfer();
        syncAdjustment();

        //getInventoryStockSummary();
    }

    //This function only targets syncing of the consumption
    public void syncConsumption() {
        ConsumptionRepository consumptionRepository = new ConsumptionRepository();
        Consumption consumption = new Consumption();
        CommodityDAO commodityDAO = new CommodityDAO();
        //get all conasumption data from the Consumption DAO
        List<Consumption> allConsumption = commodityDAO.getAllConsumptionData();
        //Create a for loop and send to the Rest Api to push to the server
        for (Consumption row : allConsumption) {
            consumption.setConsumptionDate(row.getConsumptionDate());
            consumption.setItem(row.getItem());
            consumption.setDepartment(row.getDepartment());
            consumption.setQuantity(row.getQuantity());
            consumption.setWastage(row.getWastage());
            consumption.setBatchNumber(row.getBatchNumber());
            consumption.setTestPurpose(row.getTestPurpose());
            consumption.setDepartment(row.getDepartment());
            consumption.setName(row.getName());
            consumption.setRetired(row.getRetired());
            consumption.setDataSystem(row.getDataSystem());

            consumptionRepository.syncConsumption(consumption, new DefaultResponseCallbackListener() {
                @Override
                public void onResponse() {
                    new Update(Consumption.class)
                            .set("isSynced = 1")
                            .where("id = ?", row.getId())
                            .execute();
                    //consumption.setSynced(true);
                    //consumption.save();
                    //mConsumptionInfoView.startCommodityDashboardActivity();
                    //mConsumptionInfoView.finishConsumptionInfoActivity();
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    //registeringConsumption = false;
                    //mConsumptionInfoView.setProgressBarVisibility(false);
                }
            });
        }
    }

    public void syncReceipt(){
        ReceiptRepository receiptRepository = new ReceiptRepository();
        Receipt receipt = new Receipt();
        //ReceiptItem receiptItem = new ReceiptItem();

        CommodityDAO commodityDAO = new CommodityDAO();
        //get all receipts data from the Consumption DAO
        List<Receipt> allReceipts = commodityDAO.getAllReceiptData();
        List<String> attributes = new ArrayList<>();

        for (Receipt row : allReceipts){

            receipt.setDataSystem(row.getDataSystem());
            receipt.setCommodityType(row.getCommodityType());
            receipt.setCommoditySource(row.getCommoditySource());
            receipt.setDestination(row.getDestination());
            receipt.setInstanceType(row.getInstanceType());
            receipt.setOperationDate(row.getOperationDate());
            receipt.setOperationNumber(row.getOperationNumber());
            receipt.setStatus(row.getStatus());

            //get the Receiptitems
            List<ReceiptItem> items = commodityDAO.getAllReceiptItemData(row.getId());

            if(row.getCommodityType().equals("Pharmacy")){
                receipt.setDepartment(row.getDepartment());
                receipt.setAdjustmentKind(row.getAdjustmentKind());
                receipt.setPatient(row.getPatient());
                receipt.setDisposedType(row.getDisposedType());
                receipt.setInstitution(row.getInstitution());
                receipt.setAttributes(attributes);
            }else{
                items.remove("itemDrugType");
            }

            receipt.setItems(items);

//            userJson = gson.toJson(receipt);
//            Log.v("Baron Receip", userJson);

            receiptRepository.syncReceipt(receipt, new DefaultResponseCallbackListener() {
                @Override
                public void onResponse() {
                    //mConsumptionInfoView.startCommodityDashboardActivity();
                    //mConsumptionInfoView.finishConsumptionInfoActivity();
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    //registeringConsumption = false;
                    //mConsumptionInfoView.setProgressBarVisibility(false);
                }
            });
        }
    }

    private void syncDistribution() {
        DistributionRepository distributionRepository = new DistributionRepository();
        Distribution distribution = new Distribution();
        CommodityDAO commodityDAO = new CommodityDAO();

        List<Distribution> distributions = commodityDAO.getAllDistributionData();

        for(Distribution row : distributions){
            distribution.setOperationNumber(row.getOperationNumber());
            distribution.setOperationDate(row.getOperationDate());
            distribution.setSource(row.getSource());
            distribution.setDepartment(row.getDepartment());

            distribution.setStatus(row.getStatus());
            distribution.setInstanceType(row.getInstanceType());
            distribution.setCommoditySource(row.getCommoditySource());
            distribution.setCommodityType(row.getCommodityType());
            distribution.setDataSystem(row.getDataSystem());
            distribution.setDestination("");
            distribution.setInstitution("");

            //get the Distributionitems
            List<DistributionItem> items = commodityDAO.getSingleDistributionItemData(row.getId());
            distribution.setItems(items);
            List<String> attributes = new ArrayList<>();

            if(row.getCommoditySource().equals("Pharmacy")){
                distribution.setPatient("");
                distribution.setDisposedType("");
                distribution.setAdjustmentKind("");
                distribution.setAttributes(attributes);
            }else{
                //Remove this itemDrugType from the Items because its not part of Lab
                items.remove("itemDrugType");
            }


            distributionRepository.syncDistribution(distribution, new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {
                new Update(Distribution.class)
                        .set("isSynced = 1")
                        .where("id = ?", row.getId())
                        .execute();
                new Update(DistributionItem.class)
                        .set("isSynced = 1")
                        .where("distributionId = ?", row.getId())
                        .execute();
                //mDistributionInfoView.startCommodityDashboardActivity();
                //mDistributionInfoView.finishDistributionInfoActivity();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                //registeringDistribution = false;
                //mDistributionInfoView.setProgressBarVisibility(false);
            }
        });

        }

    }

    private void syncTransfer() {
        TransferRepository transferRepository = new TransferRepository();
        Transfer transfer = new Transfer();
        CommodityDAO commodityDAO = new CommodityDAO();

        List<Transfer> allTransfers = commodityDAO.getAllTransferData();

        List<String> attributes = new ArrayList<>();

        for(Transfer row : allTransfers){
            transfer.setOperationNumber(row.getOperationNumber());
            transfer.setOperationDate(row.getOperationDate());
            transfer.setSource(row.getSource());
            transfer.setDepartment(row.getDepartment());

            transfer.setPatient(row.getPatient());
            transfer.setAdjustmentKind(row.getAdjustmentKind());
            transfer.setDisposedType(row.getDisposedType());
            transfer.setDestination(row.getDestination());
            transfer.setInstitution(row.getInstitution());

            transfer.setStatus(row.getStatus());
            transfer.setInstanceType(row.getInstanceType());
            transfer.setCommoditySource(row.getCommoditySource());
            transfer.setCommodityType(row.getCommodityType());
            transfer.setDataSystem(row.getDataSystem());
            transfer.setAttributes(attributes);

            //get the Transfer items
            List<TransferItem> items = commodityDAO.getSingleTransferItemData(row.getId());
            transfer.setItems(items);

            if(row.getCommodityType().equals("Lab")){
                //Remove this itemDrugType from the Items because its not part of Lab
                items.remove("itemDrugType");
            }

            transferRepository.syncTransfer(transfer, new DefaultResponseCallbackListener() {
                @Override
                public void onResponse() {
                    new Update(Transfer.class)
                            .set("isSynced = 1")
                            .where("id = ?", row.getId())
                            .execute();
                    new Update(TransferItem.class)
                            .set("isSynced = 1")
                            .where("transferId = ?", row.getId())
                            .execute();
                    //mTransferInfoView.startCommodityDashboardActivity();
                    //mTransferInfoView.finishTransferInfoActivity();
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    //registeringTransfer = false;
                    //mTransferInfoView.setProgressBarVisibility(false);
                }
            });

        }


    }

    private void syncAdjustment() {
        AdjustmentRepository adjustmentRepository = new AdjustmentRepository();
        Adjustment adjustment = new Adjustment();
        CommodityDAO commodityDAO = new CommodityDAO();

        List<Adjustment> allAdjustment = commodityDAO.getAllAdjustmentData();

        for(Adjustment row : allAdjustment){
            adjustment.setOperationNumber(row.getOperationNumber());
            adjustment.setOperationDate(row.getOperationDate());
            adjustment.setDestination(row.getDestination());
            adjustment.setDepartment(row.getDepartment());

            adjustment.setStatus(row.getStatus());
            adjustment.setInstanceType(row.getInstanceType());
            adjustment.setCommoditySource(row.getCommoditySource());
            adjustment.setCommodityType(row.getCommodityType());
            adjustment.setDataSystem(row.getDataSystem());

            adjustment.setInstitution(row.getInstitution());
            adjustment.setAdjustmentKind(row.getAdjustmentKind());
            adjustment.setPatient(row.getPatient());
            adjustment.setDisposedType(row.getDisposedType());
            adjustment.setAttributes(new ArrayList<String>());

            //get the Distributionitems
            List<AdjustmentItem> items = commodityDAO.getSingleAdjustmentItemData(row.getId());
            adjustment.setItems(items);

            if(row.getCommodityType().equals("Lab")){
                //Remove this itemDrugType from the Items because its not part of Lab
                items.remove("itemDrugType");
            }

            adjustmentRepository.syncAdjustment(adjustment, new DefaultResponseCallbackListener() {
                @Override
                public void onResponse() {
                    new Update(Adjustment.class)
                            .set("isSynced = 1")
                            .where("id = ?", row.getId())
                            .execute();
                    new Update(AdjustmentItem.class)
                            .set("isSynced = 1")
                            .where("adjustmentId = ?", row.getId())
                            .execute();
                    //mDistributionInfoView.startCommodityDashboardActivity();
                    //mDistributionInfoView.finishDistributionInfoActivity();
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    //registeringDistribution = false;
                    //mDistributionInfoView.setProgressBarVisibility(false);
                }
            });

        }


    }

    public void getInventoryStockSummary(){
        InventoryStockRepository inventoryStockRepository = new InventoryStockRepository();
        inventoryStockRepository.getInventoryStockSummaryLab(new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {

            }

            @Override
            public void onErrorResponse(String errorMessage) {

            }
        });
        inventoryStockRepository.getInventoryStockSummaryPharmacy(new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {

            }

            @Override
            public void onErrorResponse(String errorMessage) {

            }
        });
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure() {

    }
}
