package org.openmrs.mobile.activities.viewreceipt;

import android.util.Log;

import com.activeandroid.query.Select;

import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Pharmacy;

public class ViewReceiptRowItem {

    public String displayDate;
    public String displayCommodityType;
    public String displayItem;
    public String displayItemBatch;

    public ViewReceiptRowItem(String date, String commodityType, String item, String itemBatch){
        this.displayDate = date;
        this.displayCommodityType = commodityType;
        this.displayItem = item;
        this.displayItemBatch = itemBatch;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public String getDisplayCommodityType() {
        return displayCommodityType;
    }

    public String getDisplayItem() {
        if(displayCommodityType.equals("Lab")) {
            Item itemSelect = new Select()
                    .from(Item.class)
                    .where("uuid = ?", this.displayItem)
                    .executeSingle();
            return itemSelect.getName();
        }else{
            Pharmacy pharmacySelect = new Select()
                    .from(Pharmacy.class)
                    .where("uuid = ?", this.displayItem)
                    .executeSingle();
            return pharmacySelect.getName();
        }
    }

    public String getDisplayItemBatch() {
        return displayItemBatch;
    }

}
