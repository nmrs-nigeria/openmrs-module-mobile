package org.openmrs.mobile.activities.troubleshoot;

import android.content.Context;
import android.graphics.Region;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.utilities.FontsUtil;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class TroubleshootFragment extends ACBaseFragment<TroubleshootContract.Presenter> implements TroubleshootContract.View {

    private Button testPBSConnection;
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_troubleshoot, container, false);
        setHasOptionsMenu(true);
        resolveViews(root);
        addListeners();
        FontsUtil.setFont((ViewGroup) root);
        return root;
    }

    private void resolveViews(View v) {
        testPBSConnection = v.findViewById(R.id.testPBSConnection);
        webView = v.findViewById(R.id.webView);
        webView.setWebViewClient(new MyBrowser());
    }

    private void addListeners() {
        testPBSConnection.setOnClickListener(v -> {
            String serverURL = OpenMRS.getInstance().getServerUrl();

            int position = serverURL.lastIndexOf(":");

            String ipAddress = serverURL.substring(0, position);

            int pbsPORT = 2018;

            String pbsURL = ipAddress + ":" + pbsPORT;

            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.loadUrl(pbsURL);

            OpenMRSCustomHandler.writeLogToFile("The current server URL for PBS is: " + pbsURL);

        });
    }

    @Override
    public void finishTroubleshootActivity() {
        getActivity().finish();
    }

    @Override
    public void setErrorsVisibility(boolean receiptError) {

    }

    @Override
    public void scrollToTop() {

    }

    @Override
    public void hideSoftKeys() {

    }

    @Override
    public void setProgressBarVisibility(boolean visibility) {

    }

    @Override
    public void startTroubleshootActivity() {

    }

    public static TroubleshootFragment newInstance() {
        return new TroubleshootFragment();
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
