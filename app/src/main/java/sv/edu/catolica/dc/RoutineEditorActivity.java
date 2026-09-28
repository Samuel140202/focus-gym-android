package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class RoutineEditorActivity extends AppCompatActivity
        implements AdaptadorEjerciciosEditables.Listener, ExerciseEditDialogFragment.Callback {

    public static final String EXTRA_ROUTINE   = "routine";
    public static final String EXTRA_EXERCISES = "exercises";

    // Para compat con el selector (solo si usas long-press en el botón):
    private static final String EXTRA_SELECTED_NAMES = "selected_names"; // ArrayList<String>

    private TextInputEditText etTitle, etDesc;
    private MaterialAutoCompleteTextView actLevel;
    private RecyclerView rv;
    private AdaptadorEjerciciosEditables adapter;

    private Routine routine;
    private final ArrayList<Exercise> exercises = new ArrayList<>();
    private boolean isEditMode = false;
    private boolean dirty = false;

    // Abrir biblioteca con LONG PRESS en el botón (opcional)
    private final ActivityResultLauncher<Intent> launcherPickFromLibrary =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> names =
                            result.getData().getStringArrayListExtra(EXTRA_SELECTED_NAMES);
                    if (names != null && !names.isEmpty()) {
                        for (String name : names) {
                            if (name == null) continue;
                            String n = name.trim();
                            if (n.isEmpty()) continue;
                            // Defaults al traer desde biblioteca:
                            adapter.add(new Exercise(n, 4, 15, 45));
                        }
                        dirty = true;
                    } else {
                        toast(R.string.msg_invalid_exercises);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_editor);

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> tryFinish());

        // Views
        etTitle  = findViewById(R.id.etTitle);
        etDesc   = findViewById(R.id.etDesc);
        actLevel = findViewById(R.id.actLevel);
        rv       = findViewById(R.id.rv);
        MaterialButton btnAdd = findViewById(R.id.btnAddExercise);

        // Dropdown de niveles (usa tu string-array existing)
        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(
                this, R.array.levels_array, android.R.layout.simple_list_item_1);
        actLevel.setAdapter(levelAdapter);

        // Modo editar / crear
        Intent i = getIntent();
        if (i != null && i.hasExtra(EXTRA_ROUTINE)) {
            isEditMode = true;
            routine = i.getParcelableExtra(EXTRA_ROUTINE);
            ArrayList<Exercise> incoming = i.getParcelableArrayListExtra(EXTRA_EXERCISES);
            if (routine != null) {
                etTitle.setText(s(routine.getTitle()));
                actLevel.setText(s(routine.getLevel()), false);
                etDesc.setText(s(routine.getDescription()));
            }
            if (incoming != null) exercises.addAll(incoming);
            tb.setTitle(getString(R.string.title_edit_routine));
        } else {
            isEditMode = false;
            routine = (routine == null) ? new Routine() : routine;
            tb.setTitle(getString(R.string.title_new_routine));
        }

        // Lista
        adapter = new AdaptadorEjerciciosEditables(exercises, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Drag & drop con handle (sin long-press)
        ItemTouchHelper.SimpleCallback cb = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder from,
                                  @NonNull RecyclerView.ViewHolder to) {
                adapter.moveItem(from.getBindingAdapterPosition(), to.getBindingAdapterPosition());
                dirty = true;
                return true;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) { }
            @Override public boolean isLongPressDragEnabled() { return false; }
        };
        new ItemTouchHelper(cb).attachToRecyclerView(rv);

        // Menú guardar
        tb.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) { saveAndReturn(); return true; }
            return false;
        });

        // =========================
        // BOTÓN "AGREGAR EJERCICIO"
        // =========================
        // Click corto -> ejercicio personalizado
        btnAdd.setOnClickListener(v -> {
            promptNewExerciseName(name -> {
                Exercise nuevo = new Exercise(name, 4, 15, 45);
                ExerciseEditDialogFragment f =
                        ExerciseEditDialogFragment.newInstance(-1, nuevo, true);
                f.show(getSupportFragmentManager(), "edit_ex");
            });
        });

        // Long press (opcional) -> abrir biblioteca
        btnAdd.setOnLongClickListener(v -> {
            Intent pick = new Intent(this, ExerciseSelectorActivity.class);
            launcherPickFromLibrary.launch(pick);
            return true;
        });

        // Dirty tracking
        actLevel.setOnItemClickListener((p, v, pos, id) -> dirty = true);
        etTitle.addTextChangedListener(SimpleTextWatcher.on(() -> dirty = true));
        etDesc.addTextChangedListener(SimpleTextWatcher.on(() -> dirty = true));

        // Back con confirmación
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { tryFinish(); }
        });
    }

    private void tryFinish() {
        if (!dirty) { finish(); return; }
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.msg_discard_changes)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, (d, w) -> saveAndReturn())
                .setNeutralButton(android.R.string.no, (d, w) -> finish())
                .show();
    }

    private void saveAndReturn(){
        String title = t(etTitle);
        String level = t(actLevel);
        String desc  = t(etDesc);
        List<Exercise> out = adapter.getData();

        if (TextUtils.isEmpty(title)) { toast(R.string.msg_invalid_title); return; }

        if (out.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Guardar sin ejercicios")
                    .setMessage("Esta rutina no tiene ejercicios. ¿Quieres guardarla así?")
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.btn_save, (d,w)-> finishSave(title, level, desc, out))
                    .show();
        } else {
            for (Exercise ex : out) {
                if (ex.getSeries() < 1 || ex.getReps() < 1 || ex.getRestSeconds() < 0) {
                    toast(R.string.msg_invalid_fields); return;
                }
            }
            finishSave(title, level, desc, out);
        }
    }

    private void finishSave(String title, String level, String desc, List<Exercise> out){
        routine.setTitle(title);
        routine.setLevel(level);
        routine.setDescription(desc);

        Intent data = new Intent();
        data.putExtra(EXTRA_ROUTINE, routine);
        data.putParcelableArrayListExtra(EXTRA_EXERCISES, new ArrayList<>(out));
        setResult(RESULT_OK, data);
        finish();
    }

    // ==== EditableExerciseAdapter.Listener ====
    @Override
    public void onEdit(int position, Exercise ex) {
        ExerciseEditDialogFragment f = ExerciseEditDialogFragment.newInstance(position, ex, false);
        f.show(getSupportFragmentManager(), "edit_ex");
    }

    @Override
    public void onDelete(int position, Exercise ex) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.cd_delete))
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    adapter.deleteAt(position);
                    dirty = true;
                })
                .show();
    }

    @Override public void onStartDrag(RecyclerView.ViewHolder vh) { /* usamos handle táctil */ }

    // ==== ExerciseEditDialogFragment.Callback ====
    @Override
    public void onExerciseEdited(int position, Exercise result, boolean isNew) {
        if (isNew) adapter.add(result); else adapter.replaceAt(position, result);
        dirty = true;
    }

    // ==== helpers ====
    private void toast(int res){ Toast.makeText(this, res, Toast.LENGTH_SHORT).show(); }
    private String t(TextInputEditText et){ return et.getText()==null? "": et.getText().toString().trim(); }
    private String t(MaterialAutoCompleteTextView et){ return et.getText()==null? "": et.getText().toString().trim(); }
    private String s(String v){ return v==null? "": v; }

    private interface NameCallback { void onName(String name); }
    private void promptNewExerciseName(NameCallback cb){
        final com.google.android.material.textfield.TextInputEditText input =
                new com.google.android.material.textfield.TextInputEditText(this);
        input.setHint(getString(R.string.hint_new_exercise_name)); // ← antes: "Nombre del ejercicio"
        int pad = (int)(16*getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_new_exercise_title)) // ← antes: "Nuevo ejercicio"
                .setView(input)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String name = input.getText()==null? "" : input.getText().toString().trim();
                    if (name.isEmpty()) name = getString(R.string.default_exercise_name);
                    cb.onName(name);
                })
                .show();
    }

}
