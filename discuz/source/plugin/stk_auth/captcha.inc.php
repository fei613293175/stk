<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

require_once __DIR__ . '/lib/config.php';
require_once __DIR__ . '/lib/auth_service.php';

$challengeId = trim((string) ($_GET['challenge'] ?? ''));
$row = DB::fetch_first(
    'SELECT challenge_id FROM %t WHERE challenge_id=%s AND client_ip=%s AND status=%s AND expires_at>%d',
    ['stk_auth_captcha', $challengeId, stk_auth_client_ip(), 'active', TIMESTAMP]
);
if (!$row || !function_exists('imagecreatetruecolor')) {
    http_response_code($row ? 503 : 404);
    exit;
}

$image = imagecreatetruecolor(160, 64);
$background = imagecolorallocate($image, 237, 243, 255);
$line = imagecolorallocate($image, 157, 185, 232);
$text = imagecolorallocate($image, 36, 107, 253);
imagefill($image, 0, 0, $background);
imageline($image, 0, 16, 160, 45, $line);
imageline($image, 0, 48, 160, 14, $line);
$code = stk_auth_captcha_code($challengeId);
foreach (str_split($code) as $index => $character) {
    imagestring($image, 5, 25 + ($index * 30), 23 + (($index % 2) * 5), $character, $text);
}
header('Content-Type: image/png');
header('Cache-Control: no-store, max-age=0');
imagepng($image);
imagedestroy($image);
