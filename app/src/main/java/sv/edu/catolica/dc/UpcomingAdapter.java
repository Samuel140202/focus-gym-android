package sv.edu.catolica.dc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.VH> {

    private final List<UpcomingItem> data;

    public UpcomingAdapter(List<UpcomingItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        UpcomingItem it = data.get(pos);
        h.title.setText(it.title);
        h.meta.setText(it.meta);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, meta;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            meta  = v.findViewById(R.id.tvMeta);
        }
    }
}
