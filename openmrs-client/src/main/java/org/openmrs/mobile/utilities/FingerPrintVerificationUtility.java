package org.openmrs.mobile.utilities;

import android.util.Base64;

import org.openmrs.mobile.activities.pbs.PatientBiometricContract;
import org.openmrs.mobile.activities.pbsverification.PatientBiometricVerificationContract;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.dao.FingerPrintDAO;
import org.openmrs.mobile.dao.FingerPrintVerificationDAO;

import java.util.List;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;

public class FingerPrintVerificationUtility {
    private JSGFPLib sgfplib;

    public FingerPrintVerificationUtility(JSGFPLib jsgfpLib) {
        sgfplib = jsgfpLib;
    }

//    public String CheckIfFingerAlreadyCaptured(List<PatientBiometricContract> fingerPrint) {
//        try {
//            int index = 0;
//            while (index < fingerPrint.size()) {
//                List<PatientBiometricContract> compare = new ArrayList<>(fingerPrint);
//                compare.remove(index);
//                String matchedPosition = containsDuplicate(fingerPrint.get(index).getTemplate(), compare);
//                if (matchedPosition !=null && !matchedPosition.isEmpty()) {
//                    return matchedPosition;
//                }
//                index++;
//            }
//        }catch (Exception ignored){
//
//        }
//        return null;
//    }

