package sv.edu.catolica.dc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar la lista de ejercicios dentro del detalle de rutina.
 * - Soporta selección de 1 ejercicio (tarjeta seleccionada).
 * - El check (done) marca progreso y puede usarse como selección múltiple.
 *   PRIORIDAD de selección para getSelected(): checks marcados > tarjeta seleccionada.
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private final Context context;
    private final List<Exercise> exercises = new ArrayList<>();

    // Índice seleccionado. -1 = sin selección
    private int selectedIndex = -1;

    public ExerciseAdapter(Context context) {
        this.context = context;
    }

    // Actualiza lista
    public void setExercises(List<Exercise> newExercises) {
        exercises.clear();
        if (newExercises != null) exercises.addAll(newExercises);
        if (selectedIndex >= exercises.size()) selectedIndex = -1; // reset si cambió tamaño
        notifyDataSetChanged();
    }

    // Copia defensiva si la pides
    public List<Exercise> getExercises() {
        return new ArrayList<>(exercises);
    }

    /** Devuelve -1 cuando no hay selección (importante para no forzar 0). */
    public int getSelectedIndex() {
        return (selectedIndex >= 0 && selectedIndex < exercises.size()) ? selectedIndex : -1;
    }

    /** Permite fijar la selección externamente. */
    public void setSelectedIndex(int index) {
        int old = selectedIndex;
        if (index < 0 || index >= exercises.size()) {
            selectedIndex = -1;
        } else {
            selectedIndex = index;
        }
        if (old != -1) notifyItemChanged(old);
        if (selectedIndex != -1) notifyItemChanged(selectedIndex);
    }

    /**
     *
     * Devuelve la lista seleccionada para iniciar sesión:
     * 1) Si hay checks marcados (done), devuelve esos.
     * 2) Si no hay checks y hay tarjeta seleccionada, devuelve solo esa.
     * 3) Si nada está seleccionado, devuelve lista vacía (tu startSession hace fallback).
     */
    public ArrayList<Exercise> getSelected() {
        ArrayList<Exercise> out = new ArrayList<>();
        // 1) Prioridad: checks marcados
        for (Exercise e : exercises) {
            if (safeIsDone(e)) out.add(e);
        }
        if (!out.isEmpty()) return out;

        // 2) Si no hay checks, usa la tarjeta seleccionada
        if (selectedIndex >= 0 && selectedIndex < exercises.size()) {
            out.add(exercises.get(selectedIndex));
        }
        return out; // 3) puede ir vacía y el caller decide fallback
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkej, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise e = exercises.get(position);

        holder.tvName.setText(e.getName());
        holder.tvSeriesReps.setText("Series x Reps: " + e.getSeries() + " x " + e.getReps());
        holder.tvRest.setText("Descanso: " + e.getRestSeconds() + "s");
        holder.imgExercise.setImageResource(e.getImageRes());

        // Estado visual de selección
        boolean isSelected = (position == selectedIndex);
        holder.itemView.setAlpha(isSelected ? 1.0f : 0.92f);

        // Evitar cascadas al reciclar
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(safeIsDone(e));

        // Marcar progreso y (conveniente) seleccionar esa tarjeta
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            safeSetDone(e, isChecked);
            if (isChecked) setSelectedIndex(holder.getBindingAdapterPosition());
        });

        // Tap en la tarjeta → seleccionar
        holder.itemView.setOnClickListener(v -> setSelectedIndex(holder.getBindingAdapterPosition()));

        // Long press → deseleccionar si era la seleccionada
        holder.itemView.setOnLongClickListener(v -> {
            if (selectedIndex == holder.getBindingAdapterPosition()) {
                setSelectedIndex(-1);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    // --- Helpers seguros (por si el modelo cambia nombre de métodos/campos) ---
    private boolean safeIsDone(Exercise e) {
        try { return e.isDone(); } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Field f = Exercise.class.getDeclaredField("done");
            f.setAccessible(true);
            return f.getBoolean(e);
        } catch (Throwable ignored) {}
        return false;
    }

    private void safeSetDone(Exercise e, boolean v) {
        try { e.setDone(v); return; } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Field f = Exercise.class.getDeclaredField("done");
            f.setAccessible(true);
            f.setBoolean(e, v);
        } catch (Throwable ignored) {}
    }

    // ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgExercise;
        TextView tvName, tvSeriesReps, tvRest;
        CheckBox cbDone;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExercise = itemView.findViewById(R.id.imgExercise);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvSeriesReps = itemView.findViewById(R.id.tvSeriesReps);
            tvRest = itemView.findViewById(R.id.tvRest);
            cbDone = itemView.findViewById(R.id.cbDone);
        }
    }
}
