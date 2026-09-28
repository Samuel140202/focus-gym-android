package sv.edu.catolica.dc;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdaptadorEjerciciosEditables extends RecyclerView.Adapter<AdaptadorEjerciciosEditables.VH> {

    public interface Listener {
        void onEdit(int position, Exercise ex);
        void onDelete(int position, Exercise ex);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private final List<Exercise> data = new ArrayList<>();
    private final Listener listener;

    public AdaptadorEjerciciosEditables(List<Exercise> initial, Listener l) {
        if (initial != null) data.addAll(initial);
        this.listener = l;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_editable, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Exercise ex = data.get(pos);
        h.tvName.setText(ex.getName());
        h.tvMeta.setText("Series " + ex.getSeries() + " · Reps " + ex.getReps() + " · Desc " + ex.getRestSeconds() + "s");

        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(h.getBindingAdapterPosition(), ex); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(h.getBindingAdapterPosition(), ex); });

        h.ivDrag.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && listener != null) {
                listener.onStartDrag(h);
                return true;
            }
            return false;
        });
    }

    @Override public int getItemCount() { return data.size(); }

    public List<Exercise> getData() { return new ArrayList<>(data); }
    public void replaceAt(int position, Exercise updated){ data.set(position, updated); notifyItemChanged(position); }
    public void add(Exercise ex){ data.add(ex); notifyItemInserted(data.size()-1); }
    public void deleteAt(int position){ data.remove(position); notifyItemRemoved(position); }
    public void moveItem(int from, int to){ if (from!=to){ Collections.swap(data, from, to); notifyItemMoved(from, to); } }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivDrag; TextView tvName, tvMeta; ImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView){
            super(itemView);
            ivDrag = itemView.findViewById(R.id.ivDrag);
            tvName = itemView.findViewById(R.id.tvName);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
