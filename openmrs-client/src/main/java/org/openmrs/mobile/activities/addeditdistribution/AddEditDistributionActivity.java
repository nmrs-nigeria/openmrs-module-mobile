package org.openmrs.mobile.activities.addeditdistribution;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionContract;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionFragment;
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionPresenter;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.util.Arrays;
import java.util.List;

public class AddEditDistributionActivity extends ACBaseActivity {
    public AddEditDistributionContract.Presenter mPresenter;

    public AddEditDistributionFragment addEditDistributionFragment;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_distribution_info);

        // Create fragment
        addEditDistributionFragment =
                (AddEditDistributionFragment) getSupportFragmentManager().findFragmentById(R.id.distributionContentFrame);
        if (addEditDistributionFragment == null) {
            addEditDistributionFragment = AddEditDistributionFragment.newInstance();
        }
        if (!addEditDistributionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    addEditDistributionFragment, R.id.distributionContentFrame);
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
        // Create the mPresenter
        mPresenter = new AddEditDistributionPresenter(addEditDistributionFragment, patientID);
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
        if (!mPresenter.isRegisteringDistribution()) {
            boolean createDialog = addEditDistributionFragment.areFieldsNotEmpty();
            if (createDialog) {
                showInfoLostDialog();
            } else {
                if (!mPresenter.isRegisteringDistribution()) {
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



