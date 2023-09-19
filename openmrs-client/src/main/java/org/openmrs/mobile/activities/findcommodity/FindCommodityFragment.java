package org.openmrs.mobile.activities.findcommodity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditadjustment.AddEditAdjustmentActivity;
import org.openmrs.mobile.activities.addeditconsumption.AddEditConsumptionActivity;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionActivity;
import org.openmrs.mobile.activities.addeditreceipt.AddEditReceiptActivity;
import org.openmrs.mobile.activities.addedittransfer.AddEditTransferActivity;
import org.openmrs.mobile.activities.viewadjustment.ViewAdjustmentActivity;
import org.openmrs.mobile.activities.viewconsumption.ViewConsumptionActivity;
import org.openmrs.mobile.activities.viewdistribution.ViewDistributionActivity;
import org.openmrs.mobile.activities.viewreceipt.ViewReceiptActivity;
import org.openmrs.mobile.activities.viewtransfer.ViewTransferActivity;
import org.openmrs.mobile.api.CommodityService;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.ThemeUtils;
import org.openmrs.mobile.utilities.ToastUtil;


public class FindCommodityFragment extends ACBaseFragment<FindCommodityContract.Presenter> implements FindCommodityContract.View, View.OnClickListener  {

    private ImageView mConsumptionButton;
    private ImageView mTransferButton;
    private ImageView mReceiptButton;
    private ImageView mDistributionButton;
    private ImageView mAdjustmentButton;
    private RelativeLayout mConsumptionView;
    private RelativeLayout mTransferView;
    private RelativeLayout mReceiptView;
    private RelativeLayout mDistributionView;
    private RelativeLayout mAdjustmentView;
    private SparseArray<Bitmap> mBitmapCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_find_commodity, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);

        if (root != null) {
            initFragmentFields(root);
            setListeners();
        }

        return root;
    }

    private void initFragmentFields(View root) {
        mConsumptionButton = root.findViewById(R.id.consumptionButton);
        mDistributionButton = root.findViewById(R.id.distributionButton);
        mReceiptButton = root.findViewById(R.id.receiptButton);
        mTransferButton = root.findViewById(R.id.transferButton);
        mAdjustmentButton = root.findViewById(R.id.adjustmentButton);
        mConsumptionView = root.findViewById(R.id.consumptionView);
        mDistributionView = root.findViewById(R.id.distributionView);
        mTransferView = root.findViewById(R.id.transferView);
        mReceiptView = root.findViewById(R.id.receiptView);
        mAdjustmentView = root.findViewById(R.id.adjustmentView);
    }

    private void setListeners() {
        mReceiptView.setOnClickListener(this);
        mTransferView.setOnClickListener(this);
        mDistributionView.setOnClickListener(this);
        mConsumptionView.setOnClickListener(this);
        mAdjustmentView.setOnClickListener(this);
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
            case R.id.receiptView:
                startNewActivity(ViewReceiptActivity.class);
                break;
            case R.id.consumptionView:
                startNewActivity(ViewConsumptionActivity.class);
                break;
            case R.id.distributionView:
                startNewActivity(ViewDistributionActivity.class);
                break;
            case R.id.transferView:
                startNewActivity(ViewTransferActivity.class);
                break;
            case R.id.adjustmentView:
                startNewActivity(ViewAdjustmentActivity.class);
                break;
            default:
                // Do nothing
                break;
        }
    }

    public static FindCommodityFragment newInstance() {
        return new FindCommodityFragment();
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
        bindDrawableResource(mConsumptionButton, R.drawable.consumption1);
        bindDrawableResource(mDistributionButton, R.drawable.moving_truck21);
        bindDrawableResource(mReceiptButton, R.drawable.delivery_box1);
        bindDrawableResource(mTransferButton, R.drawable.transfer_operation1);
        bindDrawableResource(mAdjustmentButton, R.drawable.adjustment1);
        changeColorOfDashboardIcons();
        if (ThemeUtils.isDarkModeActivated()) {
            changeColorOfDashboardIcons();
        }
    }

    private void changeColorOfDashboardIcons() {
        final int greenColorResId = R.color.green;
        final int redColorResId = R.color.light_red;
        final int purpleColorResId = R.color.dark_purple;
        final int blueColorResId = R.color.dark_purple;
//        ImageUtils.changeImageViewTint(getContext(), mReceiptButton, greenColorResId);
//        ImageUtils.changeImageViewTint(getContext(), mTransferButton, redColorResId);
//        ImageUtils.changeImageViewTint(getContext(), mDistributionButton, purpleColorResId);
//        ImageUtils.changeImageViewTint(getContext(), mConsumptionButton, blueColorResId);

    }


}