    public String CheckIfFingerAlreadyCaptured(String newTemplate, List<PatientBiometricVerificationContract> compare) {
        if (compare == null || compare.size() == 0) return null;

        boolean[] matched = new boolean[1];
        try {
            byte[] unknownTemplateArray = Base64.decode(newTemplate, Base64.NO_WRAP);

            for (PatientBiometricVerificationContract each : compare) {
                int[] matchScore = new int[1];
                if (each.getTemplate() != null) {

                    debugMessage("Checking against : " + each.getPatienId() + " finger: " + each.getFingerPositions().name());

                    byte[] fingerTemplate = Base64.decode(each.getTemplate(), Base64.NO_WRAP);
                    long iError = sgfplib.MatchIsoTemplate(fingerTemplate, 0, unknownTemplateArray, 0, SGFDxSecurityLevel.SL_ABOVE_NORMAL, matched);
                    if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        if (matched[0]) {
                            sgfplib.GetIsoMatchingScore(fingerTemplate, 0, unknownTemplateArray, 0, matchScore);
                            debugMessage("found match : " + each.getFingerPositions() + " score - " + matchScore[0]);
                            return decodeFingerPosition(each.getFingerPositions().name());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            debugMessage(ex.toString());
        }
        return null;
    }

    public String CheckIfFingerAlreadyCapturedOnDevice(String newTemplate) {
        List<PatientBiometricVerificationContract> compare = new FingerPrintVerificationDAO().getAllFingerPrintsOnDevice();

        if (compare == null || compare.size() == 0) return null;

        boolean[] matched = new boolean[1];
        try {
            byte[] unknownTemplateArray = Base64.decode(newTemplate, Base64.NO_WRAP);

            for (PatientBiometricVerificationContract each : compare) {
                int[] matchScore = new int[1];
                if (each.getTemplate() != null) {
                    debugMessage("Checking against : " + each.getPatienId() + " finger: " + each.getFingerPositions().name());

                    byte[] fingerTemplate = Base64.decode(each.getTemplate(), Base64.NO_WRAP);
                    long iError = sgfplib.MatchIsoTemplate(fingerTemplate, 0, unknownTemplateArray, 0, SGFDxSecurityLevel.SL_ABOVE_NORMAL, matched);
                    if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        if (matched[0]) {
                            sgfplib.GetIsoMatchingScore(fingerTemplate, 0, unknownTemplateArray, 0, matchScore);
                            debugMessage("found match : " + each.getFingerPositions() + " score - " + matchScore[0]);
                            return decodeFingerPosition(each.getFingerPositions().name());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            debugMessage(ex.toString());
        }
        return null;
    }


    public String MATCH = "match";

    public String checkAlready(PatientBiometricVerificationContract pbs, String patientID) {
        List<PatientBiometricContract> patientAllPrints = new FingerPrintDAO().getSinglePatientPBS(Long.valueOf(patientID));

        if (patientAllPrints == null || patientAllPrints.size() == 0) return "No prints base Found";

        boolean[] matched = new boolean[1];
        try {
            byte[] unknownTemplateArray = Base64.decode(pbs.getTemplate(), Base64.NO_WRAP);
            boolean fingerInBase = false;
            for (PatientBiometricContract baseFinger : patientAllPrints) {
                int[] matchScore = new int[1];
                if (baseFinger.getTemplate() != null) {
                    if (baseFinger.getFingerPositions().equals(pbs.getFingerPositions()))
                        fingerInBase = true;

                    byte[] fingerTemplate = Base64.decode(baseFinger.getTemplate(), Base64.NO_WRAP);
                    long iError = sgfplib.MatchIsoTemplate(fingerTemplate, 0, unknownTemplateArray, 0, SGFDxSecurityLevel.SL_ABOVE_NORMAL, matched);
                    if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        if (matched[0]) {
                            sgfplib.GetIsoMatchingScore(fingerTemplate, 0, unknownTemplateArray, 0, matchScore);
                            if (baseFinger.getFingerPositions().equals(pbs.getFingerPositions()))
                                return MATCH;
                            return "Check the finger position\n"+
                                    decodeFingerPosition(pbs.getFingerPositions().name()) +
                                    " recaptured match at " + decodeFingerPosition(baseFinger.getFingerPositions().name())
                                    + " of the base";
                        }
                    } else{
                        return  "Match Error: "+getDeviceErrors((int)iError)+". Recommendation connect the device properly";
                    }
                }
            }

            if (fingerInBase) {
                return "Place the finger well and try again\n"+
                        decodeFingerPosition(pbs.getFingerPositions().name())
                        + " recaptured did no match the base";
            } else {
                return decodeFingerPosition(pbs.getFingerPositions().name())
                        + " recaptured does no have a base  and doest not match any finger of the patient in the base capture";
            }

        } catch (Exception ex) {
            OpenMRSCustomHandler.writeLogToFile("Error is comparing print " + ex.toString());
            // debugMessage(ex.toString());
            return "Error occur in matching";
        }
    }


    public String decodeFingerPosition(String fingerPositionName) {

        switch (fingerPositionName.trim()) {
            case "RightThumb":
                return "Right Thumb";
            case "RightIndex":
                return "Right Index";
            case "RightMiddle":
                return "Right Middle";
            case "RightWedding":
                return "Right Ring";
            case "RightSmall":
                return "Right Pinky";
            case "LeftThumb":
                return "Left Thumb";
            case "LeftIndex":
                return "Left Index";
            case "LeftMiddle":
                return "Left Middle";
            case "LeftWedding":
                return "Left ring";
            case "LeftSmall":
                return "Left Pinky";
            default:
                return "";
        }
    }

    public String getDeviceErrors(int errorCode) {

        switch (errorCode) {

            case 1:
                return "CREATION FAILED";
            case 2:
                return "FUNCTION FAILED";
            case 3:
                return "INVALID PARAM";
            case 4:
                return "NOT USED";
            case 5:
                return "DLL LOAD FAILED";
            case 6:
                return "DLL LOAD FAILED DRV";
            case 7:
                return "DLL LOAD FAILED ALGO";
            case 8:
                return "No LONGER SUPPORTED";
            case 51:
                return "SYS LOAD FAILED";
            case 52:
                return "INITIALIZE FAILED";
            case 53:
                return "LINE DROPPED";
            case 54:
                return "TIME OUT";
            case 55:
                return "DEVICE NOT FOUND";
            case 56:
                return "Driver LOAD FAILED";
            case 57:
                return "WRONG IMAGE";
            case 58:
                return "LACK OF BANDWIDTH";
            case 59:
                return "DEV ALREADY OPEN";
            case 60:
                return "GET Serial Number FAILED";
            case 61:
                return "UNSUPPORTED DEV";
            case 101:
                return "FEAT NUMBER";
            case 102:
                return "INVALID TEMPLATE TYPE";
            case 103:
                return "INVALID TEMPLATE1";
            case 104:
                return "INVALID TEMPLATE2";
            case 105:
                return "EXTRACT FAIL";
            case 106:
                return "MATCH FAIL";
            default:
                return "";
        }
    }

    private void debugMessage(String message) {
        System.out.print(message);
    }
}
