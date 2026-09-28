package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private static final String EMULATOR_BASE_URL = "http://10.0.2.2:8080/login/";

    private static final String DEVICE_BASE_URL   = "http://192.168.1.3:8080/login/";
    //private static final String DEVICE_BASE_URL   = "http://192.168.1.100:8080/login/";


    private static final String BASE_URL = DEVICE_BASE_URL;
    private static String url(String ep) { return BASE_URL + ep; }
    // ===============================================

    private EditText edtUser, edtPass;
    private Button btnLogin;
    private ProgressBar progress;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        edtUser   = findViewById(R.id.edtUser);
        edtPass   = findViewById(R.id.edtPass);
        btnLogin  = findViewById(R.id.btnLogin);
        progress  = findViewById(R.id.progress);
        queue     = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void doLogin() {
        final String u = edtUser.getText().toString().trim();
        final String p = edtPass.getText().toString().trim();

        if (TextUtils.isEmpty(u)) {
            edtUser.setError("Requerido");
            edtUser.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(p)) {
            edtPass.setError("Requerido");
            edtPass.requestFocus();
            return;
        }
        if (p.length() < 4) {
            edtPass.setError("Mínimo 4 caracteres");
            edtPass.requestFocus();
            return;
        }

        setLoading(true);

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url("login.php"),
                resp -> {
                    setLoading(false);
                    try {
                        String raw = resp == null ? "" : resp.trim();
                        if (raw.startsWith("<")) {
                            Toast.makeText(this,
                                    "Servidor devolvió HTML (no JSON). Revisa PHP/URL.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject o = new JSONObject(raw);
                        if (o.optBoolean("ok", false)) {
                            JSONObject user = o.optJSONObject("user");
                            if (user == null) user = new JSONObject();

                            String username = user.optString("username", "");
                            String role     = user.optString("role", "user");

                            // Guardar sesión básica
                            getSharedPreferences("auth", MODE_PRIVATE).edit()
                                    .putBoolean("logged", true)
                                    .putString("username", username)
                                    .putString("role", role)
                                    .apply();

                            Toast.makeText(this,
                                    "Bienvenido " + username + " (" + role + ")",
                                    Toast.LENGTH_SHORT).show();

                            // Redirigir según el rol:
                            Intent i;
                            if ("admin".equalsIgnoreCase(role)) {
                                // Admin → panel de administrador
                                i = new Intent(this, AdminActivity.class);
                            } else {
                                // Usuarios normales → HomeActivity (la app de verdad)
                                i = new Intent(this, HomeActivity.class);
                            }



                            // Limpiar back stack para no volver al login con back
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                        } else {
                            Toast.makeText(this,
                                    o.optString("msg", "Credenciales inválidas"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Respuesta inválida del servidor",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                err -> {
                    setLoading(false);
                    Toast.makeText(this,
                            "Error de red: " + err.toString(),
                            Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> pms = new HashMap<>();
                pms.put("username", u);
                pms.put("password", p);
                return pms;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                1,
                1.0f
        ));

        queue.add(req);
    }
}
