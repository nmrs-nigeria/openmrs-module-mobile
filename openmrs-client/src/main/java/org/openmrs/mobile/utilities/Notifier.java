package org.openmrs.mobile.utilities;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.openmrs.mobile.R;
import org.openmrs.mobile.databases.Util;
/*
Notification management static  function for creating  notification channel and notifying
A channel have been create in Dashboard  using static function
Notification have been sent in SyncPBS class
 */
public class Notifier {
 public  static  String  CHANNEL_SYNC_PBS="sync";
    public  static void notify(Context context ,int notificationId,  String channelId,
                                @NonNull String title, @NonNull String content, String largeContent ){
        NotificationCompat.Builder builder = largeContent==null?
                new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.sync_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)  :
        new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.sync_icon)
                .setContentTitle(title)
                .setContentText(content).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(largeContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());



    }

    public static void createNotificationChannel( NotificationManager notificationManager ,String CHANNEL_ID,  String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void cancel(Context context,int notificationId ) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }
}
