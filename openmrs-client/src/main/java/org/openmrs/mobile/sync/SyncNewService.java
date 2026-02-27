package org.openmrs.mobile.sync;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.openmrs.mobile.R;

// Team 1 Update
public class SyncNewService  extends IntentService {
    public SyncNewService(String name) {
        super(name);
    }
    public SyncNewService( ) {
        super("New Sync");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {

            Boolean fullSync = intent.getBooleanExtra("full_sync", false);
                             new SyncData(getApplicationContext()).runSyncAwait(fullSync);
        } else {
            new SyncData(getApplicationContext()).runSyncAwait(false);
        }
    }

}
