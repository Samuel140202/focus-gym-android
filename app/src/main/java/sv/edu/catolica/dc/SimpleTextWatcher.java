package sv.edu.catolica.dc;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {
    public static SimpleTextWatcher on(Runnable r){
        return new SimpleTextWatcher(){ @Override public void afterTextChanged(Editable s){ r.run(); } };
    }
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) { }
}
