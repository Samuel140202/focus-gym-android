<?php
// conexion.php
// Conexión SOLO para el módulo de login nuevo

$hostname = 'localhost';
$database = 'login_app_db'; // 👈 La BD nueva que creaste
$username = 'root';         // XAMPP por defecto
$password = '';             // XAMPP por defecto (sin contraseña)

// Crear conexión
$conexion = new mysqli($hostname, $username, $password, $database);

// Verificar error de conexión
if ($conexion->connect_errno) {
    http_response_code(500);
    echo json_encode([
        'ok'  => false,
        'msg' => 'ERROR_CONEXION_BD',
        'err' => $conexion->connect_error,
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Charset
$conexion->set_charset('utf8mb4');
?>
