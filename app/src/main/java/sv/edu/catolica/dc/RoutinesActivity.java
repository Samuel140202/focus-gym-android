package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoutinesActivity extends AppCompatActivity {

    private RecyclerView rv;
    private RoutineAdapter adapter;


    private final List<RoutinePack> packs = new ArrayList<>();
    // Lo que pinta tu adapter (derivado 1:1 de packs)
    private final List<RoutineAdapter.Routine> uiData = new ArrayList<>();


    private final ActivityResultLauncher<Intent> launcherCreate =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() != RESULT_OK || res.getData() == null) return;

                Routine r = res.getData().getParcelableExtra(RoutineEditorActivity.EXTRA_ROUTINE);
                ArrayList<Exercise> exs =
                        res.getData().getParcelableArrayListExtra(RoutineEditorActivity.EXTRA_EXERCISES);

                if (r == null) return;
                if (exs == null) exs = new ArrayList<>();


                RoutineStore.add(this, r, exs);
                packs.add(new RoutinePack(r, exs));

                RoutineAdapter.Routine item = toUiItem(r, exs);
                int insertAt = uiData.size();
                uiData.add(item);

                if (adapter != null) {
                    adapter.notifyItemInserted(insertAt);
                    rv.smoothScrollToPosition(insertAt);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.library_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> onBackPressed());

        // Recycler
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoutineAdapter(uiData);
        rv.setAdapter(adapter);

        // Clicks del adapter
        adapter.setOnItemClick(new RoutineAdapter.OnItemClick() {
            @Override public void onOpen(int position) { openDetailFor(position); }
            @Override public void onStart(int position) { openDetailFor(position); }
            @Override public void onLong(int position) { showItemMenu(position); }
        });


        reloadFromStore();


        if (uiData.isEmpty()) {
            Toast.makeText(this, "No tienes rutinas. Pulsa + para crear una.", Toast.LENGTH_LONG).show();
        }
    }

    // Menú toolbar superior
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_routines, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (id == R.id.action_new_routine) {
            // Abrir editor vacío (crear)
            launcherCreate.launch(new Intent(this, RoutineEditorActivity.class));
            return true;
        } else if (id == R.id.action_search) {
            Toast.makeText(this, "Buscar…", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --------- Navegar a detalle ---------
    private void openDetailFor(int pos){
        if (pos < 0 || pos >= packs.size()) {
            Toast.makeText(this, "Elemento inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        RoutinePack p = packs.get(pos);
        Routine r = p.routine;
        ArrayList<Exercise> exs = new ArrayList<>(p.exercises);

        Intent i = new Intent(this, DetalleRutina.class);
        i.putExtra(RoutineEditorActivity.EXTRA_ROUTINE, r);
        i.putParcelableArrayListExtra(RoutineEditorActivity.EXTRA_EXERCISES, exs);
        startActivity(i);
    }

    // --------- Carga/transformación ---------
    private void reloadFromStore() {
        uiData.clear();
        packs.clear();

        packs.addAll(RoutineStore.loadAll(this));

        for (RoutinePack p : packs) {
            uiData.add(toUiItem(p.routine, p.exercises));
        }
        adapter.notifyDataSetChanged();
    }

    private RoutineAdapter.Routine toUiItem(Routine r, List<Exercise> exs) {
        String title = (r.getTitle()==null || r.getTitle().isEmpty()) ? "Rutina" : r.getTitle();
        String subtitle = buildSubtitle(r, exs);
        int count = exs==null ? 0 : exs.size();
        return new RoutineAdapter.Routine(title, subtitle, count, 0);
    }

    private String buildSubtitle(Routine r, List<Exercise> exs) {
        String level = (r.getLevel()==null || r.getLevel().isEmpty()) ? "Básico" : r.getLevel();
        int count = exs==null ? 0 : exs.size();
        return level + " · " + count + " ej.";
    }

    // --------- Menú por long-press (Duplicar / Eliminar) ---------
    private void showItemMenu(int position) {
        if (position < 0 || position >= uiData.size()) return;
        String[] ops = new String[]{"Duplicar", "Eliminar", "Cancelar"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(uiData.get(position).title)
                .setItems(ops, (d, which) -> {
                    if (which == 0)      duplicateAt(position);
                    else if (which == 1) confirmDelete(position);
                })
                .show();
    }

    private void confirmDelete(int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar rutina")
                .setMessage("¿Seguro que deseas eliminar esta rutina?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dd, w) -> deleteAt(position))
                .show();
    }

    private void deleteAt(int position) {
        // Elimina de almacenamiento (por índice). Si tienes deleteById, úsalo aquí.
        RoutineStore.deleteAt(this, position);

        if (position < packs.size()) packs.remove(position);
        if (position < uiData.size()) uiData.remove(position);
        adapter.notifyItemRemoved(position);

        if (uiData.isEmpty()) {
            Toast.makeText(this, "No tienes rutinas. Pulsa + para crear una.", Toast.LENGTH_LONG).show();
        }
    }

    private void duplicateAt(int position) {
        if (position < 0 || position >= packs.size()) return;

        // Origen
        RoutinePack src = packs.get(position);
        Routine srcRoutine = src.routine;
        ArrayList<Exercise> srcExs = new ArrayList<>(src.exercises);

        // Copia
        Routine copy = new Routine();
        copy.setTitle(((srcRoutine.getTitle()==null ? "Rutina" : srcRoutine.getTitle())) + " (copia)");
        copy.setLevel(srcRoutine.getLevel());
        copy.setDescription(srcRoutine.getDescription());

        ArrayList<Exercise> copyExs = new ArrayList<>();
        for (Exercise e : srcExs) {
            copyExs.add(new Exercise(
                    UUID.randomUUID().toString(),
                    e.getName(),
                    e.getSeries(),
                    e.getReps(),
                    e.getRestSeconds(),
                    e.getImageRes()
            ));
        }

        // Persistir y actualizar memoria/UI
        RoutineStore.add(this, copy, copyExs);
        packs.add(new RoutinePack(copy, copyExs));

        RoutineAdapter.Routine uiItem = toUiItem(copy, copyExs);
        int insertAt = uiData.size();
        uiData.add(uiItem);
        adapter.notifyItemInserted(insertAt);
        rv.smoothScrollToPosition(insertAt);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
