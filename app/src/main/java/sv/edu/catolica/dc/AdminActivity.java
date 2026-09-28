package sv.edu.catolica.dc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class AdminActivity extends AppCompatActivity {

    // ====== CONFIG DE RUTA API (MISMA QUE EN LoginActivity) ======
    // Emulador:
    //private static final String EMULATOR_BASE_URL = "http://10.0.2.2:8080/login/";
    // Telefono (ip de la pc):
    private static final String DEVICE_BASE_URL   = "http://192.168.1.3:8080/login/";
    //(ip generada por pasar datos)
    //private static final String DEVICE_BASE_URL   = "http://192.168.1.100:8080/login/";


    private static final String BASE_URL = DEVICE_BASE_URL;
    private static String url(String ep) { return BASE_URL + ep; }


    private EditText edtNewUser, edtNewPass, edtDeleteUser;
    private Spinner spRole;
    private Button btnCreateUser, btnLogoutAdmin, btnDeleteUser;
    private ProgressBar progress;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // --- Referencias a la UI ---
        edtNewUser    = findViewById(R.id.edtNewUser);
        edtNewPass    = findViewById(R.id.edtNewPass);
        edtDeleteUser = findViewById(R.id.edtDeleteUser);
        spRole        = findViewById(R.id.spRole);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnLogoutAdmin= findViewById(R.id.btnLogoutAdmin);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
        progress      = findViewById(R.id.progressAdmin);

        queue = Volley.newRequestQueue(this);

        // --- Spinner de roles ---
        String[] roles = {"admin", "user"}; // agrega más si se requiere
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        // --- Listeners ---
        btnCreateUser.setOnClickListener(v -> createUser());
        btnDeleteUser.setOnClickListener(v -> deleteUser());
        btnLogoutAdmin.setOnClickListener(v -> logout());
    }

    private void setLoading(boolean loading) {
        btnCreateUser.setEnabled(!loading);
        btnDeleteUser.setEnabled(!loading);
        btnLogoutAdmin.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    // ===================== CREAR USUARIO =====================

    private void createUser() {
        final String u    = edtNewUser.getText().toString().trim();
        final String p    = edtNewPass.getText().toString().trim();
        final String role = spRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(u)) {
            edtNewUser.setError("Requerido");
            edtNewUser.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(p)) {
            edtNewPass.setError("Requerido");
            edtNewPass.requestFocus();
            return;
        }
        if (p.length() < 4) {
            edtNewPass.setError("Mínimo 4 caracteres");
            edtNewPass.requestFocus();
            return;
        }

        setLoading(true);

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url("crear_usuario.php"),
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
                            Toast.makeText(this,
                                    "Usuario creado correctamente",
                                    Toast.LENGTH_SHORT).show();
                            edtNewUser.setText("");
                            edtNewPass.setText("");
                            edtNewUser.requestFocus();
                        } else {
                            String msg = o.optString("msg", "Error al crear usuario");
                            if ("USERNAME_DUP".equals(msg)) {
                                Toast.makeText(this,
                                        "El nombre de usuario ya existe",
                                        Toast.LENGTH_SHORT).show();
                            } else if ("ROL_INVALIDO".equals(msg)) {
                                Toast.makeText(this,
                                        "Rol inválido",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this,
                                        "Error: " + msg,
                                        Toast.LENGTH_SHORT).show();
                            }
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
                pms.put("role", role); // "admin" o "user"
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

    // ===================== ELIMINAR USUARIO =====================

    private void deleteUser() {
        final String u = edtDeleteUser.getText().toString().trim();

        if (TextUtils.isEmpty(u)) {
            edtDeleteUser.setError("Requerido");
            edtDeleteUser.requestFocus();
            return;
        }

        // Opcional: evitar borrar al admin principal
        if ("admin".equalsIgnoreCase(u)) {
            Toast.makeText(this,
                    "No se puede borrar este usuario admin",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url("eliminar_usuario.php"),
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
                            Toast.makeText(this,
                                    "Usuario eliminado correctamente",
                                    Toast.LENGTH_SHORT).show();
                            edtDeleteUser.setText("");
                        } else {
                            String msg = o.optString("msg", "Error al eliminar");
                            if ("NO_EXISTE_USUARIO".equals(msg)) {
                                Toast.makeText(this,
                                        "Ese usuario no existe",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this,
                                        "Error: " + msg,
                                        Toast.LENGTH_SHORT).show();
                            }
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

    // ===================== CERRAR SESIÓN =====================

    private void logout() {
        // Limpiar sesión guardada
        getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Volver al login limpiando el back stack
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
