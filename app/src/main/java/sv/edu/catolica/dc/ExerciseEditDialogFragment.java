package sv.edu.catolica.dc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialogo simple para editar series/reps/descanso de un Exercise.
 * Usa el layout: dialog_edit_exercise.xml (etSeries, etReps, etRest)
 */
public class ExerciseEditDialogFragment extends DialogFragment {

    public interface Callback {
        void onExerciseEdited(int position, Exercise result, boolean isNew);
    }

    private static final String ARG_POS  = "pos";
    private static final String ARG_NAME = "name";
    private static final String ARG_SER  = "ser";
    private static final String ARG_REP  = "rep";
    private static final String ARG_REST = "rest";
    private static final String ARG_NEW  = "isNew";

    public static ExerciseEditDialogFragment newInstance(int position, Exercise ex, boolean isNew) {
        Bundle b = new Bundle();
        b.putInt(ARG_POS, position);
        b.putString(ARG_NAME, ex.getName());
        b.putInt(ARG_SER, ex.getSeries());
        b.putInt(ARG_REP, ex.getReps());
        b.putInt(ARG_REST, ex.getRestSeconds());
        b.putBoolean(ARG_NEW, isNew);
        ExerciseEditDialogFragment f = new ExerciseEditDialogFragment();
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_exercise, null, false);
        EditText etSeries = v.findViewById(R.id.etSeries);
        EditText etReps   = v.findViewById(R.id.etReps);
        EditText etRest   = v.findViewById(R.id.etRest);

        Bundle a = requireArguments();
        final int position = a.getInt(ARG_POS, -1);
        final String name  = a.getString(ARG_NAME, "Ejercicio");
        final boolean isNew = a.getBoolean(ARG_NEW, false);

        // 🔥 Defaults distintos si es nuevo ejercicio
        int defaultSeries = isNew ? 3 : 1;
        int defaultReps   = isNew ? 12 : 1;
        int defaultRest   = isNew ? 45 : 0;

        etSeries.setText(String.valueOf(a.getInt(ARG_SER, defaultSeries)));
        etReps.setText(String.valueOf(a.getInt(ARG_REP, defaultReps)));
        etRest.setText(String.valueOf(a.getInt(ARG_REST, defaultRest)));

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(name)
                .setView(v)
                .setNegativeButton(R.string.btn_cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    int ser  = parseInt(etSeries.getText());
                    int reps = parseInt(etReps.getText());
                    int rest = parseInt(etRest.getText());
                    if (ser >= 1 && reps >= 1 && rest >= 0) {
                        Exercise out = new Exercise(name, ser, reps, rest);
                        if (getActivity() instanceof Callback cb) {
                            cb.onExerciseEdited(position, out, isNew);
                        }
                    }
                })
                .create();
    }

    private int parseInt(CharSequence s){
        try { return Integer.parseInt(String.valueOf(s).trim()); } catch (Exception e){ return -1; }
    }
}
