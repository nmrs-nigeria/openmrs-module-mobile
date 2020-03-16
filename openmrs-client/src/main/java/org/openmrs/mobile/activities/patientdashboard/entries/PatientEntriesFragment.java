package org.openmrs.mobile.activities.patientdashboard.entries;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.formdisplay.FormDisplayActivity;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardFragment;
import org.openmrs.mobile.activities.visitdashboard.VisitDashboardActivity;
import org.openmrs.mobile.bundle.FormFieldsWrapper;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Form;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.FormService;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.openmrs.mobile.utilities.FormService.getFormResourceByName;

public class PatientEntriesFragment extends PatientDashboardFragment implements PatientDashboardContract.ViewPatientEntries {

    private ExpandableListView mExpandableListView;
    private TextView mEmptyListView;


    public static final int REQUEST_CODE_FOR_VISIT = 1;

    public static PatientEntriesFragment newInstance() {
        return new PatientEntriesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setPresenter(mPresenter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // This method is intentionally empty
    }

    @Override
    public void updateList(List<Encountercreate> visitEncounters) {
        final String[] displayableEncounterTypes = ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS;
//        final HashSet<String> displayableEncounterTypesArray = new HashSet<>(Arrays.asList(displayableEncounterTypes));

        List<Encountercreate> displayableEncounters  = new ArrayList<>();

        for (Encountercreate encounter : visitEncounters) {
            String encounterTypeDisplay = encounter.getFormname();
            encounter.pullObslistLocal();
//            if (displayableEncounterTypesArray.contains(encounterTypeDisplay)) {
            displayableEncounters.add(encounter);
//            }
        }

        EntriesExpandableListAdapter expandableListAdapter = new EntriesExpandableListAdapter(this.getActivity(), displayableEncounters,(PatientDashboardEntriesPresenter) mPresenter);
        mExpandableListView.setAdapter(expandableListAdapter);
        mExpandableListView.setGroupIndicator(null);
    }

    @Override
    public void showErrorToast(String message) {
        ToastUtil.error(message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_visit_dashboard, null, false);
        mEmptyListView = (TextView) root.findViewById(R.id.visitDashboardEmpty);
        FontsUtil.setFont(mEmptyListView, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
        mExpandableListView = (ExpandableListView) root.findViewById(R.id.visitDashboardExpList);
        mExpandableListView.setEmptyView(mEmptyListView);
        setEmptyListVisibility(false);


        return root;
    }


    @Override
    public void dismissCurrentDialog() {
        ((PatientDashboardActivity) getActivity()).dismissCustomFragmentDialog();
    }

    @Override
    public void setEmptyListVisibility(boolean visibility) {
        if (visibility) {
            mEmptyListView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void goToVisitDashboard(Long visitID) {
        Intent intent = new Intent(this.getActivity(), VisitDashboardActivity.class);
        intent.putExtra(ApplicationConstants.BundleKeys.VISIT_ID, visitID);
        startActivityForResult(intent, REQUEST_CODE_FOR_VISIT);
    }

    @Override
    public void showStartVisitDialog(boolean isVisitPossible) {
        PatientDashboardActivity activity = (PatientDashboardActivity) getActivity();
        if (activity.getSupportActionBar() != null) {
            if (isVisitPossible) {
                activity.showStartVisitDialog(activity.getSupportActionBar().getTitle());
            }
            else {
                activity.showStartVisitImpossibleDialog(activity.getSupportActionBar().getTitle());
            }
        }
    }

    @Override
    public void showStartVisitProgressDialog() {
        ((PatientDashboardActivity) getActivity()).showProgressDialog(R.string.action_starting_visit);
    }
    @Override
    public void startFormDisplayActivity(Encountercreate encounter) {
        Form form = FormService.getFormByUuid(getFormResourceByName(encounter.getFormname()).getUuid());
        String enc = encounter.getEncounterDatetime();
        String[] env_arr = enc.split(" ");
        if(form != null){
            Intent intent = new Intent(getContext(), FormDisplayActivity.class);
            intent.putExtra(ApplicationConstants.BundleKeys.FORM_NAME, encounter.getFormname());
            intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, encounter.getPatientId());
            intent.putExtra(ApplicationConstants.BundleKeys.VALUEREFERENCE, form.getValueReference());
            intent.putExtra(ApplicationConstants.BundleKeys.ENCOUNTERTYPE, encounter.getEncounterType());
            intent.putExtra(ApplicationConstants.BundleKeys.ENCOUNTERDATETIME, env_arr[0]);
            intent.putExtra(ApplicationConstants.BundleKeys.ENTRIES_ID, encounter.getId());
            intent.putParcelableArrayListExtra(ApplicationConstants.BundleKeys.FORM_FIELDS_LIST_BUNDLE, FormFieldsWrapper.create(encounter,form));
            startActivity(intent);
        } else {
            ToastUtil.notify(getString(R.string.form_error));
        }

    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            try {
                PatientDashboardActivity.hideFABs(true);
                PatientDashboardActivity.showVisitFABs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
