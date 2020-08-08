package org.openmrs.mobile.utilities;

import org.openmrs.mobile.application.OpenMRS;

import java.util.*;
import java.nio.charset.*;

public class IdGeneratorUtil  {

    public static String getAlphaNumericString(int n)
    {

        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }

    public static String getIdentifierGenerated(){
        String systemId = OpenMRS.getInstance().getSystemId();
        String[] systemArray = systemId.split("-");
        String curTime = DateUtils.convertTimeIdentifier(System.currentTimeMillis());
        String[] timeArray = curTime.split("/");
        return "CT-" + systemArray[0] +  timeArray[0]+timeArray[1]+timeArray[2] ;
    }
    public static String getIdentifierGeneratedHospital(){
        String systemId = OpenMRS.getInstance().getSystemId();
        String[] systemArray = systemId.split("-");
        String curTime = DateUtils.convertTimeIdentifier(System.currentTimeMillis());
        String[] timeArray = curTime.split("/");
        return "HO-" + systemArray[0] + timeArray[0]+timeArray[1]+timeArray[2];
    }

}
