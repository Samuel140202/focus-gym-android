<?php
require_once 'conexion.php';

// Permisos CORS básicos
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET');
header('Access-Control-Allow-Headers: Content-Type');

// --- SI ES GET → MOSTRAR FORMULARIO HTML ---
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    ?>
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>Crear usuario (prueba)</title>
    </head>
    <body>
        <h3>Crear usuario (formulario de prueba)</h3>
        <form method="post">
            <label>Username:
                <input type="text" name="username">
            </label><br><br>
            <label>Password:
                <input type="password" name="password">
            </label><br><br>
            <label>Role:
                <select name="role">
                    <option value="admin">admin</option>
                    <option value="user">user</option>
                </select>
            </label><br><br>
            <button type="submit">Crear</button>
        </form>
    </body>
    </html>
    <?php
    exit;
}

// --- A PARTIR DE AQUÍ, SOLO POST (APP / FORMULARIO) ---
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'ok'  => false,
        'msg' => 'METODO_NO_PERMITIDO'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Para POST sí mandamos JSON
header('Content-Type: application/json; charset=utf-8');

$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';
$role     = $_POST['role']     ?? 'user';

$username = trim($username);
$password = trim($password);
$role     = trim($role);

if ($username === '' || $password === '' || $role === '') {
    echo json_encode([
        'ok'  => false,
        'msg' => 'FALTAN_CAMPOS'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Roles permitidos
$rolesPermitidos = ['admin', 'user']; // agrega más si quieres
if (!in_array($role, $rolesPermitidos)) {
    echo json_encode([
        'ok'  => false,
        'msg' => 'ROL_INVALIDO'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$password_hash = password_hash($password, PASSWORD_DEFAULT);

// Insertar usuario
$sql = "INSERT INTO usuarios (username, password_hash, role) VALUES (?,?,?)";
$stmt = $conexion->prepare($sql);

if (!$stmt) {
    echo json_encode([
        'ok'  => false,
        'msg' => 'ERROR_PREPARE',
        'err' => $conexion->error
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$stmt->bind_param('sss', $username, $password_hash, $role);

if ($stmt->execute()) {
    echo json_encode([
        'ok'  => true,
        'msg' => 'USUARIO_CREADO'
    ], JSON_UNESCAPED_UNICODE);
} else {
    if ($conexion->errno == 1062) {
        echo json_encode([
            'ok'  => false,
            'msg' => 'USERNAME_DUP'
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode([
            'ok'  => false,
            'msg' => 'ERROR_INSERTAR',
            'err' => $conexion->error
        ], JSON_UNESCAPED_UNICODE);
    }
}

$stmt->close();
$conexion->close();
