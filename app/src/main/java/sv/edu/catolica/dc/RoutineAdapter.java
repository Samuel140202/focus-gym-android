package sv.edu.catolica.dc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.VH> {

    public static class Routine {
        public final String title;
        public final String meta;
        public final int progress;
        public final int imageRes;

        public Routine(String title, String meta, int progress, int imageRes) {
            this.title = title;
            this.meta = meta;
            this.progress = progress;
            this.imageRes = imageRes;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMeta;
        LinearProgressIndicator progress;
        MaterialButton btnStart;
        ImageView img;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tvTitle);
            tvMeta   = itemView.findViewById(R.id.tvMeta);
            progress = itemView.findViewById(R.id.progress);
            btnStart = itemView.findViewById(R.id.btnStart);
            img      = itemView.findViewById(R.id.img);
        }
    }

    // ⇩⇩⇩ NUEVO: listener con onLong
    public interface OnItemClick {
        void onOpen(int position);
        void onStart(int position);
        void onLong(int position);
    }
    private OnItemClick listener;
    public void setOnItemClick(OnItemClick l) { this.listener = l; }

    private final List<Routine> data;
    public RoutineAdapter(List<Routine> data) { this.data = data; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Routine r = data.get(pos);
        h.tvTitle.setText(r.title);
        h.tvMeta.setText(r.meta);
        h.progress.setProgress(r.progress);

        if (r.imageRes != 0) {
            h.img.setImageResource(r.imageRes);
            h.img.setVisibility(View.VISIBLE);
        } else {
            h.img.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(h.getBindingAdapterPosition());
        });
        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLong(h.getBindingAdapterPosition());
            return true;
        });
        h.btnStart.setOnClickListener(v -> {
            if (listener != null) listener.onStart(h.getBindingAdapterPosition());
        });
    }

    @Override public int getItemCount() { return data != null ? data.size() : 0; }
}
