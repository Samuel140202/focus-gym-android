package sv.edu.catolica.dc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    private static final Gson gson = new Gson();

    /** Reprograma TODOS los recordatorios habilitados según el estado actual. */
    public static void rescheduleAll(Context ctx, List<Reminder> reminders) {
        if (reminders == null) return;
        cancelAll(ctx, reminders);

        for (Reminder r : reminders) {
            if (r == null) continue;
            r.ensureDefaults();
            if (!r.enabled) continue;
            if (r.days == null || r.days.length != 7) continue;

            for (int d = 0; d < 7; d++) {
                if (r.days[d]) {
                    schedule(ctx, r.id, r.description, r.hour, r.minute, d);
                }
            }
        }
    }

    /** Cancela TODAS las alarmas (todas las combinaciones id+día). */
    public static void cancelAll(Context ctx, List<Reminder> reminders) {
        if (reminders == null) return;

        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        if (am == null) am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        for (Reminder r : reminders) {
            if (r == null) continue;
            for (int d = 0; d < 7; d++) {
                PendingIntent pi = pendingIntent(ctx, r.id, r.description, d, PendingIntent.FLAG_NO_CREATE);
                if (pi != null) {
                    am.cancel(pi);
                    pi.cancel();
                }
            }
        }
    }

    /** Programa la próxima alarma para el día (0..6 → L..D) y hora indicados. */
    public static void schedule(Context ctx, String id, String desc, int hour, int minute, int weekday) {
        if (id == null || id.trim().isEmpty()) return;

        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        if (am == null) am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = pendingIntent(ctx, id, desc, weekday, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar next = nextOccurrence(hour, minute, weekday);
        long triggerAt = next.getTimeInMillis();

        // Intent visible (abre pantalla de recordatorios desde el icono del reloj)
        Intent showIntent = new Intent(ctx, RemindersActivity.class);
        PendingIntent showPI = PendingIntent.getActivity(
                ctx, 9001, showIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        try {
            // ✅ Estrategia más robusta: preferir AlarmClock (dispara incluso en Doze / app cerrada)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(triggerAt, showPI);
                am.setAlarmClock(info, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException se) {
            // Fallback por si el OEM bloquea setAlarmClock (raro, pero posible)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        }

        Log.i(TAG, "schedule → id=" + id + " day=" + weekday + " at=" + next.getTime()
                + " [h=" + hour + ", m=" + minute + "]");
    }


    /** Llamado por el Receiver tras disparar, para reprogramar la siguiente semana ese mismo día. */
    public static void scheduleNextForSingleDay(Context ctx, String id, String desc, int weekday) {
        SharedPreferences sp = ctx.getSharedPreferences("gymapp_prefs", Context.MODE_PRIVATE);
        String json = sp.getString("reminders_json", "");
        List<Reminder> list = deserialize(json);
        if (list == null) return;

        for (Reminder r : list) {
            if (r != null && id != null && id.equals(r.id)) {
                schedule(ctx, id, r.description, r.hour, r.minute, weekday);
                Log.i(TAG, "scheduleNextForSingleDay → id=" + id + " day=" + weekday
                        + " h=" + r.hour + " m=" + r.minute);
                return;
            }
        }
    }

    // -------------------- Helpers --------------------

    private static PendingIntent pendingIntent(Context ctx, String id, String desc, int weekday, int flags) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        i.putExtra(ReminderReceiver.EXTRA_ID, id);
        i.putExtra(ReminderReceiver.EXTRA_DESC, desc);
        i.putExtra(ReminderReceiver.EXTRA_DAY, weekday);
        int req = requestCode(id, weekday);
        return PendingIntent.getBroadcast(ctx, req, i, PendingIntent.FLAG_IMMUTABLE | flags);
    }

    private static int requestCode(String id, int weekday) {
        return (id + "_" + weekday).hashCode();
    }

    /** Calcula la próxima fecha evitando caer “en el pasado” por los segundos. */
    private static Calendar nextOccurrence(int hour, int minute, int weekday) {
        int calDay = switch (weekday) {
            case 0 -> Calendar.MONDAY;
            case 1 -> Calendar.TUESDAY;
            case 2 -> Calendar.WEDNESDAY;
            case 3 -> Calendar.THURSDAY;
            case 4 -> Calendar.FRIDAY;
            case 5 -> Calendar.SATURDAY;
            default -> Calendar.SUNDAY;
        };

        Calendar now = Calendar.getInstance();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, calDay);
        cal.set(Calendar.HOUR_OF_DAY, clamp(hour, 0, 23));
        cal.set(Calendar.MINUTE, clamp(minute, 0, 59));

        int sec = now.get(Calendar.SECOND) + 5;
        if (sec >= 60) {
            cal.add(Calendar.MINUTE, 1);
            sec -= 60;
        }
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.before(now)) cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal;
    }

    private static int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    // Utilidades JSON (usadas en BootReceiver)
    public static List<Reminder> deserialize(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<ArrayList<Reminder>>(){}.getType();
        try {
            return gson.fromJson(json, t);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unused")
    private static void promptExactAlarmSettings(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(Uri.parse("package:" + ctx.getPackageName()))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
            } catch (Exception ignored) { }
        }
    }
}
