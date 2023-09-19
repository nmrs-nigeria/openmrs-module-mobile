package org.openmrs.mobile.activities.patientdashboard.fingerprint;

public   class ItemFingerPrint {
    private  String position;
    private  String quality ;
    private String sn;
/*
 sn=  // counter
                position=  // finger print position // left thumb etc
                quality=  // quality capture
 */
    public ItemFingerPrint(  String sn,String position, String quality) {
        this.position = position;
        this.quality = quality;
        this.sn = sn;
    }

    public String getPosition() {
        return position;
    }

    public String getQuality() {
        return quality;
    }

    public String getSn() {
        return sn;
    }
}
