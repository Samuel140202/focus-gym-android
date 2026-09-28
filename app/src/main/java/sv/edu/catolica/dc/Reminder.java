package sv.edu.catolica.dc;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class Reminder {

    // ✅ Contrato usado por Adapter/Scheduler/Receiver
    // id único (obligatorio para PendingIntent)
    public String id;

    // texto opcional que mostramos en la notificación
    public String description = "";

    // si el recordatorio está activo
    public boolean enabled = true;

    // hora en 24h (0..23) y minuto (0..59)
    public int hour;
    public int minute;

    // días de la semana (0..6 → L..D)
    public boolean[] days = new boolean[7];

    // ----- Constructores -----
    public Reminder() {
        // Defaults razonables: ahora mismo, día de hoy marcado
        if (id == null || id.trim().isEmpty()) {
            id = "r_" + UUID.randomUUID();
        }
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // marca el día actual (0..6 → L..D)
        int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        days[today] = true;
    }

    public Reminder(String description, int hour, int minute, boolean enabled, boolean[] days) {
        this();
        this.description = description == null ? "" : description;
        this.hour = clamp(hour, 0, 23);
        this.minute = clamp(minute, 0, 59);
        this.enabled = enabled;
        if (days != null && days.length == 7) this.days = days.clone();
    }

    // ----- Utilidades -----

    /** Formato 12h con cero a la izquierda, ej. "07:05 AM" */
    public String prettyTime12() {
        int h = hour % 12;
        if (h == 0) h = 12;
        String ampm = hour < 12 ? "AM" : "PM";
        return String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, ampm);
    }

    /** Asegura que el objeto tenga valores válidos (útil tras deserializar con Gson). */
    public void ensureDefaults() {
        if (id == null || id.trim().isEmpty()) id = "r_" + UUID.randomUUID();
        if (days == null || days.length != 7) days = new boolean[7];
        hour = clamp(hour, 0, 23);
        minute = clamp(minute, 0, 59);
    }

    /** Validación rápida para decidir si se puede programar. */
    public boolean isSchedulable() {
        if (!enabled) return false;
        if (days == null || days.length != 7) return false;
        boolean anyDay = false;
        for (boolean d : days) { if (d) { anyDay = true; break; } }
        return anyDay;
    }

    private static int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
