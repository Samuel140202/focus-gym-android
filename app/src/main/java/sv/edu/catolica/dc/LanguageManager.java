package sv.edu.catolica.dc;

import android.app.Activity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LanguageManager {
    private LanguageManager() {}

    /** tag: "system", "es", "en", "pt" */
    public static void applyAppLanguage(String tag) {
        LocaleListCompat locales = ("system".equals(tag) || tag == null)
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    public static void recreate(Activity a) {
        if (a != null) a.recreate();
    }
}
