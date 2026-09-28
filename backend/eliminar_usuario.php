<?php
// eliminar_usuario.php
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
$username = trim($username);

if ($username === '') {
    echo json_encode([
        'ok'  => false,
        'msg' => 'FALTAN_CAMPOS'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// (Opcional pero MUY recomendado) impedir que el admin se borre a sí mismo
// if ($username === 'admin') {
//     echo json_encode([
//         'ok'  => false,
//         'msg' => 'NO_SE_PUEDE_BORRAR_ADMIN'
//     ], JSON_UNESCAPED_UNICODE);
//     exit;
// }

$sql = "DELETE FROM usuarios WHERE username = ? LIMIT 1";
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

if ($stmt->affected_rows > 0) {
    echo json_encode([
        'ok'  => true,
        'msg' => 'USUARIO_ELIMINADO'
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        'ok'  => false,
        'msg' => 'NO_EXISTE_USUARIO'
    ], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conexion->close();
