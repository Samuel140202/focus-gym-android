package sv.edu.catolica.dc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

/** Relee de SharedPreferences y vuelve a programar todo al reinicio/cambio de hora/etc. */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        Log.i(TAG, "onReceive: action=" + action);

        // Acciones que requieren reprogramar alarmas
        boolean shouldReschedule =
                Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                        Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) || // devices
                        Intent.ACTION_TIME_CHANGED.equals(action) ||
                        Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                        Intent.ACTION_PACKAGE_REPLACED.equals(action);

        if (!shouldReschedule) return;

        SharedPreferences sp = context.getSharedPreferences("gymapp_prefs", Context.MODE_PRIVATE);
        String json = sp.getString("reminders_json", "");
        List<Reminder> list = ReminderScheduler.deserialize(json);

        Log.i(TAG, "Rescheduling after " + action + " — items=" + (list != null ? list.size() : 0));
        ReminderScheduler.rescheduleAll(context, list);
    }
}
