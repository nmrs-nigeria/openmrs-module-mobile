package org.openmrs.mobile.utilities;

import java.util.ArrayList;
import java.util.List;

public class VersioningUtil {
    public static List<Integer> extractVersion(String versionString) {
        List<Integer> versionList = new ArrayList<>();
        if(versionString!=null)
            for (char c : versionString.toCharArray()) {
                if (Character.isDigit(c) ) {
                    StringBuilder currentNumber = new StringBuilder();
                    currentNumber.append(c);
                    versionList.add(Integer.parseInt(currentNumber.toString()));
                }
            }

        return versionList;
    }
    public static  int compareVersions( List<Integer> version1,  List<Integer> version2) {
        // Compare major version
        int minL = Math.min(version1.size(), version2.size());
        int maxL = Math.max(version1.size(), version2.size());
        for (int i =0; i<minL ; i++   ){
            if (version1.get(i) < version2.get(i)) {
                return -1;
            } else if (version1.get(i) > version2.get(i)) {
                return 1;
            }
        }
        // Versions are equal
        return 0;
    }


}
