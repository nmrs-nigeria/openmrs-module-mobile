package org.openmrs.mobile.activities.patientdashboard.covid19tb;

import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardMainPresenterImpl;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;

import rx.android.schedulers.AndroidSchedulers;

public class PatientDashboardCovid19TBPresenter extends PatientDashboardMainPresenterImpl implements PatientDashboardContract.PatientCovid19TBPresenter{

    private PatientDashboardContract.ViewPatientCovid19TB mPatientCovid19TBView;
    private VisitDAO visitDAO;

    public PatientDashboardCovid19TBPresenter(String id, PatientDashboardContract.ViewPatientCovid19TB mPatientCovid19TBView) {
        this.mPatientCovid19TBView = mPatientCovid19TBView;
        this.mPatient = new PatientDAO().findPatientByID(id);
        this.visitDAO = new VisitDAO();
        mPatientCovid19TBView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        addSubscription(visitDAO.getVisitsByPatientID(mPatient.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visits -> mPatientCovid19TBView.showCovid19TBEncounter()
                ));

    }

}