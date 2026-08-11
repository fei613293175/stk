<?php

declare(strict_types=1);

// Guarded production backup for the V1.3.0 deployment. Run from Discuz root.
if (PHP_SAPI !== 'cli') {
    fwrite(STDERR, 'CLI only.' . PHP_EOL);
    exit(2);
}

$requestedDestination = rtrim((string) getenv('STK_V130_BACKUP_DIR'), '/');
if (!preg_match('#^/opt/stk-build/backups/stk-v130-predeploy-[0-9]{14}$#', $requestedDestination)) {
    fwrite(STDERR, 'STK_V130_BACKUP_DIR does not match the guarded backup path.' . PHP_EOL);
    exit(3);
}
if (file_exists($requestedDestination)) {
    fwrite(STDERR, 'Backup destination already exists.' . PHP_EOL);
    exit(4);
}
define('STK_V130_BACKUP_DESTINATION', $requestedDestination);

define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';

require rtrim((string) getcwd(), '/\\') . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();

global $_G;
$destination = STK_V130_BACKUP_DESTINATION;
$db = $_G['config']['db'][1] ?? null;
if (!is_array($db) || empty($db['dbhost']) || empty($db['dbuser']) || empty($db['dbname'])) {
    fwrite(STDERR, 'Discuz database configuration is unavailable.' . PHP_EOL);
    exit(5);
}

function runBackupCommand(array $command, ?array $environment = null): void
{
    $descriptors = [
        0 => ['pipe', 'r'],
        1 => ['pipe', 'w'],
        2 => ['pipe', 'w'],
    ];
    $process = proc_open($command, $descriptors, $pipes, null, $environment);
    if (!is_resource($process)) throw new RuntimeException('Could not start backup command.');
    fclose($pipes[0]);
    $stdout = stream_get_contents($pipes[1]);
    $stderr = stream_get_contents($pipes[2]);
    fclose($pipes[1]);
    fclose($pipes[2]);
    $status = proc_close($process);
    if ($status !== 0) {
        throw new RuntimeException(trim($stderr !== '' ? $stderr : $stdout));
    }
}

try {
    $plugins = $destination . '/plugins';
    if (!mkdir($plugins, 0700, true) && !is_dir($plugins)) {
        throw new RuntimeException('Could not create backup directory.');
    }

    $root = rtrim((string) getcwd(), '/\\');
    runBackupCommand(['/usr/bin/cp', '-a', $root . '/source/plugin/stk_auth', $plugins . '/']);
    runBackupCommand(['/usr/bin/cp', '-a', $root . '/source/plugin/stk_project', $plugins . '/']);

    $dumpPath = $destination . '/database.sql';
    $host = (string) $db['dbhost'];
    $port = '';
    if (preg_match('/^([^:]+):([0-9]+)$/', $host, $match)) {
        $host = $match[1];
        $port = $match[2];
    }
    $command = [
        '/www/server/mysql/bin/mysqldump',
        '--single-transaction',
        '--quick',
        '--skip-lock-tables',
        '--hex-blob',
        '--set-gtid-purged=OFF',
        '--no-tablespaces',
        '--default-character-set=' . ((string) ($db['dbcharset'] ?? 'utf8mb4')),
        '--host=' . $host,
        '--user=' . (string) $db['dbuser'],
        '--result-file=' . $dumpPath,
    ];
    if ($port !== '') $command[] = '--port=' . $port;
    $command[] = (string) $db['dbname'];
    $environment = getenv();
    $environment['MYSQL_PWD'] = (string) ($db['dbpw'] ?? '');
    runBackupCommand($command, $environment);
    chmod($dumpPath, 0600);

    $manifest = [
        'created_at' => date(DATE_ATOM),
        'backup_directory' => $destination,
        'discuz_root' => $root,
        'database_name' => (string) $db['dbname'],
        'database_bytes' => filesize($dumpPath),
        'database_sha256' => hash_file('sha256', $dumpPath),
        'stk_auth_api_sha256' => hash_file('sha256', $plugins . '/stk_auth/api.inc.php'),
        'stk_project_api_sha256' => hash_file('sha256', $plugins . '/stk_project/api.inc.php'),
    ];
    file_put_contents(
        $destination . '/MANIFEST.json',
        json_encode($manifest, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL
    );
    chmod($destination . '/MANIFEST.json', 0600);
    clearstatcache(true, $dumpPath);
    if (!is_dir($destination) || !is_file($dumpPath) || !is_file($destination . '/MANIFEST.json')) {
        throw new RuntimeException('Backup artifacts did not persist at the guarded destination.');
    }
    echo json_encode($manifest, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES) . PHP_EOL;
} catch (Throwable $error) {
    fwrite(STDERR, 'Backup failed: ' . $error->getMessage() . PHP_EOL);
    exit(10);
}
