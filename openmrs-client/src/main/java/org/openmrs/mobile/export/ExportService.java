package org.openmrs.mobile.export;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.File;


public class ExportService  extends IntentService {
    public ExportService( ) {
        super("ExportService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
       File openMRSFolder = new File(Environment.getExternalStorageDirectory() + "/NMRS-PBS");
        new FullExport(getApplicationContext(), openMRSFolder).starExportingPatients();
    }
}
