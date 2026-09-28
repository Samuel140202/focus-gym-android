package sv.edu.catolica.dc;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class RestActivity extends AppCompatActivity {

    private static final int DEFAULT_REST_SECONDS = 45;
    private static final String KEY_REMAINING_MS = "remainingMs";

    private CountDownTimer countDownTimer;
    private long totalMs;
    private long remainingMs;

    private TextView tvTimer;
    private MaterialButton btnSkip, btnRestart;
    private CircularProgressIndicator progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.rest_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Back = Saltar

        // Views
        tvTimer    = findViewById(R.id.tvTimer);
        btnSkip    = findViewById(R.id.btnSkip);
        btnRestart = findViewById(R.id.btnRestart);
        progress   = findViewById(R.id.progress);

        // Tiempo de descanso (segundos)
        int restSeconds = getIntent().getIntExtra("REST_TIME", DEFAULT_REST_SECONDS);
        totalMs = restSeconds * 1000L;


        progress.setIndeterminate(false);
        progress.setMax(restSeconds);


        if (savedInstanceState != null) {
            remainingMs = savedInstanceState.getLong(KEY_REMAINING_MS, totalMs);
        } else {
            remainingMs = totalMs;
        }


        updateTimerText(remainingMs);
        updateProgress(remainingMs, restSeconds);


        btnSkip.setOnClickListener(v -> finish());
        btnRestart.setOnClickListener(v -> restartTimer());

        // Iniciar
        startTimer(remainingMs);
    }

    private void restartTimer() {
        cancelTimer();
        remainingMs = totalMs;
        updateTimerText(remainingMs);
        progress.setMax((int) (totalMs / 1000));
        updateProgress(remainingMs, (int) (totalMs / 1000));
        startTimer(remainingMs);
    }

    private void startTimer(long startFromMs) {
        cancelTimer();
        countDownTimer = new CountDownTimer(startFromMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateTimerText(remainingMs);
                updateProgress(remainingMs, (int) (totalMs / 1000));
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                updateTimerText(0);
                updateProgress(0, (int) (totalMs / 1000));
                Toast.makeText(RestActivity.this, "¡Descanso terminado!", Toast.LENGTH_SHORT).show();
                finish(); // Regresa a SessionActivity
            }
        }.start();
    }

    private void updateTimerText(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long secs    = seconds % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, secs));
    }

    private void updateProgress(long msRemaining, int maxSeconds) {
        int secondsRemaining = (int) Math.ceil(msRemaining / 1000.0);
        if (secondsRemaining < 0) secondsRemaining = 0;
        if (secondsRemaining > maxSeconds) secondsRemaining = maxSeconds;
        progress.setMax(maxSeconds);
        // Cuenta regresiva: progreso = segundos restantes
        progress.setProgressCompat(secondsRemaining, true);
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(KEY_REMAINING_MS, remainingMs);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }
}

