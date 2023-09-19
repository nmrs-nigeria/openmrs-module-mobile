package org.openmrs.mobile.dao;

import android.database.Observable;
import android.util.Log;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;

import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;

import java.util.ArrayList;
import java.util.List;

public class CommodityDAO {

    public List<Consumption> getAllConsumptionData() {
        List<Consumption> consumptionList = new Select().all().from(Consumption.class).where("isSynced = ?", 0).execute();
        return consumptionList;
    }

    public List<Receipt> getAllReceiptData() {
        List<Receipt> receipts = new Select().from(Receipt.class).where("isSynced = ?", 0).execute();
        return receipts;
    }

    public List<ReceiptItem> getAllReceiptItemData(long id) {
        List<ReceiptItem> receiptItems = new Select().from(ReceiptItem.class).where("receiptId = ? AND isSynced = ?", id,0).execute();
        return receiptItems;
    }

    public List<Distribution> getAllDistributionData(){
        List<Distribution> distributionList = new Select().all().from(Distribution.class).where("isSynced = ?", 0).execute();
        return distributionList;
    }

    public List<DistributionItem> getSingleDistributionItemData(long id) {
        List<DistributionItem> distributionItems = new Select().from(DistributionItem.class).where("distributionId = ? AND isSynced = ?", id, 0).execute();
        return distributionItems;
    }

    public List<Transfer> getAllTransferData(){
        List<Transfer> transferList = new Select().all().from(Transfer.class).where("isSynced = ?", 0).execute();
        return transferList;
    }

    public List<TransferItem> getSingleTransferItemData(long id) {
        List<TransferItem> transferItems = new Select().from(TransferItem.class).where("transferId = ? AND isSynced = ?", id, 0).execute();
        return transferItems;
    }

    public List<Adjustment> getAllAdjustmentData(){
        List<Adjustment> adjustmentList = new Select().all().from(Adjustment.class).where("isSynced = ?", 0).execute();
        return adjustmentList;
    }

    public List<AdjustmentItem> getSingleAdjustmentItemData(long id) {
        List<AdjustmentItem> adjustmentItems = new Select().from(AdjustmentItem.class).where("adjustmentId = ? AND isSynced = ?", id, 0).execute();
        return adjustmentItems;
    }


    /***
     *   public CommodityDAO getAllConsumptionData(){
     *         List<Consumption> consumptionList = new Select().all().from(Consumption.class).execute();
     *         this.allConsumptions = consumptionList;
     *         return  this;
     *     }
     *
     *     public CommodityDAO unsyncedConsumption(){
     *         List<Consumption> consumptionList = new Select().all().from(Consumption.class).where("synced=0").execute();
     *         this.unscynced = consumptionList;
     *         return  this;
     *     }
     */
}
