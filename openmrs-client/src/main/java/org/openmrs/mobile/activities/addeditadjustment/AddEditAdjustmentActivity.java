package org.openmrs.mobile.activities.addeditadjustment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionPresenter;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.util.Arrays;
import java.util.List;

public class AddEditAdjustmentActivity extends ACBaseActivity {

    public AddEditAdjustmentContract.Presenter mPresenter;

    public AddEditAdjustmentFragment addEditAdjustmentFragment;
    private AlertDialog alertDialog;
    private long consumptionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment_info);

        // Create fragment
        addEditAdjustmentFragment =
                (AddEditAdjustmentFragment) getSupportFragmentManager().findFragmentById(R.id.adjustmentContentFrame);
        if (addEditAdjustmentFragment == null) {
            addEditAdjustmentFragment = AddEditAdjustmentFragment.newInstance();
        }
        if (!addEditAdjustmentFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    addEditAdjustmentFragment, R.id.adjustmentContentFrame);
        }


        //Check if bundle includes patient ID
        Bundle patientBundle = savedInstanceState;
        if (patientBundle != null) {
            patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        } else {
            patientBundle = getIntent().getExtras();
        }
        String patientID = "";
        if (patientBundle != null) {
            patientID = patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        }

        List<String> countries = Arrays.asList(getResources().getStringArray(R.array.countries_array));
        //Get the Consumption ID if any exists
        Bundle gotBasket = getIntent().getExtras();
        consumptionID = gotBasket.getLong("id", 0);
        if(consumptionID != 0){

        }
        // Create the mPresenter
         mPresenter = new AddEditAdjustmentPresenter(addEditAdjustmentFragment, consumptionID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mPresenter.isRegisteringAdjustment()) {
            boolean createDialog = addEditAdjustmentFragment.areFieldsNotEmpty();
            if (createDialog) {
                showInfoLostDialog();
            } else {
                if (!mPresenter.isRegisteringAdjustment()) {
                    super.onBackPressed();
                }
            }
        }
    }

    /**
     * The method creates a warning dialog when the user presses back button while registering a patient
     */
    private void showInfoLostDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle(R.string.dialog_title_are_you_sure);
        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialog_message_data_lost)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_button_stay, (dialog, id) -> dialog.cancel())
                .setNegativeButton(R.string.dialog_button_leave, (dialog, id) -> {
                    // Finish the activity
                    super.onBackPressed();
                    finish();
                });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    @Override
    protected void onPause() {
        if (alertDialog != null) {
            // Dismiss and clear the dialog to prevent Window leaks
            alertDialog.dismiss();
            alertDialog = null;
        }
        super.onPause();
    }

}