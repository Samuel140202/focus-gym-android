package sv.edu.catolica.dc;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoutineStore {
    private static final String PREF="routines_store", KEY="packs_json";
    private static final Gson gson=new Gson();
    private static SharedPreferences sp(Context c){ return c.getSharedPreferences(PREF, Context.MODE_PRIVATE); }

    public static List<RoutinePack> loadAll(Context c){
        String json = sp(c).getString(KEY, "[]");
        Type t = new TypeToken<ArrayList<RoutinePack>>(){}.getType();
        List<RoutinePack> out = gson.fromJson(json, t);
        return out==null? new ArrayList<>() : out;
    }
    public static void saveAll(Context c, List<RoutinePack> packs){
        sp(c).edit().putString(KEY, gson.toJson(packs)).apply();
    }
    public static void add(Context c, Routine r, ArrayList<Exercise> exs){
        List<RoutinePack> all = loadAll(c);
        all.add(new RoutinePack(r, exs));
        saveAll(c, all);
    }
    public static void clear(Context c){ sp(c).edit().remove(KEY).apply(); }

    public static void deleteAt(Context ctx, int index){
        List<RoutinePack> all = loadAll(ctx);
        if (index >= 0 && index < all.size()) {
            all.remove(index);
            saveAll(ctx, all);
        }
    }

}
