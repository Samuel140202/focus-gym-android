package sv.edu.catolica.dc;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class SessionHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ---------- Row model ----------
    public static class Row {
        static final int TYPE_HEADER = 0;
        static final int TYPE_ITEM   = 1;

        final int type;
        final String header;
        final SessionLog item;

        private Row(int type, String header, SessionLog item) {
            this.type = type; this.header = header; this.item = item;
        }
        public static Row header(String h){ return new Row(TYPE_HEADER, h, null); }
        public static Row item(SessionLog s){ return new Row(TYPE_ITEM, null, s); }
    }


    private final List<Row> rows;

    public SessionHistoryAdapter(List<Row> rows) {
        this.rows = rows;
    }

    @Override public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == Row.TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_history_header, parent, false);
            return new VHHeader(v);
        } else {
            View v = inf.inflate(R.layout.item_history_session, parent, false);
            return new VHItem(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row r = rows.get(position);
        if (holder instanceof VHHeader) {
            ((VHHeader) holder).tv.setText(r.header);
            return;
        }

        // ----- ITEM -----
        VHItem h = (VHItem) holder;
        SessionLog s = r.item;
        Context ctx = h.itemView.getContext();


        String dateStr;
        try {
            dateStr = SessionHistoryActivity.formatDate(s.dateMillis);
            if (TextUtils.isEmpty(dateStr)) {
                DateFormat df = android.text.format.DateFormat.getDateFormat(ctx);
                dateStr = df.format(new Date(s.dateMillis));
            }
        } catch (Throwable t) {
            DateFormat df = android.text.format.DateFormat.getDateFormat(ctx);
            dateStr = df.format(new Date(s.dateMillis));
        }
        h.tvDate.setText(SessionHistoryActivity.formatDate(s.dateMillis, holder.itemView.getContext()));

        // Título + nivel (internacionalizable con %1$s · %2$s)
        String title = emptyToDefault(s.title, ctx.getString(R.string.app_name));
        String level = emptyToDefault(s.level, "Basic");
        h.tvTitleLevel.setText(
                ctx.getString(R.string.history_title_level_fmt, title, level)
        );

        // Duración
        int minutes = Math.max(1, s.durationMin);
        h.tvDuration.setText(
                ctx.getString(R.string.history_duration_fmt, minutes)
        );

        // Estado
        h.tvStatus.setText(ctx.getString(
                s.completed ? R.string.history_status_completed : R.string.history_status_in_progress
        ));

        // Porcentaje + círculo
        int pct = clamp(s.completion, 0, 100);
        h.tvPercent.setText(ctx.getString(R.string.history_percent_fmt, pct));
        h.circle.setIndeterminate(false);
        h.circle.setMax(100);
        // Usa animación si tu dependencia admite compat:
        h.circle.setProgressCompat(pct, true);
    }

    @Override public int getItemCount() { return rows.size(); }

    // ---------- ViewHolders ----------
    static class VHHeader extends RecyclerView.ViewHolder {
        final TextView tv;
        VHHeader(View v){ super(v); tv = v.findViewById(R.id.tvHeader); }
    }

    static class VHItem extends RecyclerView.ViewHolder {
        final TextView tvDate, tvTitleLevel, tvDuration, tvStatus, tvPercent;
        final CircularProgressIndicator circle;
        VHItem(View v){
            super(v);
            tvDate       = v.findViewById(R.id.tvDate);
            tvTitleLevel = v.findViewById(R.id.tvTitleLevel);
            tvDuration   = v.findViewById(R.id.tvDuration);
            tvStatus     = v.findViewById(R.id.tvStatus);
            tvPercent    = v.findViewById(R.id.tvPercent);
            circle       = v.findViewById(R.id.circle);
        }
    }

    // ---------- Helpers ----------
    private static String emptyToDefault(String s, String def){
        return (s == null || s.trim().isEmpty()) ? def : s;
    }
    private static int clamp(int v, int min, int max){
        return Math.max(min, Math.min(max, v));
    }
}
