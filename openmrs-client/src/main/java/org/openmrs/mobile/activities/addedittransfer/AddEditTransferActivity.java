package org.openmrs.mobile.activities.addedittransfer;

        import android.app.AlertDialog;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;

        import org.openmrs.mobile.R;
        import org.openmrs.mobile.activities.ACBaseActivity;
        import org.openmrs.mobile.activities.addedittransfer.AddEditTransferContract;
        import org.openmrs.mobile.activities.addedittransfer.AddEditTransferFragment;
        import org.openmrs.mobile.activities.addedittransfer.AddEditTransferPresenter;
        import org.openmrs.mobile.utilities.ApplicationConstants;

        import java.util.Arrays;
        import java.util.List;

public class AddEditTransferActivity extends ACBaseActivity {
    public AddEditTransferContract.Presenter mPresenter;

    public AddEditTransferFragment addEditTransferFragment;
    private AlertDialog alertDialog;
    private long consumptionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_transfer_info);

        // Create fragment
        addEditTransferFragment =
                (AddEditTransferFragment) getSupportFragmentManager().findFragmentById(R.id.transferContentFrame);
        if (addEditTransferFragment == null) {
            addEditTransferFragment = AddEditTransferFragment.newInstance();
        }
        if (!addEditTransferFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    addEditTransferFragment, R.id.transferContentFrame);
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
        mPresenter = new AddEditTransferPresenter(addEditTransferFragment, consumptionID);
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
        if (!mPresenter.isRegisteringTransfer()) {
            boolean createDialog = addEditTransferFragment.areFieldsNotEmpty();
            if (createDialog) {
                showInfoLostDialog();
            } else {
                if (!mPresenter.isRegisteringTransfer()) {
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



