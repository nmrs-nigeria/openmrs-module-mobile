package org.openmrs.mobile.sync;

import java.util.List;

/**
 * The SimpleLog class is used to log and retrieve information about synchronization operations.
 */
public class SimpleLog {
    private String what;
    private boolean success = false;
    private String error = "";
    private boolean available = true;
    private int itemCount;

    /**
     * Constructs a SimpleLog with all fields specified.
     *
     * @param what      the description of the log
     * @param success   whether the operation was successful
     * @param error     any error message
     * @param available whether the log is available
     * @param itemCount the count of items processed
     */
    public SimpleLog(String what, boolean success, String error, boolean available, int itemCount) {
        this.what = what;
        this.success = success;
        this.error = error;
        this.available = available;
        this.itemCount = itemCount;
    }

    /**
     * Constructs a SimpleLog with what, success, and error fields.
     *
     * @param what    the description of the log
     * @param success whether the operation was successful
     * @param error   any error message
     */
    public SimpleLog(String what, boolean success, String error) {
        this.what = what;
        this.success = success;
        this.error = error;
    }

    /**
     * Constructs a SimpleLog with what, success, error, and available fields.
     *
     * @param what      the description of the log
     * @param success   whether the operation was successful
     * @param error     any error message
     * @param available whether the log is available
     */
    public SimpleLog(String what, boolean success, String error, boolean available) {
        this.what = what;
        this.success = success;
        this.error = error;
        this.available = available;
    }

    /**
     * Constructs a SimpleLog with what, error, success, available, and itemCount fields.
     *
     * @param what      the description of the log
     * @param error     any error message
     * @param success   whether the operation was successful
     * @param available whether the log is available
     * @param itemCount the count of items processed
     */
    public SimpleLog(String what, String error, boolean success, boolean available, int itemCount) {
        this.what = what;
        this.error = error;
        this.success = success;
        this.available = available;
        this.itemCount = itemCount;
    }

    /**
     * Constructs a SimpleLog with what, success, and available fields.
     *
     * @param what      the description of the log
     * @param success   whether the operation was successful
     * @param available whether the log is available
     */
    public SimpleLog(String what, boolean success, boolean available) {
        this.what = what;
        this.success = success;
        this.available = available;
    }

    /**
     * Constructs a SimpleLog with what and success fields.
     *
     * @param what    the description of the log
     * @param success whether the operation was successful
     */
    public SimpleLog(String what, boolean success) {
        this.what = what;
        this.success = success;
    }

    /**
     * Gets the error message of the log.
     *
     * @return the error message
     */
    public String getError() {
        return error+"\n";
    }

    /**
     * Gets the description of the log.
     *
     * @return the description
     */
    public String getWhat() {
        return what;
    }

    /**
     * Checks if the operation was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if the log is available.
     *
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Gets the count of items processed.
     *
     * @return the item count
     */
    public int getItemCount() {
        return itemCount;
    }

    /**
     * Creates an array of Strings representing the log values for PBS.
     *
     * @param simpleLogList the list of SimpleLog instances
     * @return an array of Strings containing the log values and errors
     */
    public static String[] createSimpleLogValuePBS(List<SimpleLog> simpleLogList) {
        StringBuilder result = new StringBuilder();
        StringBuilder errorLog = new StringBuilder();

        for (SimpleLog log : simpleLogList) {
            result.append(log.getWhat());
            result.append(" synced ");
            result.append(log.isSuccess() ? "successfully" : "unsuccessfully");

            result.append(", Count: ");
            result.append(log.getItemCount());
            result.append("  ");

//            result.append("Available: ");
//            result.append(log.isAvailable());
//            result.append("\t\t");
            errorLog.append(log.getError());
        }

        return new String[]{result.toString(), errorLog.toString()};
    }

    /**
     * Creates an array of Strings representing the log values for Forms.
     *
     * @param simpleLogList the list of SimpleLog instances
     * @return an array of Strings containing the log values and errors
     */
    public static String[] createSimpleLogValueForms(List<SimpleLog> simpleLogList) {
        StringBuilder result = new StringBuilder();
        StringBuilder errorLog = new StringBuilder();

        for (SimpleLog log : simpleLogList) {
            result.append(log.getWhat());
            result.append(" synced ");
            result.append(log.isSuccess() ? "successfully" : "unsuccessfully");

            errorLog.append(log.getError());
        }

        return new String[]{result.toString(), errorLog.toString()};
    }

    /**
     * Creates an array of Strings representing the log values for Bio.
     *
     * @param simpleLogList the list of SimpleLog instances
     * @return an array of Strings containing the log values and errors
     */
    public static String[] createSimpleLogValueBio(List<SimpleLog> simpleLogList) {
        StringBuilder result = new StringBuilder();
        StringBuilder errorLog = new StringBuilder();

         for (SimpleLog log : simpleLogList) {
             if (log.available) {
                 result.append(log.getWhat());
                 result.append(" synced ");
                 result.append(log.isSuccess() ? "successfully" : "unsuccessfully");

//            result.append("Available: ");
//            result.append(log.isAvailable());
                 // result.append("\t\t");}
                 errorLog.append(log.getError());
             }
         }
        return new String[]{result.toString(), errorLog.toString()};
    }
}
