package pbsprocessor.util;

import pbsprocessor.model.EncounterType;

public class ApplicationConstants {
    public static final String REST_ENDPOINT = "/ws/rest/v1/";
    public static final String PBS_PASSWORD_VERSION ="";
    public static final String PBS_PASSWORD = "";

    public abstract static class EncounterTypes {
        public static final String VITALS = "67a71486-1a54-468f-ac3e-7091a9a79584";
        public static String[] ENCOUNTER_TYPES_DISPLAYS = {EncounterType.VITALS, EncounterType.ADMISSION, EncounterType.DISCHARGE, EncounterType.VISIT_NOTE};
    }
}


