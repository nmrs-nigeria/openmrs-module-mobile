package org.openmrs.mobile.activities.covidtbintegrator;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditconsumption.AddEditConsumptionFragment;
import org.openmrs.mobile.activities.commodity.CommodityActivity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.models.CovidTBIntegrator;
import org.openmrs.mobile.models.CovidTBIntegrator;
import org.openmrs.mobile.models.Destination;
import org.openmrs.mobile.models.Item;
import org.openmrs.mobile.models.Pharmacy;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CovidTBIntegratorFragment extends ACBaseFragment<CovidTBIntegratorContract.Presenter> implements CovidTBIntegratorContract.View {

    private ProgressBar progressBar;
    Spinner blood_transfusion;
    Spinner unprotected_sex;
    Spinner sti;
    Spinner diagnosed_tb;
    Spinner iv_drugs;
    Spinner forced_sex;
    Spinner complaints_genital_sores;
    Spinner cough_gt_2wks;
    Spinner weight_loss;
    Spinner fever;
    Spinner night_sweats;
    Spinner cough_w_heamoptysis;
    Spinner cough_weightloss_fever;
    Spinner cough_unexplained_weightloss;
    Spinner cough_w_fever_gt_2wks;
    Spinner unexplained_weightloss;
    Spinner cough_sputum;
    EditText temperature_reading;
    Spinner health_care_worker;
    Spinner dry_cough;
    Spinner shortness_of_breath;
    Spinner history_of_fever;
    Spinner muscle_aches;
    Spinner not_vaccinated;
    Spinner loss_of_taste;
    Spinner loss_of_sense;
    Spinner sore_throat;
    Spinner headache;
    Spinner international_travel;
    Spinner close_contact;
    Spinner history_of_chronic;

    String patientUUID;

    private OpenMRSLogger logger = new OpenMRSLogger();

    private boolean isUpdateCovidTBIntegrator = false;
    private CovidTBIntegrator updatedCovidTBIntegrator;
    private int PERMISSION_ID = 44;


    private CovidTBIntegrator admCovidTBIntegrator;
    private Button addMoreBtn;
    private Button deleteCommodity;

    //Declare a List array where consumption objects will be saved for multiple encounters
    private List<CovidTBIntegrator> covidTBIntegratorMultiple;
    private boolean allCompleted = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_covidtbintegrator_info, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        fillFields(mPresenter.getCovidTBIntegratorToUpdate());
        covidTBIntegratorMultiple = new ArrayList<CovidTBIntegrator>();
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    private void resolveViews(View v) {
        progressBar = v.findViewById(R.id.progress_bar);
        blood_transfusion = v.findViewById(R.id.blood_transfusion);
        unprotected_sex = v.findViewById(R.id.unprotected_sex);
        sti = v.findViewById(R.id.sti);
        diagnosed_tb = v.findViewById(R.id.diagnosed_tb);
        iv_drugs = v.findViewById(R.id.iv_drugs);
        forced_sex = v.findViewById(R.id.forced_sex);
        complaints_genital_sores = v.findViewById(R.id.complaints_genital_sores);
        cough_gt_2wks = v.findViewById(R.id.cough_gt_2wks);
        weight_loss = v.findViewById(R.id.weight_loss);
        fever = v.findViewById(R.id.fever);
        night_sweats = v.findViewById(R.id.night_sweats);
        cough_w_heamoptysis = v.findViewById(R.id.cough_w_heamoptysis);
        cough_weightloss_fever = v.findViewById(R.id.cough_weightloss_fever);
        cough_unexplained_weightloss = v.findViewById(R.id.cough_unexplained_weightloss);
        cough_w_fever_gt_2wks = v.findViewById(R.id.cough_w_fever_gt_2wks);
        unexplained_weightloss = v.findViewById(R.id.unexplained_weightloss);
        cough_sputum = v.findViewById(R.id.cough_sputum);
        temperature_reading = v.findViewById(R.id.temperature_reading);
        health_care_worker = v.findViewById(R.id.health_care_worker);
        dry_cough = v.findViewById(R.id.dry_cough);
        shortness_of_breath = v.findViewById(R.id.shortness_of_breath);
        history_of_fever = v.findViewById(R.id.history_of_fever);
        muscle_aches = v.findViewById(R.id.muscle_aches);
        not_vaccinated = v.findViewById(R.id.not_vaccinated);
        loss_of_taste = v.findViewById(R.id.loss_of_taste);
        loss_of_sense = v.findViewById(R.id.loss_of_sense);
        sore_throat = v.findViewById(R.id.sore_throat);
        headache = v.findViewById(R.id.headache);
        international_travel = v.findViewById(R.id.international_travel);
        close_contact = v.findViewById(R.id.close_contact);
        history_of_chronic = v.findViewById(R.id.history_of_chronic);
    }

    @Override
    public void finishCovidTBIntegratorInfoActivity() {

    }

    @Override
    public void setErrorsVisibility(boolean covidTBIntegratorError) {
        // Only two dedicated text views will be visible for error messages.
        // Rest error messages will be displayed in dedicated TextInputLayouts.

    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView = this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }

    private void updateCovidTBIntegratorWithData(CovidTBIntegrator covidTBIntegrator) {
        //validate(covidTBIntegrator);
    }

    public void validateSubmition() {

    }

    public void validate() {
        String covidTBIntegrator_date = null;
        boolean allCompleted = true;
        CovidTBIntegrator covidTBIntegrator = new CovidTBIntegrator();

        if (blood_transfusion.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.blood_transfusion_in_last_3_months));
            allCompleted = false;
        } else {
            covidTBIntegrator.setBlood_transfusion(blood_transfusion.getSelectedItem().toString());
        }

        if (unprotected_sex.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.unprotected_sex_in_the_last_3_months));
            allCompleted = false;
        } else {
            covidTBIntegrator.setUnprotected_sex(unprotected_sex.getSelectedItem().toString());
        }

        if (sti.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.sti_in_the_last_3_months));
            allCompleted = false;
        } else {
            covidTBIntegrator.setSti(sti.getSelectedItem().toString());
        }

        if (diagnosed_tb.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.diagnosed_for_tb_currently_on_tb_treatment));
            allCompleted = false;
        } else {
            covidTBIntegrator.setDiagnosed_tb(diagnosed_tb.getSelectedItem().toString());
        }

        if (iv_drugs.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.iv_drugs_user_shares_needle_or_sharps));
            allCompleted = false;
        } else {
            covidTBIntegrator.setIv_drugs(iv_drugs.getSelectedItem().toString());
        }

        if (forced_sex.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.forced_to_have_sex_transactional_sex_for_money_gifts));
            allCompleted = false;
        } else {
            covidTBIntegrator.setForced_sex(forced_sex.getSelectedItem().toString());
        }

        if (complaints_genital_sores.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.complaints_of_genital_sores_or_swollen_inguinal_lymph_nodes_with_or_without_pains));
            allCompleted = false;
        } else {
            covidTBIntegrator.setComplaints_genital_sores(complaints_genital_sores.getSelectedItem().toString());
        }

        if (cough_gt_2wks.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.cough_2_weeks));
            allCompleted = false;
        } else {
            covidTBIntegrator.setCough_gt_2wks(cough_gt_2wks.getSelectedItem().toString());
        }

        if (weight_loss.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.weight_loss));
            allCompleted = false;
        } else {
            covidTBIntegrator.setWeight_loss(weight_loss.getSelectedItem().toString());
        }

        if (fever.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.fever));
            allCompleted = false;
        } else {
            covidTBIntegrator.setFever(fever.getSelectedItem().toString());
        }

        if (night_sweats.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.night_sweats));
            allCompleted = false;
        } else {
            covidTBIntegrator.setNight_sweats(night_sweats.getSelectedItem().toString());
        }

        if (cough_w_heamoptysis.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.cough_of_any_duration_with_heamoptysis_with_out_other_symptoms));
            allCompleted = false;
        } else {
            covidTBIntegrator.setCough_w_heamoptysis(cough_w_heamoptysis.getSelectedItem().toString());
        }

        if (cough_weightloss_fever.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.cough_of_any_duration_with_unexplained_weightloss_fever));
            allCompleted = false;
        } else {
            covidTBIntegrator.setCough_weightloss_fever(cough_weightloss_fever.getSelectedItem().toString());
        }

        if (cough_unexplained_weightloss.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.cough_of_any_duration_with_unexplained_weightloss));
            allCompleted = false;
        } else {
            covidTBIntegrator.setCough_unexplained_weightloss(cough_unexplained_weightloss.getSelectedItem().toString());
        }

        if (cough_w_fever_gt_2wks.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.cough_of_any_duration_with_fever_2_weeks));
            allCompleted = false;
        } else {
            covidTBIntegrator.setCough_w_fever_gt_2wks(cough_w_fever_gt_2wks.getSelectedItem().toString());
        }

        if (unexplained_weightloss.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.unexplained_weight_loss_fever_2_weeks));
            allCompleted = false;
        } else {
            covidTBIntegrator.setUnexplained_weightloss(unexplained_weightloss.getSelectedItem().toString());
        }

        if (cough_sputum.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.cough_less_than_2_weeks_with_sputum));
            allCompleted = false;
        } else {
            covidTBIntegrator.setCough_sputum(cough_sputum.getSelectedItem().toString());
        }

        if (ViewUtils.isEmpty(temperature_reading)) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.temperature_reading));
            allCompleted = false;
        } else {
            covidTBIntegrator.setTemperature_reading(temperature_reading.getText().toString());
        }

        if (health_care_worker.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.health_care_worker));
            allCompleted = false;
        } else {
            covidTBIntegrator.setHealth_care_worker(health_care_worker.getSelectedItem().toString());
        }

        if (dry_cough.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.dry_cough));
            allCompleted = false;
        } else {
            covidTBIntegrator.setDry_cough(dry_cough.getSelectedItem().toString());
        }

        if (shortness_of_breath.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.shortness_of_breath_in_the_past_week));
            allCompleted = false;
        } else {
            covidTBIntegrator.setShortness_of_breath(shortness_of_breath.getSelectedItem().toString());
        }

        if (history_of_fever.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.history_of_fever_in_the_past_week));
            allCompleted = false;
        } else {
            covidTBIntegrator.setHistory_of_fever(history_of_fever.getSelectedItem().toString());
        }

        if (muscle_aches.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.muscle_aches));
            allCompleted = false;
        } else {
            covidTBIntegrator.setMuscle_aches(muscle_aches.getSelectedItem().toString());
        }

        if (not_vaccinated.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.not_vaccinated_for_covid_19));
            allCompleted = false;
        } else {
            covidTBIntegrator.setNot_vaccinated(not_vaccinated.getSelectedItem().toString());
        }

        if (loss_of_taste.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.loss_of_taste));
            allCompleted = false;
        } else {
            covidTBIntegrator.setLoss_of_taste(loss_of_taste.getSelectedItem().toString());
        }

        if (loss_of_sense.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.loss_of_sense_of_smell));
            allCompleted = false;
        } else {
            covidTBIntegrator.setLoss_of_sense(loss_of_sense.getSelectedItem().toString());
        }

        if (sore_throat.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.sore_throat));
            allCompleted = false;
        } else {
            covidTBIntegrator.setSore_throat(sore_throat.getSelectedItem().toString());
        }

        if (headache.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.headache));
            allCompleted = false;
        } else {
            covidTBIntegrator.setHeadache(headache.getSelectedItem().toString());
        }

        if (international_travel.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.international_travel_or_lived_in_area_with_community_spread_covid));
            allCompleted = false;
        } else {
            covidTBIntegrator.setInternational_travel(international_travel.getSelectedItem().toString());
        }

        if (close_contact.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.close_contact_with_known_suspected_COVID_19_patient));
            allCompleted = false;
        } else {
            covidTBIntegrator.setClose_contact(close_contact.getSelectedItem().toString());
        }

        if (history_of_chronic.getSelectedItemPosition() == 0) {
            OpenMRSCustomHandler.showDialogMessage(getContext(), "Please select this field: " + getResources().getString(R.string.history_of_chronic_non_communicable_disease));
            allCompleted = false;
        } else {
            covidTBIntegrator.setHistory_of_chronic(history_of_chronic.getSelectedItem().toString());
        }


        if (allCompleted) {
            //If all validations are correct then save consumption
            covidTBIntegrator.save();
            //go out of this activity

        }

    }

    private CovidTBIntegrator updateCovidTBIntegrator(CovidTBIntegrator covidTBIntegrator) {
        updateCovidTBIntegratorWithData(covidTBIntegrator);
        return covidTBIntegrator;
    }


    private List<CovidTBIntegrator> createCovidTBIntegrator() {
        CovidTBIntegrator covidTBIntegrator = new CovidTBIntegrator();
        updateCovidTBIntegratorWithData(covidTBIntegrator);
        return covidTBIntegratorMultiple;
    }

    @Override
    public void hideSoftKeys() {
        View view = this.getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(this.getActivity());
        }
        InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void setProgressBarVisibility(boolean visibility) {
        progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }


    @Override
    public void startCommodityDashboardActivity() {
        Intent intent = new Intent(getActivity(), CommodityActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean areFieldsNotEmpty() {
        return (!patientUUID.isEmpty());
    }


    private void fillFields(final CovidTBIntegrator covidTBIntegrator) {

        if (covidTBIntegrator != null) {
            addMoreBtn.setVisibility(View.GONE);
            deleteCommodity.setVisibility(View.VISIBLE);
            isUpdateCovidTBIntegrator = true;
            updatedCovidTBIntegrator = covidTBIntegrator;
            //Change to Update CovidTBIntegrator Form
            //Change to Update CovidTBIntegrator Form


        }
    }

    private void addListeners() {

    }

    public void covidTBIntegratorAdapter(Spinner spinner, List<String> records) {
        ArrayAdapter arrayAdapter_department = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, records) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setPadding(5, 20, 0, 0);
                FontsUtil.setFont(text, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
                return view;
            }
        };

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.submit_done_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSubmit:
                submitAction();
                return true;
            default:
                // Do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitAction() {
        //Validate the form fields to address those clicking the submit forms directly without going through the add more
        validateSubmition();
        if (allCompleted) {
            if (isUpdateCovidTBIntegrator) {
                mPresenter.confirmUpdate(updateCovidTBIntegrator(updatedCovidTBIntegrator));
            } else {
                mPresenter.confirmRegister(createCovidTBIntegrator());
                startCommodityDashboardActivity();
                finishCovidTBIntegratorInfoActivity();
            }
        }
        allCompleted = true;
    }

    public static CovidTBIntegratorFragment newInstance() {
        return new CovidTBIntegratorFragment();
    }

    private void addMore() {
        admCovidTBIntegrator = new CovidTBIntegrator();
        updateCovidTBIntegratorWithData(admCovidTBIntegrator);
    }

    private void deleteCommodity() {
        mPresenter.deleteCovidTBIntegrator();
        //new Delete().from(ReceiptItem.class).where("receiptId = ?", lastid).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

