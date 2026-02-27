package org.openmrs.mobile.utilities;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.openmrs.mobile.R;
import org.openmrs.mobile.api.SyncStateReceiver;
import org.openmrs.mobile.databases.Util;
import org.openmrs.mobile.sync.SyncNewService;

/*
Notification management static  function for creating  notification channel and notifying
A channel have been create in Dashboard  using static function
Notification have been sent in SyncPBS class
 */
public class Notifier {
    public static final String CHANNEL_EXPORT = "export";
    public static final String  CANCEL_NOTIFICATION_ACTION="cancel_notification_action";
    public static String CHANNEL_SYNC_PBS = "sync";

    public static void notify(Context context, int notificationId, String channelId,
                              @NonNull String title, @NonNull String content, String largeContent) {

        // Create a PendingIntent to cancel the notification
        Intent cancelIntent = new Intent(context, SyncStateReceiver.class);
        cancelIntent.setAction(Notifier.CANCEL_NOTIFICATION_ACTION);  // Custom action string
        cancelIntent.putExtra("cancel_notification_id",notificationId);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0,
                cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Create a notification action for canceling the notification
        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_close,
                "Close",
                cancelPendingIntent
        ).build();
        NotificationCompat.Builder builder = largeContent == null ?
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.sync_icon)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT) :
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.sync_icon)
                        .setContentTitle(title)
                        .setContentText(content).setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(largeContent))
                        .addAction(cancelAction)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());


    }

    public static void createNotificationChannel(NotificationManager notificationManager, String CHANNEL_ID, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void cancel(Context context, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }


    public static void notifyWithAction(Context context, int notificationId, String channelId,
                                        @NonNull String title, @NonNull String content, String largeContent) {

        // Create a PendingIntent to cancel the notification
        Intent cancelIntent = new Intent(context, SyncStateReceiver.class);
        cancelIntent.setAction(Notifier.CANCEL_NOTIFICATION_ACTION);  // Custom action string
        cancelIntent.putExtra("cancel_notification_id",notificationId);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0,
                cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
       // Create a notification action for canceling the notification
        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_close,
                "Close",
                cancelPendingIntent
        ).build();


        Intent serviceIntent = new Intent(context, SyncNewService.class);
        serviceIntent.putExtra("full_sync", true);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        NotificationCompat.Builder builder = largeContent == null ?
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.sync_icon)
                        .setContentTitle(title)
                        .setContentText(content)
                        .addAction(cancelAction)
                        .addAction(new NotificationCompat.Action(R.drawable.sync_icon, "Full sync", pendingIntent))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT) :
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.sync_icon)
                        .setContentTitle(title)
                        .addAction(cancelAction)
                        .addAction(new NotificationCompat.Action(R.drawable.upload_icon, "Full sync", pendingIntent))
                        .setContentText(content).setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(largeContent))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());


    }
}
