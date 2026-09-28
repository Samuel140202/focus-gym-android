package sv.edu.catolica.dc;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SessionStore {

    private static final String PREF = "session_store";
    private static final String KEY  = "session_logs";

    /** Agrega un log al inicio (más reciente primero). */
    public static void add(Context ctx, SessionLog log) {
        try {
            List<SessionLog> all = loadAll(ctx);
            all.add(0, log); // más reciente primero

            JSONArray arr = new JSONArray();
            for (SessionLog s : all) arr.put(s.toJson());

            prefs(ctx).edit().putString(KEY, arr.toString()).apply();
        } catch (Exception ignored) { }
    }


    public static List<SessionLog> loadAll(Context ctx) {
        List<SessionLog> out = new ArrayList<>();
        try {
            String raw = prefs(ctx).getString(KEY, "[]");
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                out.add(SessionLog.fromJson(obj));
            }
        } catch (Exception ignored) { }
        return out;
    }


    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY).apply();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}
