package sv.edu.catolica.dc;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/** Modelo de una sesión para historial + Parcelable + (de/a) JSON. */
public class SessionLog implements Parcelable {

    public final long dateMillis;
    public final String title;
    public final String level;
    public final int durationMin;
    public final int completion;
    public final boolean completed;

    public SessionLog(long dateMillis, String title, String level,
                      int durationMin, int completion, boolean completed) {
        this.dateMillis = dateMillis;
        this.title = title;
        this.level = level;
        this.durationMin = durationMin;
        this.completion = completion;
        this.completed = completed;
    }

    // ===== Parcelable =====
    protected SessionLog(Parcel in) {
        dateMillis  = in.readLong();
        title       = in.readString();
        level       = in.readString();
        durationMin = in.readInt();
        completion  = in.readInt();
        completed   = in.readByte() != 0;
    }

    public static final Creator<SessionLog> CREATOR = new Creator<SessionLog>() {
        @Override public SessionLog createFromParcel(Parcel in) { return new SessionLog(in); }
        @Override public SessionLog[] newArray(int size) { return new SessionLog[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dateMillis);
        dest.writeString(title);
        dest.writeString(level);
        dest.writeInt(durationMin);
        dest.writeInt(completion);
        dest.writeByte((byte) (completed ? 1 : 0));
    }

    // ===== JSON helpers (para SessionStore) =====

    /** Convierte el objeto a JSON usando las claves esperadas por SessionStore. */
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("dateMillis",  dateMillis);
            o.put("title",       title);
            o.put("level",       level);
            o.put("durationMin", durationMin);
            o.put("percent",     completion);
            o.put("completed",   completed);
        } catch (JSONException ignored) { }
        return o;
    }

    /** Crea un SessionLog desde JSON. Usa valores por defecto si faltan claves. */
    public static SessionLog fromJson(JSONObject o) {
        if (o == null) {
            return new SessionLog(System.currentTimeMillis(),
                    "Rutina", "Intermedio", 0, 0, false);
        }
        long   dateMillis  = o.optLong("dateMillis", System.currentTimeMillis());
        String title       = o.optString("title", "Rutina");
        String level       = o.optString("level", "Intermedio");
        int    durationMin = o.optInt("durationMin", 0);
        int    completion  = o.optInt("percent", 0);
        boolean completed  = o.optBoolean("completed", false);
        return new SessionLog(dateMillis, title, level, durationMin, completion, completed);
    }
}
