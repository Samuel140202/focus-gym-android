package sv.edu.catolica.dc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView tvRoutineTitle, tvRoutineMeta;
    private LinearProgressIndicator progressToday;
    private MaterialButton btnViewDetails, btnStartNow;
    private RecyclerView rvUpcoming;
    private UpcomingAdapter upcomingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Permiso notificaciones (Android 13+)
        requestNotificationPermissionIfNeeded();

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name); // usa string, se traduce

        // Vistas encabezado
        tvRoutineTitle = findViewById(R.id.tvRoutineTitle);
        tvRoutineMeta  = findViewById(R.id.tvRoutineMeta);
        progressToday  = findViewById(R.id.progressToday);
        btnViewDetails = findViewById(R.id.btnViewDetails);
        btnStartNow    = findViewById(R.id.btnStartNow);

        // Accesos rápidos
        View shortcutLibrary   = findViewById(R.id.shortcut_library);
        View shortcutStats     = findViewById(R.id.shortcut_stats);
        View shortcutReminders = findViewById(R.id.shortcut_reminders);

        if (shortcutLibrary != null) {
            shortcutLibrary.setOnClickListener(v ->
                    startActivity(new Intent(this, RoutinesActivity.class)));
        }
        if (shortcutStats != null) {
            shortcutStats.setOnClickListener(v ->
                    startActivity(new Intent(this, StatisticsActivity.class)));
        }
        if (shortcutReminders != null) {
            shortcutReminders.setOnClickListener(v ->
                    startActivity(new Intent(this, RemindersActivity.class)));
        }

        // Próximas sesiones (usa resources localizados)
        rvUpcoming = findViewById(R.id.rvUpcoming);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new UpcomingAdapter(fakeUpcomingLocalized());
        rvUpcoming.setAdapter(upcomingAdapter);

        // Navegación principal (extras localizados)
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetalleRutina.class);
                intent.putExtra("ROUTINE_ID", "1");
                intent.putExtra("ROUTINE_TITLE", getString(R.string.rutina_full_body));
                intent.putExtra("ROUTINE_META",  getString(R.string.duraci_n_45_min_nivel_intermedio));
                intent.putExtra("ROUTINE_OBJECTIVE", getString(R.string.objetivo_fuerza_sample));
                intent.putExtra("ROUTINE_MUSCLES",   getString(R.string.musculos_sample));
                startActivity(intent);
            });
        }
        if (btnStartNow != null) {
            btnStartNow.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetalleRutina.class);
                intent.putExtra("ROUTINE_ID", "1");
                intent.putExtra("ROUTINE_TITLE", getString(R.string.rutina_full_body));
                intent.putExtra("ROUTINE_META",  getString(R.string.duraci_n_45_min_nivel_intermedio));
                intent.putExtra("ROUTINE_OBJECTIVE", getString(R.string.objetivo_fuerza_sample));
                intent.putExtra("ROUTINE_MUSCLES",   getString(R.string.musculos_sample));
                intent.putExtra("AUTO_START", true);
                startActivity(intent);
            });
        }

        // Encabezado (resources)
        if (tvRoutineTitle != null) tvRoutineTitle.setText(R.string.rutina_de_hoy_full_body);
        if (tvRoutineMeta  != null) tvRoutineMeta.setText(R.string.duraci_n_45_min_nivel_intermedio);
        if (progressToday  != null) progressToday.setProgress(35);
    }

    /**
     * Pide permiso POST_NOTIFICATIONS en Android 13+ usando ContextCompat/ActivityCompat.
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    // Datos localizados para el RecyclerView
    private List<UpcomingItem> fakeUpcomingLocalized() {
        List<UpcomingItem> list = new ArrayList<>();
        list.add(new UpcomingItem(
                getString(R.string.upper_body_power),
                getString(R.string.ma_ana_40_min_intermedio)
        ));
        list.add(new UpcomingItem(
                getString(R.string.legs_core),
                getString(R.string.legs_core_meta)
        ));
        list.add(new UpcomingItem(
                getString(R.string.hiit_express),
                getString(R.string.hiit_express_meta)
        ));
        return list;
    }
}
