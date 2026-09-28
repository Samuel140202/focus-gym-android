package sv.edu.catolica.dc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    public static final String CHANNEL_ID = "reminders_channel";

    public static void ensureChannel(Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "Recordatorios",
                        NotificationManager.IMPORTANCE_HIGH
                );
                ch.setDescription("Notificaciones de recordatorios de la app");
                ch.enableVibration(true);
                ch.enableLights(true);
                nm.createNotificationChannel(ch);
            }
        }
    }


    private static PendingIntent contentIntent(Context ctx){
        Intent open = new Intent(ctx, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                ctx, 7001, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static NotificationCompat.Builder builder(Context ctx, String title, String text){
        ensureChannel(ctx);
        return new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent(ctx))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }
}
