package sv.edu.catolica.dc;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class RemindersActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ReminderAdapter adapter;
    private final List<Reminder> data = new ArrayList<>();
    private SharedPreferences sp;
    private final Gson gson = new Gson();
    private static final String KEY = "reminders_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        // Asegurar canal y permisos relevantes
        ensureNotificationChannel();
        requestNotificationPermissionIfNeeded();
        requestExactAlarmPermissionIfNeeded();

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.reminders_title);
        }


        // Menú (si no tienes menu_reminders, comenta estas dos líneas)
        tb.inflateMenu(R.menu.menu_reminders);
        tb.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_add) {
                // Crear uno que suene en ~2 minutos hoy
                Reminder r = new Reminder();
                r.id = "r_" + UUID.randomUUID();
                long when = System.currentTimeMillis() + 120_000L;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(when);
                r.hour = c.get(Calendar.HOUR_OF_DAY);
                r.minute = c.get(Calendar.MINUTE);
                r.enabled = true;
                if (r.days == null || r.days.length != 7) r.days = new boolean[7];
                int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Mon..Sun -> 0..6
                r.days[today] = true;
                r.description = "Recordatorio rápido";

                data.add(r);
                if (adapter != null) {
                    int pos = data.size() - 1;
                    adapter.notifyItemInserted(pos);
                    rv.smoothScrollToPosition(pos);
                }
                onDataChanged(); // guarda + programa
                return true;

            } else if (id == R.id.action_test) {
                // Programa una alarma en 60s
                long now = System.currentTimeMillis() + 60_000L;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(now);
                int h = c.get(Calendar.HOUR_OF_DAY);
                int m = c.get(Calendar.MINUTE);
                int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Mon..Sun -> 0..6
                ReminderScheduler.schedule(this, "test_id", "Prueba en 60s", h, m, today);
                Snackbar.make(findViewById(android.R.id.content),
                        "Alarma de prueba en 60s programada", Snackbar.LENGTH_SHORT).show();
                return true;

            } else if (id == R.id.action_notify_now) {
                // Notificación inmediata
                NotificationHelper.ensureChannel(this);
                int nid = ("now_" + System.currentTimeMillis()).hashCode();
                android.app.NotificationManager nm =
                        (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(nid, NotificationHelper
                        .builder(this, "Prueba inmediata", "Si ves esto, el canal funciona")
                        .build());
                Snackbar.make(findViewById(android.R.id.content),
                        "Enviada notificación inmediata", Snackbar.LENGTH_SHORT).show();
                return true;

            } else if (id == R.id.action_diag) {
                runNotificationDiagnostics();
                return true;
            }
            return false;
        });

        sp = getSharedPreferences("gymapp_prefs", MODE_PRIVATE);
        load();
        ensureIds();

        rv = findViewById(R.id.rvReminders);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(data, this::onDataChanged);
        rv.setAdapter(adapter);

        // Programa lo actual al entrar
        ReminderScheduler.rescheduleAll(this, data);

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            sp.edit().putString(KEY, gson.toJson(data)).commit(); // síncrono
            ReminderScheduler.rescheduleAll(this, data);
            Snackbar.make(v, "Guardado y recordatorios programados", Snackbar.LENGTH_SHORT).show();
        });
    }

    /** Se dispara cada vez que el adapter reporta cambios (hora/días/enable/desc). */
    private void onDataChanged() {
        sp.edit().putString(KEY, gson.toJson(data)).commit();
        ReminderScheduler.rescheduleAll(this, data);
    }

    private void load() {
        String json = sp.getString(KEY, "");
        if (json.isEmpty()) {
            for (int i = 0; i < 3; i++) data.add(new Reminder());
        } else {
            Type t = new TypeToken<ArrayList<Reminder>>() {}.getType();
            data.clear();
            data.addAll(gson.fromJson(json, t));
        }
    }

    private void ensureIds() {
        for (Reminder r : data) {
            if (r.id == null || r.id.trim().isEmpty()) {
                r.id = "r_" + UUID.randomUUID();
            }
        }
    }

    private void ensureNotificationChannel() {
        // Crea/asegura el canal para que el sistema no silencie
        NotificationHelper.ensureChannel(this);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != getPackageManager().PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }


    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = getSystemService(AlarmManager.class);
            if (am != null && !am.canScheduleExactAlarms()) {
                try {
                    Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            .setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } catch (Exception ignored) {
                    // Fallback a la pantalla de info de la app
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                }
            }
        }
    }

    private void runNotificationDiagnostics() {
        StringBuilder sb = new StringBuilder();
        boolean enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        sb.append(enabled ? "✅ Notificaciones del app: PERMITIDAS\n"
                : "❌ Notificaciones del app: BLOQUEADAS\n");

        if (Build.VERSION.SDK_INT >= 26) {
            android.app.NotificationManager nm = getSystemService(android.app.NotificationManager.class);
            android.app.NotificationChannel ch = nm.getNotificationChannel(NotificationHelper.CHANNEL_ID);
            if (ch == null) {
                sb.append("❌ Canal '").append(NotificationHelper.CHANNEL_ID).append("': NO EXISTE (se creará al enviar)\n");
            } else {
                sb.append("✅ Canal '").append(ch.getId()).append("': importancia ")
                        .append(ch.getImportance()).append(" (3=DEFAULT, 4=HIGH)\n");
            }
        } else {
            sb.append("ℹ️ Sin canales (API<26)\n");
        }

        if (!enabled) {
            sb.append("\n➡️ Toca aquí para abrir ajustes y habilitar notificaciones.");
            Snackbar.make(findViewById(android.R.id.content), sb.toString(), Snackbar.LENGTH_LONG)
                    .setAction("Ajustes", v -> openAppNotificationSettings())
                    .show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), sb.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void openAppNotificationSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
