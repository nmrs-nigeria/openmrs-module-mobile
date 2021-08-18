package org.openmrs.mobile.activities.commodity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.formentrypatientlist.FormEntryPatientListActivity;
import org.openmrs.mobile.activities.formprogramlist.FormProgramActivity;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsActivity;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.ThemeUtils;


public class CommodityFragment extends ACBaseFragment<CommodityContract.Presenter> implements CommodityContract.View, View.OnClickListener  {
    private ImageView mConsumptionButton;
    private ImageView mReportButton;
    private ImageView mReceiptButton;
    private ImageView mDistributionButton;
    private RelativeLayout mConsumptionView;
    private RelativeLayout mReportView;
    private RelativeLayout mReceiptView;
    private RelativeLayout mDistributionView;
    private SparseArray<Bitmap> mBitmapCache;
    private String patientID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_commodity, container, false);
        //Check if bundle includes patient ID

        if (root != null) {
            initFragmentFields(root);
            setListeners();
        }

        // Font config
        FontsUtil.setFont(this.getActivity().findViewById(android.R.id.content));
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }


    private void initFragmentFields(View root) {
        mConsumptionButton = root.findViewById(R.id.consumptionButton);
        mDistributionButton = root.findViewById(R.id.distributionButton);
        mReceiptButton = root.findViewById(R.id.receiptButton);
        mReportButton = root.findViewById(R.id.reportButton);
        mConsumptionView = root.findViewById(R.id.consumptionView);
        mDistributionView = root.findViewById(R.id.distributionView);
        mReportView = root.findViewById(R.id.reportView);
        mReceiptView = root.findViewById(R.id.receiptView);
    }

    private void setListeners() {
        mReceiptView.setOnClickListener(this);
        mReportView.setOnClickListener(this);
        mDistributionView.setOnClickListener(this);
        mConsumptionView.setOnClickListener(this);

    }

    /**
     * Starts new Activity depending on which ImageView triggered it
     */
    private void startNewActivity(Class<? extends ACBaseActivity> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientID);
//        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_PROGRAM, programName);
        startActivity(intent);
    }

    public void setPatientId(String pId){
        patientID = pId;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.receiptView:
//                startNewActivity(AddEditPatientActivity.class);

                break;
            case R.id.reportView:
//                startNewActivity(AddEditPatientActivity.class);
                break;
            case R.id.distributionView:
//                startNewActivity(AddEditPatientActivity.class);
                break;
            case R.id.consumptionView:
                startNewActivity(SyncedPatientsActivity.class);
                break;
            default:
                // Do nothing
                break;
        }
    }
    /**
     * @return New instance of SyncedPatientsFragment
     */
    public static CommodityFragment newInstance() {
        return new CommodityFragment();
    }

    /**
     * Unbinds drawable resources
     */
    private void unbindDrawableResources() {
        if (null != mBitmapCache) {
            for (int i = 0; i < mBitmapCache.size(); i++) {
                Bitmap bitmap = mBitmapCache.valueAt(i);
                bitmap.recycle();
            }
        }
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
        bindDrawableResource(mConsumptionButton, R.drawable.consumption);
        bindDrawableResource(mDistributionButton, R.drawable.moving_truck2);
        bindDrawableResource(mReceiptButton, R.drawable.delivery_box);
        bindDrawableResource(mReportButton, R.drawable.sieo_report);
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
        ImageUtils.changeImageViewTint(getContext(), mReceiptButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), mReportButton, redColorResId);
        ImageUtils.changeImageViewTint(getContext(), mDistributionButton, purpleColorResId);
        ImageUtils.changeImageViewTint(getContext(), mConsumptionButton, blueColorResId);

    }

}
