package sv.edu.catolica.dc;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionHistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private SessionHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(getString(R.string.title_history)); // i18n
        tb.setNavigationOnClickListener(v -> onBackPressed());
        tb.inflateMenu(R.menu.menu_history);


        // Lista
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 1) Cargar historial real desde SessionStore
        List<SessionLog> raw = SessionStore.loadAll(this);

        // 2) Si no hay datos aún, usar mock de demo
        if (raw == null || raw.isEmpty()) raw = mockData();

        // 3) Seccionar y pintar
        List<SessionHistoryAdapter.Row> rows = buildSectionedRows(raw);
        adapter = new SessionHistoryAdapter(rows);
        rv.setAdapter(adapter);
    }



    // ==============================
    // Seccionado por rangos de tiempo
    // ==============================
    private List<SessionHistoryAdapter.Row> buildSectionedRows(List<SessionLog> all) {
        List<SessionHistoryAdapter.Row> rows = new ArrayList<>();

        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);

        // Inicio de esta semana (día de inicio del sistema, 0:00)
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startThisWeek = cal.getTimeInMillis();

        long startPrevWeek  = startThisWeek - 7L  * 24 * 60 * 60 * 1000;
        long startLastMonth = startPrevWeek - 30L * 24 * 60 * 60 * 1000;

        List<SessionLog> thisWeek  = new ArrayList<>();
        List<SessionLog> prevWeek  = new ArrayList<>();
        List<SessionLog> lastMonth = new ArrayList<>();

        for (SessionLog s : all) {
            if (s.dateMillis >= startThisWeek) thisWeek.add(s);
            else if (s.dateMillis >= startPrevWeek) prevWeek.add(s);
            else if (s.dateMillis >= startLastMonth) lastMonth.add(s);
        }

        if (!thisWeek.isEmpty()) {
            rows.add(SessionHistoryAdapter.Row.header(getString(R.string.section_this_week)));
            for (SessionLog s : thisWeek) rows.add(SessionHistoryAdapter.Row.item(s));
        }
        if (!prevWeek.isEmpty()) {
            rows.add(SessionHistoryAdapter.Row.header(getString(R.string.section_prev_week)));
            for (SessionLog s : prevWeek) rows.add(SessionHistoryAdapter.Row.item(s));
        }
        if (!lastMonth.isEmpty()) {
            rows.add(SessionHistoryAdapter.Row.header(getString(R.string.section_last_month)));
            for (SessionLog s : lastMonth) rows.add(SessionHistoryAdapter.Row.item(s));
        }
        if (rows.isEmpty()) {
            rows.add(SessionHistoryAdapter.Row.header(getString(R.string.history_empty)));
        }
        return rows;
    }

    // ==============================
    // Mock de respaldo
    // ==============================
    private List<SessionLog> mockData() {
        List<SessionLog> list = new ArrayList<>();
        long now = System.currentTimeMillis();

        // Hoy / esta semana
        list.add(new SessionLog(now - 1 * 60 * 60 * 1000, "Full Body", "Intermedio", 45, 100, true));
        list.add(new SessionLog(now - 2 * 24 * 60 * 60 * 1000, "Upper Body Power", "Intermedio", 40, 75, false));

        // Semana pasada
        list.add(new SessionLog(now - 8 * 24 * 60 * 60 * 1000, "Legs & Core", "Principiante", 35, 60, false));

        // Mes pasado aprox.
        list.add(new SessionLog(now - 20 * 24 * 60 * 60 * 1000, "HIIT Express", "Avanzado", 20, 100, true));

        return list;
    }

    // ==============================
    // Formateo de fecha (i18n)
    // ==============================

    /** NUEVO: usa el Locale actual del dispositivo (ES/EN/PT, etc.). */
    public static String formatDate(long millis, Context ctx) {
        // Toma el primer locale activo del sistema
        Locale locale = ctx.getResources().getConfiguration().getLocales().get(0);
        // Obtiene el mejor patrón para “día mes completo año” según el locale
        String skeleton = "d MMMM y";
        String pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton);
        return new SimpleDateFormat(pattern, locale).format(new Date(millis));
    }


    static String formatDate(long millis) {
        Locale locale = Locale.getDefault();
        String pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "d MMMM y");
        return new SimpleDateFormat(pattern, locale).format(new Date(millis));
    }
}
