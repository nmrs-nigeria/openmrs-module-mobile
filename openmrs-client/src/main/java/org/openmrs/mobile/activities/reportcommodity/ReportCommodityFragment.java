package org.openmrs.mobile.activities.reportcommodity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.viewadjustment.ViewAdjustmentActivity;
import org.openmrs.mobile.activities.viewconsumption.ViewConsumptionActivity;
import org.openmrs.mobile.activities.viewdistribution.ViewDistributionActivity;
import org.openmrs.mobile.activities.viewreceipt.ViewReceiptActivity;
import org.openmrs.mobile.activities.viewtransfer.ViewTransferActivity;
import org.openmrs.mobile.api.CommodityService;
import org.openmrs.mobile.api.repository.InventoryStockRepository;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.InventoryStockSummaryLab;
import org.openmrs.mobile.models.InventoryStockSummaryPharmacy;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.ThemeUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.List;


public class ReportCommodityFragment extends ACBaseFragment<ReportCommodityContract.Presenter> implements ReportCommodityContract.View, View.OnClickListener {

    private Button mReloadButton;
    private SparseArray<Bitmap> mBitmapCache;
    private InventoryStockRepository inventoryStockRepository;
    private TextView labReport;
    private TextView pharmacyReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report_commodity, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);

        if (root != null) {
            inventoryStockRepository = new InventoryStockRepository();
            initFragmentFields(root);
            setListeners();
            fillfields();
        }

        return root;
    }

    private void initFragmentFields(View root) {
        mReloadButton = root.findViewById(R.id.reloadButton);
        labReport = root.findViewById(R.id.labReport);
        pharmacyReport = root.findViewById(R.id.pharmacyReport);
    }

    private void fillfields(){
        this.displayLabItems();
        this.displayPharmacyItems();
    }

    private void setListeners() {
        mReloadButton.setOnClickListener(this);
    }

    /**
     * Starts new Activity depending on which ImageView triggered it
     */
    private void startNewActivity(Class<? extends ACBaseActivity> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reloadButton:
                boolean syncState = OpenMRS.getInstance().getSyncState();
                if(!syncState){
                    OpenMRSCustomHandler.showDialogMessage(getContext(), "Please turn on the Sync Button");
                }else {
                    this.getLabItems();
                    this.getPharmacyItems();
                    this.displayLabItems();
                    this.displayPharmacyItems();
                    ToastUtil.notifyLong("Report Loaded Successfully");
                }
                break;
            default:
                // Do nothing
                break;
        }
    }

    public static ReportCommodityFragment newInstance() {
        return new ReportCommodityFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSubmit:

                return true;
            default:
                // Do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createImageBitmap(Integer key, ViewGroup.LayoutParams layoutParams) {
        if (mBitmapCache.get(key) == null) {
            mBitmapCache.put(key, ImageUtils.decodeBitmapFromResource(getResources(), key,
                    layoutParams.width, layoutParams.height));
        }
    }

    /**
     * Binds drawable resource to ImageView
     *
     * @param imageView  ImageView to bind resource to
     * @param drawableId id of drawable resource (for example R.id.somePicture);
     */
    private void bindDrawableResource(ImageView imageView, int drawableId) {
        mBitmapCache = new SparseArray<>();
        if (getView() != null) {
            createImageBitmap(drawableId, imageView.getLayoutParams());
            imageView.setImageBitmap(mBitmapCache.get(drawableId));
        }
    }

    /**
     * Binds drawable resources to all ashboard buttons
     * Initially called by this view's presenter
     */
    @Override
    public void bindDrawableResources() {

    }

    private void getLabItems() {
        inventoryStockRepository.getInventoryStockSummaryLab(new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {

            }

            @Override
            public void onErrorResponse(String errorMessage) {

            }
        });
    }

    private void getPharmacyItems() {
        inventoryStockRepository.getInventoryStockSummaryPharmacy(new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {

            }

            @Override
            public void onErrorResponse(String errorMessage) {

            }
        });
    }

    private void displayLabItems() {
        List<InventoryStockSummaryLab> itemSelect = new Select()
                .from(InventoryStockSummaryLab.class).execute();

        StringBuilder stringBuilder = new StringBuilder();
        for(InventoryStockSummaryLab row : itemSelect){
            stringBuilder.append("Name:" + row.getName() + " - Quantity:" + row.getQuantity() + " - Expiration:" + row.getExpiration() + " - Item Batch:" +row.getItemBatch() + "\n\n");
        }
        labReport.setText(stringBuilder);
    }

    private void displayPharmacyItems() {
        List<InventoryStockSummaryPharmacy> itemSelect = new Select()
                .from(InventoryStockSummaryPharmacy.class).execute();

        StringBuilder stringBuilder = new StringBuilder();
        for(InventoryStockSummaryPharmacy row : itemSelect){
            stringBuilder.append("Name:" + row.getName() + " - Quantity:" + row.getQuantity() + " - Expiration:" + row.getExpiration() + " - Item Batch:" +row.getItemBatch() + "\n\n");
        }
        pharmacyReport.setText(stringBuilder);
    }

}
