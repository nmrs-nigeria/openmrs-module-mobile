package org.openmrs.mobile.security;

import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashMethods {
    // Model is use for PBS hash and the manufacturer is use for PBS hash version
    public  static  String getPBSHash(String patientUUID,
                                      String dateCreated,
                                      int imageQuality,
                                      String serialNumber,
                                      String fingerPosition){


        String password= ApplicationConstants.PBS_PASSWORD;
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String inputString =patientUUID+serialNumber.trim()+password+dateCreated.split("T")[0]+fingerPosition+imageQuality;

            md.update(inputString.getBytes());

            byte[] digest = md.digest();

            for(byte b: digest){
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

         return  null;
    }

    public static String getPBSHashTime(String uuid, Long time, String dateCreated) {
                 String password= ApplicationConstants.PATIENT_IMPORT_PASSWORD;
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String inputString =uuid+time+password+dateCreated;

            md.update(inputString.getBytes());

            byte[] digest = md.digest();

            for(byte b: digest){
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return  null;
    }
}
