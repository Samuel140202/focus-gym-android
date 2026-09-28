<?php
// login.php
header('Content-Type: application/json; charset=utf-8');

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'conexion.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'ok'  => false,
        'msg' => 'METODO_NO_PERMITIDO'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

$username = trim($username);
$password = trim($password);

if ($username === '' || $password === '') {
    echo json_encode([
        'ok'  => false,
        'msg' => 'FALTAN_CAMPOS'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Buscar usuario
$sql = "SELECT id, username, password_hash, role FROM usuarios WHERE username = ? LIMIT 1";
$stmt = $conexion->prepare($sql);

if (!$stmt) {
    echo json_encode([
        'ok'  => false,
        'msg' => 'ERROR_PREPARE',
        'err' => $conexion->error
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$stmt->bind_param('s', $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        'ok'  => false,
        'msg' => 'CREDENCIALES_INVALIDAS'
    ], JSON_UNESCAPED_UNICODE);
    $stmt->close();
    $conexion->close();
    exit;
}

$usuario = $result->fetch_assoc();

// Verificar contraseña
if (!password_verify($password, $usuario['password_hash'])) {
    echo json_encode([
        'ok'  => false,
        'msg' => 'CREDENCIALES_INVALIDAS'
    ], JSON_UNESCAPED_UNICODE);
    $stmt->close();
    $conexion->close();
    exit;
}

// Login OK
echo json_encode([
    'ok'  => true,
    'msg' => 'OK',
    'user' => [
        'id'       => (int)$usuario['id'],
        'username' => $usuario['username'],
        'role'     => $usuario['role']
    ]
], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conexion->close();
