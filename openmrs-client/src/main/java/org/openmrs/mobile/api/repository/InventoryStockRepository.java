package org.openmrs.mobile.api.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.activeandroid.query.Delete;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilderCommodity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.application.OpenMRSLogger;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.InventoryStockSummaryLab;
import org.openmrs.mobile.models.InventoryStockSummaryPharmacy;
import org.openmrs.mobile.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryStockRepository extends RetrofitRepository {

    private OpenMRSLogger logger;
    private RestApi restApi;
    private List<InventoryStockSummaryLab> inventoryStockSummaries;
    private String lab_uuid = "2741bae2-c5de-43ef-891f-7ec2fd58f442";
    private String pharmacy_uuid = "5452ec3e-2fe1-46de-8a6e-28c6442e4cc0";

    public InventoryStockRepository() {
        this.logger = new OpenMRSLogger();
        this.restApi = RestServiceBuilderCommodity.createService(RestApi.class);
    }

    public void getInventoryStockSummaryLab(@Nullable final DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            Call<HashMap<String, Object>> call = restApi.getInventoryStockSummary(100, 1, this.lab_uuid);
            call.enqueue(new Callback<HashMap<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<HashMap<String, Object>> call, @NonNull Response<HashMap<String, Object>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, Object> summaryData = response.body();

                        //inventoryStockSummaries = response.body().getResults();

                        //for (InventoryStockSummary inventoryStockSummary : summaryData.getResults()) {
                        //OpenMRSCustomHandler.showJson(summaryData.getResults());
                        //OpenMRSCustomHandler.showJson(summaryData.get("results"));
                        ArrayList<Object> resultsArrayList = (ArrayList<Object>) response.body().get("results");
                        try {
                            accessArrayListLab(resultsArrayList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //OpenMRSCustomHandler.showJson(retVal);
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<HashMap<String, Object>> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
//                        callbackListener.onErrorResponse(t.getMessage());
                        callbackListener.onResponse();

                    }
                    OpenMRSCustomHandler.writeLogToFile("Inventory Stock Repository: " + t.getMessage());
                }
            });
        }
    }

    void accessArrayListLab(ArrayList<Object> stockSummaryArrayList) throws JSONException {
        //OpenMRSCustomHandler.showJson(stockSummaryArrayList);
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()// STATIC|TRANSIENT in the default configuration
                .create();
        String values = gson.toJson(stockSummaryArrayList);


        JSONArray jsonarray = new JSONArray(stockSummaryArrayList);

        //Clear all exisiting data in this table and recreate
        new Delete().from(InventoryStockSummaryLab.class).execute();


        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject obj = jsonarray.getJSONObject(i);
            JSONObject item = obj.getJSONObject("item");

            String uuid = item.getString("uuid");
            String name = item.getString("name");
            String itemType = item.getString("itemType");

            String expiration = obj.getString("expiration");
            String itemBatch = obj.getString("itemBatch");

            String quantity = obj.getString("quantity");

            InventoryStockSummaryLab inventoryStockSummaryLab = new InventoryStockSummaryLab();
            inventoryStockSummaryLab.setExpiration(expiration);
            inventoryStockSummaryLab.setQuantity(quantity);
            inventoryStockSummaryLab.setUuid(uuid);
            inventoryStockSummaryLab.setName(name);
            inventoryStockSummaryLab.setItemType(itemType);
            inventoryStockSummaryLab.setItemBatch(itemBatch);
            inventoryStockSummaryLab.save();
        }
    }


    public void getInventoryStockSummaryPharmacy(@Nullable final DefaultResponseCallbackListener callbackListener) {
        if (NetworkUtils.isOnline()) {
            Call<HashMap<String, Object>> call = restApi.getInventoryStockSummary(100, 1, this.pharmacy_uuid);
            call.enqueue(new Callback<HashMap<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<HashMap<String, Object>> call, @NonNull Response<HashMap<String, Object>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, Object> summaryData = response.body();

                        //inventoryStockSummaries = response.body().getResults();

                        //Log.v("Baron", "Start logging data");
                        //for (InventoryStockSummary inventoryStockSummary : summaryData.getResults()) {
                        //OpenMRSCustomHandler.showJson(summaryData.getResults());
                        //OpenMRSCustomHandler.showJson(summaryData.get("results"));
                        ArrayList<Object> resultsArrayList = (ArrayList<Object>) response.body().get("results");
                        try {
                            accessArrayListPharmacy(resultsArrayList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //OpenMRSCustomHandler.showJson(retVal);
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<HashMap<String, Object>> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
//                        callbackListener.onErrorResponse(t.getMessage());
                        callbackListener.onResponse();

                    }
                    OpenMRSCustomHandler.writeLogToFile("Failure logging data (Inventory Stock Repository): " + t.getMessage());
                }
            });
        }
    }

    void accessArrayListPharmacy(ArrayList<Object> stockSummaryArrayList) throws JSONException {
        OpenMRSCustomHandler.showJson(stockSummaryArrayList);
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()// STATIC|TRANSIENT in the default configuration
                .create();
        String values = gson.toJson(stockSummaryArrayList);


        JSONArray jsonarray = new JSONArray(stockSummaryArrayList);

        //Clear all exisiting data and recreate
        new Delete().from(InventoryStockSummaryPharmacy.class).execute();

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject obj = jsonarray.getJSONObject(i);
            JSONObject item = obj.getJSONObject("item");

            String uuid = item.getString("uuid");
            String name = item.getString("name");
            String itemType = item.getString("itemType");

            String expiration = obj.getString("expiration");
            String itemBatch = obj.getString("itemBatch");

            String quantity = obj.getString("quantity");

            InventoryStockSummaryPharmacy inventoryStockSummaryPharmacy = new InventoryStockSummaryPharmacy();

            inventoryStockSummaryPharmacy.setExpiration(expiration);
            inventoryStockSummaryPharmacy.setQuantity(quantity);
            inventoryStockSummaryPharmacy.setUuid(uuid);
            inventoryStockSummaryPharmacy.setName(name);
            inventoryStockSummaryPharmacy.setItemType(itemType);
            inventoryStockSummaryPharmacy.setItemBatch(itemBatch);
            inventoryStockSummaryPharmacy.save();

            /**Log.v("Baron", "" +
                    "Data = Expiration:" + expiration + " " +
                    "Quantity:" + quantity + " " +
                    "UUID: " + uuid + " " +
                    "Name: " + name + " " +
                    "Itemtype: " + itemType + " " +
                    "ItemBatch: " + itemBatch);**/
        }
    }


}
