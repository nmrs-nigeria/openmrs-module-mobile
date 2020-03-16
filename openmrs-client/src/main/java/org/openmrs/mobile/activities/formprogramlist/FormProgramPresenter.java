package org.openmrs.mobile.activities.formprogramlist;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.FormResource;
import org.openmrs.mobile.utilities.FormService;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FormProgramPresenter extends BasePresenter implements FormProgramContract.Presenter {
    private static String[] formsStringArray = null;

    private FormProgramContract.View view;
    private Long patientId;
    private String programName;
    private List<FormResource> formResourceList;
    private EncounterDAO encounterDAO;

    public FormProgramPresenter(FormProgramContract.View view, long patientId, String programName) {
        this.view = view;
        this.view.setPresenter(this);
        this.patientId = patientId;
        this.programName = programName;
        this.encounterDAO = new EncounterDAO();
    }

    public FormProgramPresenter(FormProgramContract.View view, long patientId, EncounterDAO encounterDAO) {
        this.view = view;
        this.view.setPresenter(this);
        this.patientId = patientId;
        this.encounterDAO = encounterDAO;
    }

    @Override
    public void subscribe() {
        loadFormResourceList();
        view.bindDrawableResources();
    }

    @Override
    public void loadFormResourceList() {
        formResourceList = new ArrayList<>();
        List<String> formName = new ArrayList<>();
        List<FormResource> allFormResourcesList = FormService.getFormResourceList();
        for (FormResource formResource : allFormResourcesList) {
            List<FormResource> valueRef = formResource.getResourceList();
            String valueRefString = null;

            for (FormResource resource : valueRef) {
                if (resource.getName().equals("json")) {
                    valueRefString = resource.getValueReference();
                }
            }
            // Get the forms (REMEMBER to set default if there is issue - get reference from the old)
            if (!StringUtils.isBlank(valueRefString)) {
                formResourceList.add(formResource);
            }

        }

        int size = formResourceList.size();
        formsStringArray = new String[size];
        for (int i = 0; i < size; i++) {
            formsStringArray[i] = formResourceList.get(i).getName();
            formName.add(formResourceList.get(i).getName());
        }
        view.showFormList(formsStringArray,programName,formName);
    }

    @Override
    public void listItemClicked(int position, String formName) {
        List<FormResource> valueRef = formResourceList.get(position).getResourceList();
        String valueRefString = null;
        for (FormResource resource : valueRef) {
            if (resource.getName().equals("json"))
                valueRefString = resource.getValueReference();
        }

        EncounterType encType = encounterDAO.getEncounterTypeByFormName(formsStringArray[position]);
        if (encType != null) {
            String encounterType = encType.getUuid();
            view.startFormDisplayActivity(formName, patientId, valueRefString, encounterType);
        } else {
            view.showError("There is no encounter type called " + formName);
        }
    }

}
