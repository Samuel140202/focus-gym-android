package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvTotalSessions, tvTotalMinutes, tvStreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Toolbar como ActionBar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.statistics_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Referencias
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvTotalMinutes  = findViewById(R.id.tvTotalMinutes);
        tvStreak        = findViewById(R.id.tvStreak);

        // Datos
        List<SessionLog> logs = SessionStore.loadAll(this);
        if (logs == null) logs = new ArrayList<>();

        int totalSessions = logs.size();
        int totalMinutes = 0;
        for (SessionLog s : logs) totalMinutes += Math.max(0, s.durationMin);
        int streakWeeks = computeWeeklyStreak(logs);

        // UI
        tvTotalSessions.setText(String.valueOf(totalSessions));
        tvTotalMinutes.setText(getResources().getQuantityString(
                R.plurals.minutes_quantity, totalMinutes, totalMinutes));
        tvStreak.setText(getResources().getQuantityString(
                R.plurals.weeks_quantity, streakWeeks, streakWeeks));
    }

    // ===== Menú de la ActionBar (ícono de historial) =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, SessionHistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private int computeWeeklyStreak(List<SessionLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;

        Set<String> weeksWithSessions = new HashSet<>();
        Calendar cal = Calendar.getInstance();
        for (SessionLog s : logs) {
            if (!s.completed) continue;
            cal.setTimeInMillis(s.dateMillis);
            weeksWithSessions.add(cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.WEEK_OF_YEAR));
        }

        int streak = 0;
        Calendar it = Calendar.getInstance();
        while (true) {
            String key = it.get(Calendar.YEAR) + "-" + it.get(Calendar.WEEK_OF_YEAR);
            if (weeksWithSessions.contains(key)) {
                streak++;
                it.add(Calendar.WEEK_OF_YEAR, -1);
            } else break;
        }
        return streak;
    }
}
