package sv.edu.catolica.dc;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_ID  = "reminder_id";
    public static final String EXTRA_DESC = "reminder_desc";
    public static final String EXTRA_DAY  = "reminder_day";

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String id   = intent != null ? intent.getStringExtra(EXTRA_ID)   : null;
            String desc = intent != null ? intent.getStringExtra(EXTRA_DESC) : null;
            int day     = intent != null ? intent.getIntExtra(EXTRA_DAY, -1) : -1;

            if (id == null || id.trim().isEmpty()) {
                id = "auto_" + System.currentTimeMillis();
                Log.w(TAG, "onReceive: EXTRA_ID venía null/vacío, usando id=" + id);
            }

            NotificationHelper.ensureChannel(context);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int notifId = (id + "_" + day).hashCode();
            nm.notify(
                    notifId,
                    NotificationHelper
                            .builder(context,
                                    "¡Es hora de tu rutina!",
                                    desc == null ? "Recordatorio" : desc)
                            .build()
            );

            Log.i(TAG, "Notificado id=" + id + " day=" + day + " desc=" + desc);

            if (day >= 0 && day <= 6) {
                ReminderScheduler.scheduleNextForSingleDay(context, id, desc, day);
            } else {
                Log.w(TAG, "onReceive: EXTRA_DAY inválido (" + day + "), no se reprograma.");
            }

        } catch (Throwable t) {
            Log.e(TAG, "onReceive: error procesando recordatorio", t);
        }
    }
}
