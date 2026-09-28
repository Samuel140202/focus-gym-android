package sv.edu.catolica.dc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SesionCurso extends AppCompatActivity {

    private static final String TAG = "SesionCurso";

    private Chronometer chronometer;
    private MaterialButton btnPlayPause, btnNext, btnPrev, btnFinish;
    private TextView tvRoutineTitle, tvExerciseName, tvSetRepInfo;

    private boolean running = false;
    private long pauseOffset = 0L;
    private long startRealtimeBase = 0L; // arranque real del cronómetro

    private Routine routine;
    private ArrayList<Exercise> exercises = new ArrayList<>();
    private int currentIndex = 0;
    private int currentSet = 1;

    private boolean pendingReset = false;

    // Video (ExoPlayer)
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_session);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.session_in_progress);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Views
        chronometer    = findViewById(R.id.chronometer);
        btnPlayPause   = findViewById(R.id.btnPlayPause);
        btnNext        = findViewById(R.id.btnNext);
        btnPrev        = findViewById(R.id.btnPrev);
        btnFinish      = findViewById(R.id.btnFinish);
        tvRoutineTitle = findViewById(R.id.tvRoutineTitle);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvSetRepInfo   = findViewById(R.id.tvSetRepInfo);
        playerView     = findViewById(R.id.playerView);
        playerView.setKeepContentOnPlayerReset(true);
        playerView.setUseArtwork(false);

        // Extras (rutina + lista de ejercicios)
        try {
            routine = getIntent().getParcelableExtra("routine");
            ArrayList<Exercise> incoming =
                    getIntent().getParcelableArrayListExtra("selected_exercises");
            if (incoming == null || incoming.isEmpty()) {
                incoming = getIntent().getParcelableArrayListExtra("exercises");
            }
            if (incoming != null && !incoming.isEmpty()) {
                exercises = new ArrayList<>(incoming);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo extras parcelables", e);
            Toast.makeText(this, "Extras inválidos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (exercises == null || exercises.isEmpty()) {
            Toast.makeText(this, R.string.no_exercises_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (routine == null) {
            routine = new Routine("Rutina", "", "", "", "Intermedio", 0);
        }

        // Estado inicial
        if (savedInstanceState == null) {
            int startIndex = getIntent().getIntExtra("startIndex", 0);
            if (startIndex >= 0 && startIndex < exercises.size()) {
                currentIndex = startIndex;
                currentSet = 1;
            }
        } else {
            running      = savedInstanceState.getBoolean("running", false);
            pauseOffset  = savedInstanceState.getLong("pauseOffset", 0L);
            currentIndex = savedInstanceState.getInt("currentIndex", 0);
            currentSet   = savedInstanceState.getInt("currentSet", 1);
        }

        // Cronómetro
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        btnPlayPause.setIconResource(running ? R.drawable.ic_pause_24 : R.drawable.ic_play_24);
        if (running) chronometer.start();

        updateHeader();

        // Autoplay si viene bandera
        boolean autoStart = getIntent().getBooleanExtra("AUTO_START", false);
        if (autoStart && !running) startTimerAndMedia();

        // Listeners
        btnPlayPause.setOnClickListener(v -> {
            if (!running) startTimerAndMedia();
            else          pauseTimerAndMedia();
        });

        btnNext.setOnClickListener(v -> {
            boolean advanced = advanceToNext();
            if (advanced) {
                pendingReset = true;
                openRest(getRestSecondsForCurrent());
            } else {
                pauseTimerAndMedia();
                logSessionCompletion(true);
                Toast.makeText(this, R.string.session_completed, Toast.LENGTH_SHORT).show();
                resetTimer();
                updateHeader();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentSet > 1) {
                currentSet--;
            } else if (currentIndex > 0) {
                currentIndex--;
                currentSet = Math.max(1, exercises.get(currentIndex).getSeries());
            }
            resetTimer();
            updateHeader();
        });

        btnFinish.setOnClickListener(v -> {
            pauseTimerAndMedia();
            logSessionCompletion(false);
            Toast.makeText(this, R.string.session_finished, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void startTimerAndMedia() {
        if (!running && startRealtimeBase == 0L) {
            startRealtimeBase = SystemClock.elapsedRealtime();
        }
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
        running = true;
        btnPlayPause.setIconResource(R.drawable.ic_pause_24);
        if (player != null && playerView.getVisibility() == View.VISIBLE) player.play();
    }

    private void pauseTimerAndMedia() {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        running = false;
        btnPlayPause.setIconResource(R.drawable.ic_play_24);
        if (player != null) player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingReset) {
            resetTimer();
            updateHeader();
            // startTimerAndMedia();
            pendingReset = false;
        }
    }

    private boolean advanceToNext() {
        Exercise ex = exercises.get(currentIndex);
        int series = Math.max(1, ex.getSeries());
        if (currentSet < series) {
            currentSet++;
            return true;
        } else if (currentIndex < exercises.size() - 1) {
            currentIndex++;
            currentSet = 1;
            return true;
        } else {
            return false;
        }
    }

    private int getRestSecondsForCurrent() {
        try {
            int s = exercises.get(currentIndex).getRestSeconds();
            return s > 0 ? s : 45;
        } catch (Throwable t) {
            return 45;
        }
    }

    private void openRest(int seconds) {
        if (running) pauseTimerAndMedia();
        Intent i = new Intent(this, RestActivity.class);
        i.putExtra("REST_TIME", seconds);
        startActivity(i);
    }

    private void resetTimer() {
        chronometer.stop();
        running = false;
        pauseOffset = 0L;
        chronometer.setBase(SystemClock.elapsedRealtime());
        btnPlayPause.setIconResource(R.drawable.ic_play_24);
    }

    private void updateHeader() {
        if (exercises.isEmpty()) return;
        if (currentIndex < 0 || currentIndex >= exercises.size()) currentIndex = 0;

        tvRoutineTitle.setText(getString(
                R.string.routine_prefix,
                routine != null ? routine.getTitle() : "Rutina"
        ));

        Exercise ex = exercises.get(currentIndex);
        tvExerciseName.setText(getString(R.string.exercise_prefix, ex.getName()));
        tvSetRepInfo.setText(getString(
                R.string.series_reps_fmt_full,
                currentSet,
                Math.max(1, ex.getSeries()),
                ex.getReps()
        ));

        bindExerciseMedia(ex);

        Log.d(TAG, "current=" + currentIndex + "/" + exercises.size()
                + " → " + ex.getName() + " | uri=" + safeGetVideoUri(ex));
    }

    // ---------- Reproductor (ExoPlayer) ----------
    private void ensurePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
            player.setVolume(0f);
            playerView.setPlayer(player);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }

    // ---------- Normalización / mapeo por nombre ----------
    private String normalize(String s) {
        if (s == null) return "";
        s = s.toLowerCase().trim();

        // Español
        s = s.replace("á","a").replace("é","e").replace("í","i")
                .replace("ó","o").replace("ú","u").replace("ñ","n");

        // Portugués
        s = s.replace("ã","a").replace("â","a").replace("à","a");
        s = s.replace("ê","e").replace("é","e");
        s = s.replace("õ","o").replace("ô","o").replace("ó","o");
        s = s.replace("ç","c");


        return s.replaceAll("\\s+", " ");
    }

    private String canonicalKey(String rawName) {
        String n = normalize(rawName);

        // ========== FLEXIONES ==========
        if (n.contains("lagartija")
                || n.contains("flexion de brazo")
                || n.contains("flexiones de brazo")
                || n.equals("flexiones")
                || n.equals("flexion de brazos")
                || n.contains("push up")
                || n.contains("push-up")
                || n.contains("pushup")
                // PT:
                || n.equals("flexoes")
                || n.contains("flexoes de braco")
                || n.contains("flexao de braco")) {
            return "flexiones";
        }

        // ========== SENTADILLA ==========
        if (n.contains("sentadilla")
                || n.contains("squat")
                || n.contains("agachamento")) {
            return "sentadilla";
        }

        // ========== PLANCHA ==========
        if (n.contains("plancha")
                || n.contains("plank")
                || n.contains("prancha")) {
            return "plancha";
        }

        // ========== PRESS BANCA MANCUERNAS ==========
        if ((n.contains("press") && (n.contains("pecho") || n.contains("banca")) && n.contains("mancuerna"))
                || n.contains("bench press")
                || (n.contains("supino") && n.contains("halter"))) {
            return "press_banca_mancuernas";
        }

        // ========== PRESS HOMBRO ==========
        if ((n.contains("press") && n.contains("hombro") && n.contains("mancuerna"))
                || n.contains("shoulder press")
                || (n.contains("desenvolvimento") && n.contains("halter"))) {
            return "press_hombro_mancuernas";
        }

        // ========== REMO MANCUERNAS ==========
        if ((n.contains("remo") && n.contains("mancuerna"))
                || n.contains("dumbbell row")
                || (n.contains("remada") && n.contains("halter"))) {
            return "remo_mancuernas";
        }


        if (n.contains("curl biceps")
                || n.contains("curl de biceps")
                || n.contains("bicep curl")
                || n.contains("biceps curl")
                || (n.contains("curl") && n.contains("biceps"))
                || n.contains("rosca")) {
            return "curl_biceps";
        }

        // ========== ZANCADA ==========
        if (n.contains("zancada")
                || n.contains("lunge")
                || n.contains("avanco")
                || n.contains("afundo")) {
            return "zancada";
        }

        // ========== CRUNCH ABDOMINAL ==========
        if (n.contains("crunch")
                || (n.contains("abdominal") && n.contains("crunch"))) {
            return "crunch_abdominal";
        }

        // ========== PESO MUERTO MANCUERNAS ==========
        if ((n.contains("peso muerto") && n.contains("mancuerna"))
                || n.contains("deadlift")
                || (n.contains("levantamento terra") && n.contains("halter"))) {
            return "peso_muerto_mancuernas";
        }

        // ========== ESPALDA ==========
        if (n.contains("espalda")
                || n.contains("back")
                || n.contains("costas")
                || n.contains("dorsal")) {
            return "espalda";
        }

        // ========== TRÍCEPS ==========
        if (n.contains("triceps") || n.contains("tricep")) {
            return "extension_triceps";
        }

        return "";
    }

    private int mediaResForKey(String key) {
        switch (key) {
            case "flexiones":               return R.raw.lagartijas_demo;
            case "sentadilla":              return R.raw.sentadilla_demo;
            case "plancha":                 return R.raw.plancha_demo;
            case "press_banca_mancuernas":  return R.raw.press_banca_mancuernas_demo;
            case "press_hombro_mancuernas": return R.raw.press_hombro_mancuernas_demo;
            case "remo_mancuernas":         return R.raw.remo_mancuernas_demo;
            case "curl_biceps":             return R.raw.curl_biceps_demo;
            case "zancada":                 return R.raw.zancada_demo;
            case "crunch_abdominal":        return R.raw.crunch_abdominal_demo;
            case "extension_triceps":       return R.raw.extension_triceps;
            case "peso_muerto_mancuernas":  return R.raw.peso_muerto_mancuerna;
            case "espalda":                 return R.raw.espalda;
            default:                        return 0;
        }
    }

    private void bindExerciseMedia(Exercise ex) {
        ensurePlayer();


        String modelUri = safeGetVideoUri(ex);
        if (modelUri != null && !modelUri.trim().isEmpty()) {
            player.stop();
            player.clearMediaItems();
            player.setMediaItem(MediaItem.fromUri(Uri.parse(modelUri)), true);
            player.prepare();
            if (running) player.play(); else player.pause();
            playerView.setVisibility(View.VISIBLE);
            return;
        }

        // 2) Fallback a raw por nombre
        String key = canonicalKey(ex != null ? ex.getName() : null);
        int resId = mediaResForKey(key);

        if (resId == 0) {
            playerView.setVisibility(View.GONE);
            if (player != null) player.pause();
            return;
        }

        player.stop();
        player.clearMediaItems();
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resId);
        player.setMediaItem(MediaItem.fromUri(uri), true);
        player.prepare();
        if (running) player.play(); else player.pause();
        playerView.setVisibility(View.VISIBLE);
    }

    private String safeGetVideoUri(Exercise e) {
        if (e == null) return null;
        try {
            return (String) Exercise.class.getMethod("getVideoUri").invoke(e);
        } catch (Throwable ignore) { }
        try {
            Field f = Exercise.class.getDeclaredField("videoUri");
            f.setAccessible(true);
            Object v = f.get(e);
            return v != null ? v.toString() : null;
        } catch (Throwable ignore) { }
        return null;
    }

    // ---------- Historial ----------
    private void logSessionCompletion(boolean completed) {
        int percent = Math.min(100, Math.round(progressPercent() * 100f));
        int durationMin = (int) elapsedMinutes();

        SessionLog log = new SessionLog(
                System.currentTimeMillis(),
                routine != null ? routine.getTitle() : "Rutina",
                routine != null ? routine.getLevel() : "Intermedio",
                durationMin,
                percent,
                completed
        );

        try {
            ArrayList<String> names = new ArrayList<>();
            for (Exercise e : exercises) {
                if (e != null && e.getName() != null) names.add(e.getName());
            }
            String csv = TextUtils.join(", ", names);

            try {
                Field f = SessionLog.class.getDeclaredField("exercisesCsv");
                f.setAccessible(true);
                f.set(log, csv);
            } catch (NoSuchFieldException ignore) { }

            try {
                Field f2 = SessionLog.class.getDeclaredField("exercises");
                f2.setAccessible(true);
                f2.set(log, new ArrayList<>(names));
            } catch (NoSuchFieldException ignore) { }

        } catch (Throwable t) {
            Log.w(TAG, "No se pudieron adjuntar nombres de ejercicios al log.", t);
        }

        SessionStore.add(this, log);
    }

    private float progressPercent() {
        if (exercises == null || exercises.isEmpty()) return 0f;

        int totalSets = 0;
        for (Exercise e : exercises) totalSets += Math.max(1, e.getSeries());
        if (totalSets <= 0) return 0f;

        int doneSets = 0;
        for (int i = 0; i < currentIndex; i++) {
            doneSets += Math.max(1, exercises.get(i).getSeries());
        }
        int currentSeries = Math.max(1, exercises.get(currentIndex).getSeries());
        doneSets += Math.max(0, Math.min(currentSeries, currentSet - 1));

        return Math.max(0f, Math.min(1f, doneSets / (float) totalSets));
    }

    private long elapsedMinutes() {
        if (startRealtimeBase == 0L) return 0L;

        long elapsedMs;
        if (running) {
            elapsedMs = SystemClock.elapsedRealtime() - startRealtimeBase;
        } else {
            elapsedMs = pauseOffset;
        }
        return Math.max(1, Math.round(elapsedMs / 60000f));
    }

    // ---------- Ciclo de vida ----------
    @Override
    protected void onStart() {
        super.onStart();
        if (running && player != null && playerView.getVisibility() == View.VISIBLE) {
            player.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("running", running);
        outState.putLong("pauseOffset", running
                ? SystemClock.elapsedRealtime() - chronometer.getBase()
                : pauseOffset);
        outState.putInt("currentIndex", currentIndex);
        outState.putInt("currentSet", currentSet);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
