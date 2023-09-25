package org.openmrs.mobile.sync;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.openmrs.mobile.bulksync.SyncData;

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
        //new StartNewSync(getApplicationContext()).runSyncAwait();
        new SyncData(getApplicationContext()).runSyncAwait();
    }

}
