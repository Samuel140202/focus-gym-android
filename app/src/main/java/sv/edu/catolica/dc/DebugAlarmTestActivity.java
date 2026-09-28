package sv.edu.catolica.dc;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class DebugAlarmTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        // (Android 13+) pedir POST_NOTIFICATIONS si hace falta
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != getPackageManager().PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1234);
        }

        MaterialButton btnNow  = findViewById(R.id.btnNotifyNow);
        MaterialButton btn10s  = findViewById(R.id.btnAlarm10s);

        // Notificación inmediata
        btnNow.setOnClickListener(v -> {
            NotificationHelper.ensureChannel(this);
            int nid = ("now_" + System.currentTimeMillis()).hashCode();
            android.app.NotificationManager nm =
                    (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(nid, NotificationHelper
                    .builder(this, "Prueba inmediata", "Si ves esto, el canal funciona")
                    .build());
            Snackbar.make(v, "Enviada notificación inmediata", Snackbar.LENGTH_SHORT).show();
        });

        // Programa una alarma en 10 segundos usando tu ReminderScheduler
        btn10s.setOnClickListener(v -> {
            long when = System.currentTimeMillis() + 10_000L;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(when);

            int h = c.get(Calendar.HOUR_OF_DAY);
            int m = c.get(Calendar.MINUTE);
            int weekday = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Mon..Sun -> 0..6

            ReminderScheduler.schedule(
                    this,
                    "debug_test",
                    "Alarma de diagnóstico (10s)",
                    h, m, weekday
            );

            Snackbar.make(v, "Alarma de prueba en 10s programada", Snackbar.LENGTH_SHORT).show();
        });
    }
}
