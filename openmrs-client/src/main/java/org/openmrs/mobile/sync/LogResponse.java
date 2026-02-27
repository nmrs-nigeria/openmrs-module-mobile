package org.openmrs.mobile.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogResponse {
    // when the process archive the main goal success is set to be true

    private  boolean isSuccess;
    // Unique string for identifying a patient for debugging
    private  String patientIdentifier;
    //Success or error message that occur in the process
    private  String message;
    // Recommendation for the log user
    private  String recommendation;
    //Process name or function name to help program go the line of code
    private  String eventType;
    // When a process have many request use index to separate each and it recommendation
    private  int  index =1;
    List<SimpleLog> simpleLogs =new ArrayList<>();

    // init with all result.
    public LogResponse(boolean isSuccess, String patientIdentifier, String message, String recommendation, String eventType) {
        this.isSuccess = isSuccess;
        this.patientIdentifier = patientIdentifier;
        this.message = message;
        this.recommendation = recommendation;
        this.eventType = eventType;
        index++;
    }
    public LogResponse(boolean isSuccess, String patientIdentifier, String message, String recommendation ) {
        this.isSuccess = isSuccess;
        this.patientIdentifier = patientIdentifier;
        this.message = message;
        this.recommendation = recommendation;
        this.eventType = "";
        index++;
    }
    public LogResponse(boolean isSuccess, String patientIdentifier, String message ) {
        this.isSuccess = isSuccess;
        this.patientIdentifier = patientIdentifier;
        this.message = message;
        this.recommendation = "";
        this.eventType = "";
        index++;
    }

    public LogResponse(String patientIdentifier) {
        this.isSuccess = false;
        this.message ="";
        this.recommendation = "";
        this.eventType = "";
        this.patientIdentifier =patientIdentifier;
    }


    public List<SimpleLog> getSimpleLogs() {
        return simpleLogs;
    }
    public void addSimpleLogs(SimpleLog simpleLog) {
         simpleLogs.add(simpleLog);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    // add more result
    public  void  appendLogs(boolean isSuccess, String message, String recommendation, String process){

        this.message=  this.message+"\t"+index+":\t"+message;
       this.recommendation=  this.recommendation+"\t"+index+":\t"+recommendation;
        this.eventType =  this.eventType +"\t"+index+":\t"+process;
        this.isSuccess= isSuccess;
        index++;
    }
 // add more result with  set the success. Track progress
    public  void  appendLogs( String message, String recommendation, String process){

        this.message=  this.message+"\t"+index+":\t"+message;
        this.recommendation=  this.recommendation+"\t"+index+":\t"+recommendation;
        this.eventType =  this.eventType +"\t"+index+":\t"+process;
        index++;
    }









// Format the response in to a single string for logging
    public String getFullMessage() {
      Date d=  new Date();
        return patientIdentifier+"\t"  +d.getHours()+":"
                +d.getMinutes()+":"  +d.getSeconds()+
                "'IS_SUCCESS': ["+this.isSuccess+"] \t\t"+
                "'EVENT_TYPE': ["+this.eventType +"] \t\t"+
                "'MESSAGE': ["+this.message+"] \t\t"+
                "'RECOMMENDATION': ["+this.recommendation+"]";

    }
}
