package org.openmrs.mobile.sync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The PatientLog class is used to log and retrieve information about a patient.
 */
public class PatientLog {
    private String identifier;
    private String name;
    private String dob;
    private String gender;
    private String latitude;
    private String longitude;
    private String bio;
    private String pbs;
    private String Forms;
    private String uuid;
    private final String dateTime;
    private String mobileVersion;
    private String errorLog;
    private boolean  geoCoordinateSync;


    /**
     * Constructs a PatientLog with the current date and time.
     */
    public PatientLog() {
        this.dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    /**
     * Constructs a PatientLog with all fields specified.
     *
     * @param identifier    the patient's identifier
     * @param name          the patient's name
     * @param dob           the patient's date of birth
     * @param gender        the patient's gender
     * @param latitude      the latitude of the patient's location
     * @param longitude     the longitude of the patient's location
     * @param bio           the patient's biographical information
     * @param pbs           the patient's PBS information
     * @param forms         the patient's form information
     * @param uuid          the patient's UUID
     * @param mobileVersion the version of the mobile app
     * @param errorLog      any error messages
     * @param dateTime      the date and time of the log
     * @param geoCoordinateSync      the Coordinate values sync status log
     */
    public PatientLog(String identifier, String name, String dob, String gender, String latitude, String longitude, String bio, String pbs, String forms, String uuid, String mobileVersion, String errorLog, String dateTime, boolean geoCoordinateSync) {
        this.identifier = identifier;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bio = bio;
        this.pbs = pbs;
        this.Forms = forms;
        this.uuid = uuid;
        this.dateTime = dateTime;
        this.mobileVersion = mobileVersion;
        this.errorLog = errorLog;
        this.geoCoordinateSync=geoCoordinateSync;
    }

    /**
     * Gets the patient's identifier.
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the patient's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the patient's date of birth.
     *
     * @return the date of birth
     */
    public String getDob() {
        return dob;
    }

    /**
     * Gets the patient's gender.
     *
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Gets the latitude of the patient's location.
     *
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude of the patient's location.
     *
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Gets the patient's biographical information.
     *
     * @return the biographical information
     */
    public String getBio() {
        return bio;
    }

    /**
     * Gets the patient's PBS information.
     *
     * @return the PBS information
     */
    public String getPbs() {
        return pbs;
    }

    /**
     * Gets the patient's form information.
     *
     * @return the form information
     */
    public String getForms() {
        return Forms;
    }

    /**
     * Gets the patient's UUID.
     *
     * @return the UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Gets the date and time of the log.
     *
     * @return the date and time
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Gets the version of the mobile app.
     *
     * @return the mobile version
     */
    public String getMobileVersion() {
        return mobileVersion;
    }

    /**
     * Gets any error messages.
     *
     * @return the error messages
     */
    public String getErrorLog() {
        return errorLog;
    }

    /**
     * Sets the patient's identifier.
     *
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the patient's name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the patient's date of birth.
     *
     * @param dob the date of birth to set
     */
    public void setDob(String dob) {
        this.dob = dob;
    }

    /**
     * Sets the patient's gender.
     *
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Sets the latitude of the patient's location.
     *
     * @param latitude the latitude to set
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets the longitude of the patient's location.
     *
     * @param longitude the longitude to set
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the patient's biographical information.
     *
     * @param bio the biographical information to set
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Sets the patient's PBS information.
     *
     * @param pbs the PBS information to set
     */
    public void setPbs(String pbs) {
        this.pbs = pbs;
    }

    /**
     * Sets the patient's form information.
     *
     * @param forms the form information to set
     */
    public void setForms(String forms) {
        this.Forms = forms;
    }

    /**
     * Sets the patient's UUID.
     *
     * @param uuid the UUID to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Sets the version of the mobile app.
     *
     * @param mobileVersion the mobile version to set
     */
    public void setMobileVersion(String mobileVersion) {
        this.mobileVersion = mobileVersion;
    }

    /**
     * Sets any error messages.
     *
     * @param errorLog the error messages to set
     */
    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    /**
     * Checks if geo-coordinate synchronization is enabled.
     *
     * @return {@code true} if geo-coordinate synchronization is enabled, {@code false} otherwise.
     */
    public boolean isGeoCoordinateSync() {
        return geoCoordinateSync;
    }

    /**
     * Sets the geo-coordinate synchronization status.
     *
     * @param geoCoordinateSync {@code true} to enable geo-coordinate synchronization, {@code false} to disable it.
     */
    public void setGeoCoordinateSync(boolean geoCoordinateSync) {
        this.geoCoordinateSync = geoCoordinateSync;
    }
}
