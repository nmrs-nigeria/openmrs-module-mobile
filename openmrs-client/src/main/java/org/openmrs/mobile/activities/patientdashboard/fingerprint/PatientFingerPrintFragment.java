package org.openmrs.mobile.activities.patientdashboard.fingerprint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardFragment;
import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;
import org.openmrs.mobile.models.FingerPrintLog;
import org.openmrs.mobile.utilities.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PatientFingerPrintFragment extends PatientDashboardFragment implements PatientDashboardContract.ViewPatientFingerPrints {

    private View rootView;
    private PatientDashboardActivity mPatientDashboardActivity;
    FingerPrintAdapter fingerPrintAdapter;
    TextView summary;

    public static PatientFingerPrintFragment newInstance() {
        return new PatientFingerPrintFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_patient_fingerprint, null, false);
        RecyclerView recyclerView = root.findViewById(R.id.item_finger_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fingerPrintAdapter = new FingerPrintAdapter(new ArrayList<>());
        recyclerView.setAdapter(fingerPrintAdapter);
        summary = root.findViewById(R.id.summary);


        return root;
    }

    @Override
    public void populateFingerPrints(Long patient_id) {
        List<PatientBiometricContract> dao = new FingerPrintDAO().getSinglePatientPBS(patient_id);
        List<PatientBiometricVerificationContract> daoVerification = new FingerPrintVerificationDAO().getSinglePatientPBS(patient_id);
       /*
        String recaptureMssage="\n*";
        FingerPrintLog fingerPrintLog = new Select().from(FingerPrintLog.class).where(
                "pid = ?",patient_id
        ).executeSingle();
        if(fingerPrintLog!=null){
            recaptureMssage="\nLast recapture count: "+fingerPrintLog.getRecapturedCount()
            +"\tLast recapture date: "+fingerPrintLog.getLastCapturedDate()+
                    "\tObserved on: "+ DateUtils.convertTime(fingerPrintLog.getTime());

        }*/
        List<ItemFingerPrint> dataList = new ArrayList<>();
        int index = 0;
        boolean synced = false;
        boolean syncedCompletely = true;

        if (dao.size() > 0) {
            for (PatientBiometricContract item : dao) {
                if (item.getSyncStatus() < 1) {
                    syncedCompletely = false;
                } else {
                    synced = true;
                }
                index++;
                dataList.add(new ItemFingerPrint(String.valueOf(index), item.getFingerPositions().toString(),
                        String.valueOf(item.getImageQuality())));
            }
        } else {
            if (daoVerification.isEmpty()) {
                summary.setText("Message: No fingerprint captured at the moment for the client using this device."//+recaptureMssage
                );
            }
        }
        if (index > 0) {
            dataList.add(new ItemFingerPrint("", "", ""));
            dataList.add(new ItemFingerPrint("", "", ""));
            index = 0;


        }

        // verification display
        if (daoVerification.size() > 0) {
            String pbsDetails = "";
            String pbsDetails2 = "";
            for (PatientBiometricVerificationContract item : daoVerification) {
                index++;
                dataList.add(new ItemFingerPrint(String.valueOf(index), item.getFingerPositions().toString(),
                        String.valueOf(item.getImageQuality())));
            }
        } // if empty the else for upper will show the empty


        if (!dataList.isEmpty()) {
            summary.setText(
                    "\nCaptured Fingers  Size: " + dao.size() + "\n" +
                            //  "Based Synced: "+synced +", Synced completely: "+syncedCompletely+
                            "\nRecaptured Fingers " + daoVerification.size() //+recaptureMssage
            );
            fingerPrintAdapter.setData(dataList);

        }
    }

}
