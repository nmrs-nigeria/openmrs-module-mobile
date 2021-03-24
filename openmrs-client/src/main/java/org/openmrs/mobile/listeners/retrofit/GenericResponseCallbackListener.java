package org.openmrs.mobile.listeners.retrofit;

public interface GenericResponseCallbackListener<T> {

    //void onResponse();
    void onResponse(T obj);
    void onErrorResponse(T errorMessage);
    void onErrorResponse(String errorMessage);
}
