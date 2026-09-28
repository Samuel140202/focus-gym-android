package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExerciseSelectorActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_IDS   = "selected_ids";     // HashSet<String>
    public static final String EXTRA_SELECTED_NAMES = "selected_names";   // ArrayList<String>

    private RecyclerView rv;
    private android.widget.Button btnConfirm;
    private android.widget.Spinner spGrupo, spNivel;

    private ExercisePickAdapter adapter;

    private final List<SelectorExercise> all = new ArrayList<>();
    private final List<SelectorExercise> filtered = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_exercise_selector);

        androidx.appcompat.widget.Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(R.string.selector_title);
        tb.setNavigationOnClickListener(v -> finish());

        spGrupo = findViewById(R.id.spGrupo);
        spNivel = findViewById(R.id.spNivel);
        btnConfirm = findViewById(R.id.btnConfirm);
        rv = findViewById(R.id.rv);

        // Grid 2 columnas
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setHasFixedSize(true);
        rv.setPadding(12, 8, 12, 8);
        rv.setClipToPadding(false);
        rv.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));

        adapter = new ExercisePickAdapter(() -> btnConfirm.setEnabled(adapter.getSelectedCount() > 0));
        rv.setAdapter(adapter);

        // Spinners localizables desde resources
        ArrayAdapter<CharSequence> gAdapter =
                ArrayAdapter.createFromResource(this, R.array.selector_groups, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> nAdapter =
                ArrayAdapter.createFromResource(this, R.array.selector_levels, android.R.layout.simple_spinner_dropdown_item);
        spGrupo.setAdapter(gAdapter);
        spNivel.setAdapter(nAdapter);

        AdapterView.OnItemSelectedListener l = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        };
        spGrupo.setOnItemSelectedListener(l);
        spNivel.setOnItemSelectedListener(l);

        seed();          // catálogo (nombres via strings)
        applyFilters();  // filtros iniciales

        // Restaurar selección previa (opcional)
        Serializable prev = getIntent().getSerializableExtra(EXTRA_SELECTED_IDS);
        if (prev instanceof Set) {
            //noinspection unchecked
            adapter.setSelectedIds((Set<String>) prev);
            btnConfirm.setEnabled(adapter.getSelectedCount() > 0);
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setOnClickListener(v -> {
            ArrayList<String> names = new ArrayList<>();
            for (SelectorExercise e : adapter.getSelected()) names.add(e.nombre);
            Intent out = new Intent();
            out.putStringArrayListExtra(EXTRA_SELECTED_NAMES, names);
            out.putExtra(EXTRA_SELECTED_IDS, new HashSet<>(adapter.getSelectedIds()));
            setResult(RESULT_OK, out);
            finish();
        });
    }

    private void applyFilters() {
        String gLabel = spGrupo.getSelectedItem().toString();
        String nLabel = spNivel.getSelectedItem().toString();

        boolean allGroups = gLabel.equals(getString(R.string.selector_all));
        boolean allLevels = nLabel.equals(getString(R.string.selector_all));

        filtered.clear();
        for (SelectorExercise e : all) {
            boolean okG = allGroups || groupLabel(this, e.grupo).equalsIgnoreCase(gLabel);
            boolean okN = allLevels || levelLabel(this, e.nivel).equalsIgnoreCase(nLabel);
            if (okG && okN) filtered.add(e);
        }
        adapter.submit(filtered);
        btnConfirm.setEnabled(adapter.getSelectedCount() > 0);
    }

    /** Catálogo base: usa getString(...) para que cambie por idioma */
    //Aqui estaaaaaaaaaaaaaaaaaan
    private void seed() {
        all.clear();
        all.add(new SelectorExercise("ex_press_manc", getString(R.string.press_de_pecho_con_mancuernas),
                SelectorExercise.Group.PECHO,  SelectorExercise.Level.INTERMEDIO,   "press_banca_mancuernas_demo"));
        all.add(new SelectorExercise("ex_lag", getString(R.string.flexiones_brazos),
                SelectorExercise.Group.PECHO,  SelectorExercise.Level.PRINCIPIANTE, "lagartijas_demo"));
        all.add(new SelectorExercise("ex_peso_muerto", getString(R.string.peso_muerto_mancuernas),
                SelectorExercise.Group.PIERNA, SelectorExercise.Level.INTERMEDIO,   "peso_muerto_demo"));
        all.add(new SelectorExercise("ex_espalda", getString(R.string.espalda),
                SelectorExercise.Group.ESPALDA, SelectorExercise.Level.PRINCIPIANTE, "espalda"));
        all.add(new SelectorExercise("ex_zancada", getString(R.string.zancada),
                SelectorExercise.Group.PIERNA, SelectorExercise.Level.PRINCIPIANTE, "zancada_demo"));
        all.add(new SelectorExercise("ex_remo", getString(R.string.remo_mancuernas),
                SelectorExercise.Group.ESPALDA, SelectorExercise.Level.INTERMEDIO,  "remo_mancuernas_demo"));
        all.add(new SelectorExercise("ex_press_hombro", getString(R.string.press_hombro_mancuernas),
                SelectorExercise.Group.HOMBRO, SelectorExercise.Level.INTERMEDIO,   "press_hombro_mancuernas_demo"));
        all.add(new SelectorExercise("ex_curl", getString(R.string.curl_biceps),
                SelectorExercise.Group.BRAZO,  SelectorExercise.Level.PRINCIPIANTE, "curl_biceps_demo"));
        all.add(new SelectorExercise("ex_triceps", getString(R.string.extension_triceps),
                SelectorExercise.Group.BRAZO,  SelectorExercise.Level.INTERMEDIO,   "extension_triceps_demo"));
        all.add(new SelectorExercise("ex_crunch", getString(R.string.crunch_abdominal),
                SelectorExercise.Group.CORE,   SelectorExercise.Level.PRINCIPIANTE, "crunch_abdominal_demo"));
        all.add(new SelectorExercise("ex_plancha", getString(R.string.plancha_abdominal),
                SelectorExercise.Group.CORE,   SelectorExercise.Level.INTERMEDIO,   "plancha_demo"));
    }

    // --- Adapter usando item_exercise.xml con CheckBox (cbDone) ---
    static class ExercisePickAdapter extends RecyclerView.Adapter<ExercisePickAdapter.VH> {
        interface OnAnyToggle { void onAnyChange(); }

        private final List<SelectorExercise> data = new ArrayList<>();
        private final Set<String> selected = new HashSet<>();
        private final OnAnyToggle cb;

        ExercisePickAdapter(OnAnyToggle cb) { this.cb = cb; }

        void submit(List<SelectorExercise> list) { data.clear(); data.addAll(list); notifyDataSetChanged(); }

        List<SelectorExercise> getSelected() {
            List<SelectorExercise> out = new ArrayList<>();
            for (SelectorExercise e : data) if (selected.contains(e.id)) out.add(e);
            return out;
        }

        Set<String> getSelectedIds() { return selected; }

        void setSelectedIds(Set<String> ids) {
            selected.clear();
            if (ids != null) selected.addAll(ids);
            notifyDataSetChanged();
        }

        int getSelectedCount() { return selected.size(); }

        @Override public VH onCreateViewHolder(android.view.ViewGroup p, int v) {
            android.view.View view = android.view.LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_checkej, p, false);
            return new VH(view);
        }

        @Override public void onBindViewHolder(VH h, int i) {
            SelectorExercise e = data.get(i);
            h.tvName.setText(e.nombre);
            h.tvMeta.setText(h.itemView.getContext()
                    .getString(R.string.group_level_meta_fmt,
                            groupLabel(h.itemView.getContext(), e.grupo),
                            levelLabel(h.itemView.getContext(), e.nivel)));
            h.img.setImageResource(R.drawable.ic_fitness_center_24);

            boolean sel = selected.contains(e.id);
            h.cb.setChecked(sel);

            h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selected.add(e.id); else selected.remove(e.id);
                if (cb != null) cb.onAnyChange();
            });

            h.itemView.setOnClickListener(v -> h.cb.setChecked(!h.cb.isChecked()));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final android.widget.TextView tvName, tvMeta;
            final android.widget.ImageView img;
            final android.widget.CheckBox cb;

            VH(android.view.View v){
                super(v);
                tvName = v.findViewById(R.id.tvExerciseName);
                tvMeta = v.findViewById(R.id.tvSeriesReps);
                img    = v.findViewById(R.id.imgExercise);
                cb     = v.findViewById(R.id.cbDone);
            }
        }
    }

    // ---- Espaciado uniforme para la grilla ----
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount, spacing;
        private final boolean includeEdge;
        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount; this.spacing = spacing; this.includeEdge = includeEdge;
        }
        @Override public void getItemOffsets(android.graphics.Rect outRect, android.view.View view,
                                             RecyclerView parent, RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            int col = pos % spanCount;
            if (includeEdge) {
                outRect.left  = spacing - col * spacing / spanCount;
                outRect.right = (col + 1) * spacing / spanCount;
                if (pos < spanCount) outRect.top = spacing;
                outRect.bottom = spacing;
            } else {
                outRect.left  = col * spacing / spanCount;
                outRect.right = spacing - (col + 1) * spacing / spanCount;
                if (pos >= spanCount) outRect.top = spacing;
            }
        }
    }

    // ======= Mapeos localizables para Group/Level (con Context) =======
    private static String groupLabel(android.content.Context c, SelectorExercise.Group g) {
        switch (g) {
            case PECHO:    return c.getString(R.string.group_chest);
            case ESPALDA:  return c.getString(R.string.group_back);
            case HOMBRO:   return c.getString(R.string.group_shoulder);
            case PIERNA:   return c.getString(R.string.group_leg);
            case BRAZO:    return c.getString(R.string.group_arm);
            case CORE:     return c.getString(R.string.group_core);
        }
        return "";
    }
    private static String levelLabel(android.content.Context c, SelectorExercise.Level l) {
        switch (l) {
            case PRINCIPIANTE: return c.getString(R.string.level_beginner);
            case INTERMEDIO:   return c.getString(R.string.level_intermediate);
            case AVANZADO:     return c.getString(R.string.level_advanced);
        }
        return "";
    }
}
