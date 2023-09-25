package org.openmrs.mobile.activities.ip;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.openmrs.mobile.R;
import org.openmrs.mobile.application.OpenMRS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class EnforceChangeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText urlAddress;
    private Button saveButton;
    private boolean isConn = false;
    private ArrayList<Integer> allConn;
    SharedPreferences prefs;
    boolean syncState, closeDialog;
    String newUrl, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_enforce_change);

        urlAddress = (EditText) findViewById(R.id.urlAddress);
        saveButton = (Button) findViewById(R.id.saveDetails);

        saveButton.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allConn = new ArrayList<Integer>();
        prefs = PreferenceManager.getDefaultSharedPreferences(OpenMRS.getInstance());
        syncState = prefs.getBoolean("sync", true);
        TextView textView = (TextView) findViewById(R.id.showCurrentURL);
        textView.setText(Html.fromHtml("<span style='color:#228B22;'>Current URL: " + OpenMRS.getInstance().getServerUrl() + "</span>"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        newUrl = urlAddress.getText().toString();
        if (view == saveButton) {
            getStatus(newUrl);
            if (message != null) {
                CustomAction(message, closeDialog);
                message = null;
            }
            //Log.v("CustomDebug", "I am calling u now with " + this.isConn);
        }
    }

    private void afterExcutionOfThread() {
        Log.v("CustomDebug", "After Execution " + syncState);
        if (syncState) {
            closeDialog = false;
            message = "The App is currently in online mode. Kindly toggle the icon to switch to offline mode before saving the new URL.";
        } else if (!isConn) {
            closeDialog = false;
            message = "Sorry, there is no connection between the Client and the Host using the URL Address you entered. Please check and try again.";
        } else {
            if (newUrl.trim().isEmpty()) {
                closeDialog = false;
                message = "Sorry, the URL field cannot be empty.";
            } else {
                OpenMRS.getInstance().setServerUrl(newUrl);
                closeDialog = true;
                message = "Url Address Changed Successfully. Kindly logout to effect then the App close  changes.";
            }
        }
    }

    private void CustomAction(String s, boolean finishOnOk) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(s);
        dlgAlert.setTitle("Enforce URL Message");
        dlgAlert.setPositiveButton("OK",
                (dialog, whichButton) -> {
                    if (finishOnOk) {
                        finish();
                    }
                }
        );
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    public void getStatus(String url) {
        final boolean[] result = new boolean[1];
        Log.v("CustomDebug", "Connection in getStatus");
        allConn.clear();
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                try {
                    HttpURLConnection connection = null;
                    try {
                        URL u = new URL(url);
                        connection = (HttpURLConnection) u.openConnection();
                        connection.setRequestMethod("HEAD");
                        int code = connection.getResponseCode();
                        Log.v("CustomDebug", "Connection in inside");
                        if (code == 200) {
                            result[0] = true;
                            allConn.add(1);
                            Log.v("CustomDebug", "Connection in true " + " = " + isConn);
                        } else {
                            result[0] = false;
                            Log.v("CustomDebug", "Connection in false " + " = " + isConn);
                            allConn.add(0);
                        }
                        //isConn = result[0];
                        // You can determine on HTTP return code received. 200 is success.
                    } catch (MalformedURLException e) {
                        Log.v("CustomDebug", "Connection in MalformedURLException");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.v("CustomDebug", "Connection in IOException");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            Log.v("CustomDebug", "Connection in finally");
                            connection.disconnect();
                        }
                        isConn = result[0];
                        Log.v("CustomDebug", "Connection is " + " " + isConn);
                        afterExcutionOfThread();
                    }
                } catch (Exception e) {
                    Log.v("CustomDebug", "Connection in no working");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        //return result[0];
    }

}
