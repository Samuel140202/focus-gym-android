package sv.edu.catolica.dc;

import android.app.TimePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.VH> {

    public interface OnChange { void onChanged(); }

    private final List<Reminder> data;
    private final OnChange onChange;

    public ReminderAdapter(List<Reminder> data, OnChange onChange) {
        this.data = data;
        this.onChange = onChange;
        setHasStableIds(true);
    }


    static class VH extends RecyclerView.ViewHolder {
        TextInputEditText etDescription;
        TextView tvTime;
        SwitchCompat swEnabled;
        CheckBox[] days = new CheckBox[7];


        TextWatcher descWatcher;
        CompoundButton.OnCheckedChangeListener switchListener;
        CompoundButton.OnCheckedChangeListener[] dayListeners = new CompoundButton.OnCheckedChangeListener[7];

        VH(@NonNull View v) {
            super(v);
            etDescription = v.findViewById(R.id.etDescription);
            tvTime        = v.findViewById(R.id.tvTime);
            swEnabled     = v.findViewById(R.id.swEnabled);
            days[0] = v.findViewById(R.id.d0);
            days[1] = v.findViewById(R.id.d1);
            days[2] = v.findViewById(R.id.d2);
            days[3] = v.findViewById(R.id.d3);
            days[4] = v.findViewById(R.id.d4);
            days[5] = v.findViewById(R.id.d5);
            days[6] = v.findViewById(R.id.d6);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Reminder r = data.get(pos);
        if (r.days == null || r.days.length != 7) {
            r.days = new boolean[7];
        }


        if (h.descWatcher != null) {
            h.etDescription.removeTextChangedListener(h.descWatcher);
        }
        h.etDescription.setText(r.description == null ? "" : r.description);
        h.descWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                r.description = s.toString();
                if (onChange != null) onChange.onChanged();
            }
        };
        h.etDescription.addTextChangedListener(h.descWatcher);

        // --- Hora ---
        h.tvTime.setText(r.prettyTime12());
        h.tvTime.setOnClickListener(v -> {
            TimePickerDialog dlg = new TimePickerDialog(
                    v.getContext(),
                    (view, hourOfDay, minute) -> {
                        r.hour = hourOfDay;
                        r.minute = minute;
                        h.tvTime.setText(r.prettyTime12());
                        if (onChange != null) onChange.onChanged();
                    },
                    r.hour, r.minute, false
            );
            dlg.show();
        });


        if (h.switchListener != null) {
            h.swEnabled.setOnCheckedChangeListener(null);
        }
        h.swEnabled.setChecked(r.enabled);
        h.switchListener = (buttonView, isChecked) -> {
            r.enabled = isChecked;
            if (onChange != null) onChange.onChanged();
        };
        h.swEnabled.setOnCheckedChangeListener(h.switchListener);


        for (int i = 0; i < 7; i++) {
            if (h.dayListeners[i] != null) {
                h.days[i].setOnCheckedChangeListener(null);
            }
            h.days[i].setChecked(r.days[i]);
            final int idx = i;
            h.dayListeners[i] = (buttonView, isChecked) -> {
                r.days[idx] = isChecked;
                if (onChange != null) onChange.onChanged();
            };
            h.days[i].setOnCheckedChangeListener(h.dayListeners[i]);
        }
    }

    @Override public int getItemCount() { return data.size(); }


    @Override public long getItemId(int position) {
        Reminder r = data.get(position);
        String id = (r != null && r.id != null) ? r.id : ("pos_" + position);
        return id.hashCode();
    }
}
