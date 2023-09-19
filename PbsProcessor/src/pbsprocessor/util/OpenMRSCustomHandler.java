package pbsprocessor.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpenMRSCustomHandler {
    private static String logFile ="logs";
    private static String crashFile ="crash";
    private static String folderName="NMRS_mobile";
    private static String logPBS="pbs";

    public static void writeLogToFile(String message){
        createFolder();
        String currentTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        try {
            String fileNaming = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            File gpxfile = new File(createFolder(), OpenMRSCustomHandler.logFile + fileNaming + ".txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            BufferedWriter buf = new BufferedWriter(writer);
            buf.append(currentTime + ": " + message);
            buf.newLine();
            buf.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void EXPORT_PATIENTS_TO_JSON(String data){
        createFolder();
        try {
            String fileNaming = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(new Date())+new Date().getTime();
            File gpxfile = new File(createFolder(), OpenMRSCustomHandler.logPBS + fileNaming + ".txt");
            FileWriter writer = new FileWriter(gpxfile, false);
            BufferedWriter buf = new BufferedWriter(writer);
            buf.write(data);
            buf.flush();
            writer.flush();
            buf.close();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void writeCrashToFile(String message){
        String currentTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        try {
            String fileNaming = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            File gpxfile = new File(createFolder(), OpenMRSCustomHandler.crashFile + fileNaming + ".txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            BufferedWriter buf = new BufferedWriter(writer);
            buf.append(currentTime + ": " + message);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static File createFolder() {
        File dir = new File(    "C:/" + OpenMRSCustomHandler.folderName);

        if(!dir.exists()){
            dir.mkdir();
        }
        return dir;
    }
}
