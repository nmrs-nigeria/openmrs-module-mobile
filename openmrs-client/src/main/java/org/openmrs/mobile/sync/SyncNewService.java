package org.openmrs.mobile.sync;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class SyncNewService  extends IntentService {
    public SyncNewService(String name) {
        super(name);
    }
    public SyncNewService( ) {
        super("New Sync");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        new StartNewSync(getApplicationContext()).runSyncAwait();
    }

}
