/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.utilities;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.models.Visit;

import java.util.ArrayList;
import java.util.List;

public class FilterUtil {

    /**
     * Used to filter list by specified query
     * Its possible to filter patients by: Name, Surname (Family Name) or ID.
     *
     * @param patientList list of patients to filter
     * @param query query that needs to be contained in Name, Surname or ID.
     * @return patient list filtered by query
     */
    static OpenMRSLogger logger = new OpenMRSLogger();

    public static List<Patient> getPatientsFilteredByQuery(List<Patient> patientList, String query) {
        List<Patient> filteredList = new ArrayList<>();

        for (Patient patient : patientList) {

            List<String> searchableWords = getPatientSearchableWords(patient);

            if (doesAnySearchableWordFitQuery(searchableWords, query)) {
                filteredList.add(patient);
            }

        }
        return filteredList;
    }

    public static List<Patient> getPatientsFilteredByQuery(List<Patient> patientList) {
        List<Patient> filteredList = new ArrayList<>();
        try {
            for (Patient patient : patientList) {

                List<Encountercreate> encountersPatientNotEligibleSynced = new Select()
                        .from(Encountercreate.class)
                        .where("patientid = ? AND synced = 1 AND eligble = ? AND formname = 'Risk Stratification Adult'", patient.getId(), "No")
                        .execute();
                List<Encountercreate> encountersPatientNegativeSynced = new Select()
                        .from(Encountercreate.class)
                        .where("patientid = ? AND synced = 1 AND eligble = ? AND formname = 'Client intake form'", patient.getId(), "No")
                        .execute();
                List<Encountercreate> encountersReferred = new Select()
                        .from(Encountercreate.class)
                        .where("patientid = ? AND synced = 1 AND formname = 'Client Referral Form'", patient.getId())
                        .execute();
                List<Encountercreate> encCreateListPharm = new Select()
                        .from(Encountercreate.class)
                        .where("patientid = ? AND synced = 1 AND formname = 'Pharmacy Order Form'", patient.getId())
                        .execute();

                if (!encountersPatientNotEligibleSynced.isEmpty()) {
                    new PatientDAO().deletePatient(patient.getId());

                    new Delete().from(Encountercreate.class).where("patientId = ?", patient.getId()).execute();
                } else if (!encountersPatientNegativeSynced.isEmpty()) {
                    new PatientDAO().deletePatient(patient.getId());
                    new Delete().from(Encountercreate.class).where("patientId = ?", patient.getId()).execute();
                } else if (!encountersReferred.isEmpty()) {
                    new PatientDAO().deletePatient(patient.getId());
                    new Delete().from(Encountercreate.class).where("patientId = ?", patient.getId()).execute();
                } else if (!encCreateListPharm.isEmpty()) {
                    new PatientDAO().deletePatient(patient.getId());
                    new Delete().from(Encountercreate.class).where("patientId = ?", patient.getId()).execute();
                } else {
                    filteredList.add(patient);
                }
            }
        } catch (Exception e) {
            logger.e("Error syncing: ", e);
        }
        return filteredList;
    }

    public static List<Visit> getPatientsWithActiveVisitsFilteredByQuery(List<Visit> visitList, String query) {
        List<Visit> filteredList = new ArrayList<>();

        for (Visit visit : visitList) {
            Patient patient = visit.getPatient();
            List<String> patientsWithActiveVisitsSearchableWords = new ArrayList<>();
            patientsWithActiveVisitsSearchableWords.addAll(getVisitSearchableWords(visit));
            patientsWithActiveVisitsSearchableWords.addAll(getPatientSearchableWords(patient));

            if (doesAnySearchableWordFitQuery(patientsWithActiveVisitsSearchableWords, query)) {
                filteredList.add(visit);
            }
        }
        return filteredList;
    }

    private static List<String> getPatientSearchableWords(Patient patient) {
        String patientIdentifier = patient.getIdentifier().getIdentifier();
        String fullName = patient.getName().getNameString();
        String givenFamilyName = patient.getName().getGivenName() + " "
                + patient.getName().getFamilyName();

        List<String> searchableWords = new ArrayList<>();
        searchableWords.add(patientIdentifier);
        searchableWords.add(fullName);
        searchableWords.add(givenFamilyName);

        return searchableWords;
    }

    private static List<String> getVisitSearchableWords(Visit visit) {
        String visitPlace = visit.getLocation().getDisplay();
        String visitType = visit.getVisitType().getDisplay();

        List<String> searchableWords = new ArrayList<>();
        searchableWords.add(visitPlace);
        searchableWords.add(visitType);

        return searchableWords;
    }

    private static boolean doesAnySearchableWordFitQuery(List<String> searchableWords, String query) {
        for (String searchableWord : searchableWords) {
            if (searchableWord != null) {
                int queryLength = query.trim().length();
                searchableWord = searchableWord.toLowerCase();
                query = query.toLowerCase().trim();
                boolean fits = searchableWord.length() >= queryLength && searchableWord.contains(query);
                if (fits) {
                    return true;
                }
            }
        }
        return false;
    }

}
