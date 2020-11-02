package org.openmrs.mobile.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.settings.SettingsActivity;
import org.openmrs.mobile.api.EncounterService;
import org.openmrs.mobile.api.PatientService;
import org.openmrs.mobile.api.RestApi;
import org.openmrs.mobile.api.RestServiceBuilder;
import org.openmrs.mobile.dao.ConceptDAO;
import org.openmrs.mobile.models.Concept;
import org.openmrs.mobile.models.Link;
import org.openmrs.mobile.models.Results;
import org.openmrs.mobile.models.SystemSetting;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.ToastUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ApplicationConstants.ServiceActions.START_SYNC_ACTION)) {
//            showNotification(1);
            ToastUtil.notify("Syncing switched on, attempting to sync patients and form data");
            Intent i = new Intent(this, PatientService.class);
            this.startService(i);
            Intent i1 = new Intent(this, EncounterService.class);
            this.startService(i1);
        }
        return START_STICKY;

    }


    private void showNotification(int downloadedConcepts) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_openmrs);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Downloading Concepts")
                .setTicker("OpenMRS Android Client")
                .setContentText("hh")
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true)
                .build();
        startForeground(ApplicationConstants.ServiceNotificationId.CONCEPT_DOWNLOADFOREGROUND_SERVICE,
                notification);
    }


    private void sendProgressBroadcast() {
        Intent intent = new Intent(ApplicationConstants.BroadcastActions.CONCEPT_DOWNLOAD_BROADCAST_INTENT_ID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
