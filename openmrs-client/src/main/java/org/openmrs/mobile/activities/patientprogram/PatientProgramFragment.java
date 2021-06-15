package org.openmrs.mobile.activities.patientprogram;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.activevisits.ActiveVisitsActivity;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.formentrypatientlist.FormEntryPatientListActivity;
import org.openmrs.mobile.activities.formprogramlist.FormProgramActivity;
import org.openmrs.mobile.activities.providermanagerdashboard.ProviderManagerDashboardActivity;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsActivity;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ImageUtils;
import org.openmrs.mobile.utilities.ThemeUtils;


public class PatientProgramFragment extends ACBaseFragment<PatientProgramContract.Presenter> implements PatientProgramContract.View, View.OnClickListener  {
    private ImageView mheiButton;
    private ImageView mhtsButton;
    private ImageView mpmtctButton;
    private ImageView martButton;
    private RelativeLayout martView;
    private RelativeLayout mpmtctView;
    private RelativeLayout mhtsView;
    private RelativeLayout mheiView;
    private SparseArray<Bitmap> mBitmapCache;
    private String patientID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_patient_program, container, false);
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
        mhtsButton = root.findViewById(R.id.htsButton);
        mpmtctButton = root.findViewById(R.id.pmtctButton);
        martButton = root.findViewById(R.id.artButton);
        mheiButton = root.findViewById(R.id.heiButton);
        mhtsView = root.findViewById(R.id.htsView);
        mpmtctView = root.findViewById(R.id.pmtctView);
        martView = root.findViewById(R.id.artView);
        mheiView = root.findViewById(R.id.heiView);
    }

    private void setListeners() {
        mhtsView.setOnClickListener(this);
        mpmtctView.setOnClickListener(this);
        martView.setOnClickListener(this);
        mheiView.setOnClickListener(this);

    }

    /**
     * Starts new Activity depending on which ImageView triggered it
     */
    private void startNewActivity(Class<? extends ACBaseActivity> clazz, String programName) {
        Intent intent = new Intent(getActivity(), clazz);
        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientID);
        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_PROGRAM, programName);
        startActivity(intent);
    }

    public void setPatientId(String pId){
        patientID = pId;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.artView:
                startNewActivity(FormProgramActivity.class, "ART");
                break;
            case R.id.htsView:
                startNewActivity(FormProgramActivity.class,"HTS");
                break;
            case R.id.heiView:
//                startNewActivity(FormProgramActivity.class,"HEI");
                break;
            case R.id.pmtctView:
                Patient patient = new PatientDAO().findPatientByID(patientID);
                if(("F").equals(patient.getGender())) {
                    startNewActivity(FormProgramActivity.class, "PMTCT");
                }
                else{
                    mpmtctView.setVisibility(View.GONE);
                }
                break;
            default:
                // Do nothing
                break;
        }
    }
    /**
     * @return New instance of SyncedPatientsFragment
     */
    public static PatientProgramFragment newInstance() {
        return new PatientProgramFragment();
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
        bindDrawableResource(mhtsButton, R.drawable.healthcare);
        bindDrawableResource(martButton, R.drawable.medical_art);
        bindDrawableResource(mpmtctButton, R.drawable.holidays);
        bindDrawableResource(mheiButton, R.drawable.healthcare_and_medical);
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
        ImageUtils.changeImageViewTint(getContext(), mhtsButton, greenColorResId);
        ImageUtils.changeImageViewTint(getContext(), martButton, redColorResId);
        ImageUtils.changeImageViewTint(getContext(), mpmtctButton, purpleColorResId);
        ImageUtils.changeImageViewTint(getContext(), mheiButton, blueColorResId);

    }

}
