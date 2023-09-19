package org.openmrs.mobile.activities.viewconsumption;

import com.activeandroid.query.Select;

import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Pharmacy;

public class ViewConsumptionRowItem {

    public String displayDate;
    public String displayDepartment;
    public String displayItem;
    public String displayItemBatch;

    public ViewConsumptionRowItem(String date, String department, String item, String itemBatch){
        this.displayDate = date;
        this.displayDepartment = department;
        this.displayItem = item;
        this.displayItemBatch = itemBatch;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public String getDisplayDepartment() {
        Department department = new Select()
                .from(Department.class)
                .where("uuid = ?", this.displayDepartment)
                .executeSingle();
        if(department.equals(null)){
            return displayDepartment;
        }
        return department.getName();
    }

    public String getDisplayItem() {
            Item itemSelect = new Select()
                    .from(Item.class)
                    .where("uuid = ?", this.displayItem)
                    .executeSingle();
            if(itemSelect.equals(null)){
                return displayItem;
            }
            return itemSelect.getName();
    }

    public String getDisplayItemBatch() {
        return displayItemBatch;
    }
}
