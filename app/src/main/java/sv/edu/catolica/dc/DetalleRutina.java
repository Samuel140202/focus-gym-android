package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DetalleRutina extends AppCompatActivity {

    private static final String EXTRA_SELECTED_NAMES = "selected_names"; // ArrayList<String>

    private androidx.recyclerview.widget.RecyclerView rvExercises;
    private ExerciseAdapter adapter;
    private MaterialButton btnStart;
    private MaterialButton btnAddExercise;
    private boolean isFavorite = false;

    // Encabezado
    private TextView tvTitle, tvMeta, tvObjective, tvMuscles;

    private Routine currentRoutine;
    private final ArrayList<Exercise> currentExercises = new ArrayList<>();

    private final ActivityResultLauncher<Intent> launcherEdit =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                    Routine r = res.getData().getParcelableExtra(RoutineEditorActivity.EXTRA_ROUTINE);
                    ArrayList<Exercise> exs =
                            res.getData().getParcelableArrayListExtra(RoutineEditorActivity.EXTRA_EXERCISES);
                    if (r == null) return;
                    if (exs == null) exs = new ArrayList<>();

                    currentRoutine = r;
                    currentExercises.clear();
                    currentExercises.addAll(exs);

                    paintHeaderFrom(currentRoutine, currentExercises);
                    if (adapter != null) {
                        adapter.setExercises(currentExercises);
                        adapter.notifyDataSetChanged();
                    }

                    Toast.makeText(this, R.string.guardar_cambios, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> launcherPickFromLibrary =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> names = result.getData().getStringArrayListExtra(EXTRA_SELECTED_NAMES);
                    if (names != null && !names.isEmpty()) {
                        for (String name : names) {
                            if (name == null) continue;
                            String n = name.trim();
                            if (n.isEmpty()) continue;
                            currentExercises.add(new Exercise(n, 4, 15, 45));
                        }
                        if (adapter != null) {
                            adapter.setExercises(currentExercises);
                            adapter.notifyDataSetChanged();
                        }
                        paintHeaderFrom(currentRoutine, currentExercises);
                        Toast.makeText(this, R.string.btn_add_exercise, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.no_exercises_selected, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_rutina);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.detail_title); // ✅ sin texto duro
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        toolbar.inflateMenu(R.menu.menu_detail_routine);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_fav) {
                isFavorite = !isFavorite;
                item.setIcon(isFavorite ? R.drawable.ic_favorite_24 : R.drawable.ic_favorite_border_24);
                Toast.makeText(this,
                        isFavorite ? R.string.action_fav : R.string.action_edit, // mensaje simple
                        Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_edit) {
                openEditor();
                return true;
            }
            return false;
        });

        // Encabezado
        tvTitle     = findViewById(R.id.tvTitle);
        tvMeta      = findViewById(R.id.tvMeta);
        tvObjective = findViewById(R.id.tvObjective);
        tvMuscles   = findViewById(R.id.tvMuscles);

        // Lista
        rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(this);
        rvExercises.setAdapter(adapter);

        // Carga desde extras o mock
        loadFromExtrasOrMock();

        // Botón Comenzar
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> startSession());

        // Botón Agregar ejercicio → abre selector
        btnAddExercise = findViewById(R.id.btnAddExercise);
        if (btnAddExercise != null) {
            btnAddExercise.setOnClickListener(v -> {
                Intent pick = new Intent(this, ExerciseSelectorActivity.class);
                launcherPickFromLibrary.launch(pick);
            });
        }

        // Auto-start si aplica
        boolean autoStart = getIntent().getBooleanExtra("AUTO_START", false);
        if (autoStart && btnStart != null) btnStart.post(btnStart::performClick);
    }

    /** Lee de extras o arma un mock traducible. */
    private void loadFromExtrasOrMock() {
        Routine r = getIntent().getParcelableExtra(RoutineEditorActivity.EXTRA_ROUTINE);
        ArrayList<Exercise> list = getIntent().getParcelableArrayListExtra(RoutineEditorActivity.EXTRA_EXERCISES);

        if (r != null) currentRoutine = r;
        if (list != null) {
            currentExercises.clear();
            currentExercises.addAll(list);
        }

        if (currentRoutine != null && !currentExercises.isEmpty()) {
            paintHeaderFrom(currentRoutine, currentExercises);
        } else {
            // Fallback mock (todo con strings)
            currentRoutine = new Routine();
            currentRoutine.setTitle(getString(R.string.rutina_full_body));
            currentRoutine.setLevel(getString(R.string.selector_filter_level)); // “Nivel”
            currentRoutine.setDescription(
                    getString(R.string.objetivo_fuerza_sample).replaceFirst("^.*?:\\s*", "")
            );

            setText(tvTitle, getString(R.string.rutina_full_body));
            setText(tvMeta, getString(R.string.level_ex_count_fmt,
                    getString(R.string.selector_filter_level), 5));
            setText(tvObjective, getString(R.string.objetivo_fuerza_sample));
            setText(tvMuscles,   getString(R.string.musculos_sample));

            currentExercises.clear();
            currentExercises.addAll(getMockExercises());
        }

        if (adapter != null) adapter.setExercises(currentExercises);
    }

    /** Pinta encabezado a partir de rutina + ejercicios (localizable). */
    private void paintHeaderFrom(Routine r, List<Exercise> exs) {
        String level = safe(r.getLevel(), getString(R.string.level_beginner)); // o tu default
        String subtitle = level + " · " + (exs == null ? 0 : exs.size()) + " " + getString(R.string.exercises_label_short);
        setText(tvTitle, safe(r.getTitle(), getString(R.string.rutina_full_body)));
        setText(tvMeta, subtitle);
        setText(tvObjective, getString(R.string.objective_prefix, safe(r.getDescription(), "")));
        int count = (exs == null ? 0 : exs.size());
        setText(tvTitle, safe(r != null ? r.getTitle() : null, getString(R.string.rutina_full_body)));
        setText(tvMeta, getString(R.string.level_ex_count_fmt, level, count));
        setText(tvObjective, getString(R.string.objective_prefix, safe(r != null ? r.getDescription() : null, "")));
        // tvMuscles: mantén el valor que mandes por intent o el sample si no tienes campo.
        if (tvMuscles.getText() == null || tvMuscles.getText().toString().trim().isEmpty()) {
            setText(tvMuscles, getString(R.string.musculos_sample));
        }
    }

    private void openEditor() {
        String title = tvTitle != null && tvTitle.getText() != null ? tvTitle.getText().toString() : getString(R.string.rutina_full_body);
        String level = parseLevelFromMeta(tvMeta != null ? tvMeta.getText().toString() : null);
        String desc  = tvObjective != null && tvObjective.getText() != null ? tvObjective.getText().toString() : "";
        desc = desc.replaceFirst("^" + getString(R.string.objective_prefix, "").trim() + "\\s*", "")
                .replaceFirst("^Objetivo:\\s*", ""); // fallback

        Routine r = (currentRoutine != null) ? currentRoutine : new Routine();
        r.setTitle(title);
        r.setLevel(level);
        r.setDescription(desc);

        Intent i = new Intent(this, RoutineEditorActivity.class);
        i.putExtra(RoutineEditorActivity.EXTRA_ROUTINE, r);
        i.putParcelableArrayListExtra(RoutineEditorActivity.EXTRA_EXERCISES, new ArrayList<>(currentExercises));
        launcherEdit.launch(i);
    }

    private void startSession() {
        try {
            Toast.makeText(this, R.string.comenzar, Toast.LENGTH_SHORT).show();

            if (currentRoutine == null) {
                currentRoutine = new Routine();
                String title = tvTitle != null && tvTitle.getText() != null ? tvTitle.getText().toString() : getString(R.string.rutina_full_body);
                String meta  = tvMeta  != null && tvMeta.getText()  != null ? tvMeta.getText().toString()  : "";
                String obj   = tvObjective != null && tvObjective.getText() != null ? tvObjective.getText().toString() : "";
                String mus   = tvMuscles   != null && tvMuscles.getText()   != null ? tvMuscles.getText().toString()   : "";
                obj = obj.replaceFirst("^" + getString(R.string.objective_prefix, "").trim() + "\\s*", "")
                        .replaceFirst("^Objetivo:\\s*", "");
                mus = mus.replaceFirst("^" + getString(R.string.muscles_prefix, "").trim() + "\\s*", "")
                        .replaceFirst("^Grupos musculares:\\s*", "");
                currentRoutine.setTitle(title);
                currentRoutine.setLevel(parseLevelFromMeta(meta));
                currentRoutine.setDescription(obj);
            }
            if (currentExercises.isEmpty()) currentExercises.addAll(getMockExercises());

            ArrayList<Exercise> selected = (adapter != null) ? adapter.getSelected() : new ArrayList<>();
            if (selected == null || selected.isEmpty()) selected = new ArrayList<>(currentExercises);

            Intent i = new Intent(this, SesionCurso.class);
            i.putExtra("routine", currentRoutine);
            i.putParcelableArrayListExtra("selected_exercises", selected);
            i.putExtra("startIndex", 0);
            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String parseLevelFromMeta(String meta){
        if (meta == null) return getString(R.string.selector_filter_level);
        String[] parts = meta.split("·");
        if (parts.length == 0) return getString(R.string.selector_filter_level);
        String lvl = parts[0].trim();
        return lvl.isEmpty() ? getString(R.string.selector_filter_level) : lvl;
    }

    // DetalleRutina.java
    private List<Exercise> getMockExercises() {
        List<Exercise> list = new ArrayList<>();
        list.add(new Exercise("1", getString(R.string.press_de_pecho_con_mancuernas), 4, 12, 45, R.drawable.ic_fitness_center_24));
        list.add(new Exercise("2", getString(R.string.sentadillas_peso_corporal),     4, 15, 60, R.drawable.ic_fitness_center_24));
        list.add(new Exercise("3", getString(R.string.plancha_abdominal),             3, 30, 40, R.drawable.ic_fitness_center_24));
        list.add(new Exercise("4", getString(R.string.peso_muerto_mancuernas),        3, 10, 50, R.drawable.ic_fitness_center_24));
        list.add(new Exercise("5", getString(R.string.flexiones_brazos),              3, 15, 30, R.drawable.ic_fitness_center_24));
        return list;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helpers
    private void setText(TextView tv, String text){ if (tv != null) tv.setText(text); }
    private String safe(String v, String def){ return (v == null || v.trim().isEmpty()) ? def : v; }
}
