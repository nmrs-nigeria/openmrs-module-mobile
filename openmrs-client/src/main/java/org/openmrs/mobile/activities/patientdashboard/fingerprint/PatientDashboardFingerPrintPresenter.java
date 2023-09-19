package org.openmrs.mobile.activities.patientdashboard.fingerprint;

import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardMainPresenterImpl;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;

import rx.android.schedulers.AndroidSchedulers;

public class PatientDashboardFingerPrintPresenter extends PatientDashboardMainPresenterImpl implements PatientDashboardContract.PatientFingerPrintPresenter{

    private PatientDashboardContract.ViewPatientFingerPrints mPatientFingerPrintView;
    private VisitDAO visitDAO;

    public PatientDashboardFingerPrintPresenter(String id, PatientDashboardContract.ViewPatientFingerPrints mPatientFingerPrintView) {
        this.mPatientFingerPrintView = mPatientFingerPrintView;
        this.mPatient = new PatientDAO().findPatientByID(id);
        this.visitDAO = new VisitDAO();
        mPatientFingerPrintView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        addSubscription(visitDAO.getVisitsByPatientID(mPatient.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visits -> mPatientFingerPrintView.populateFingerPrints(mPatient.getId())
                ));

    }

}
