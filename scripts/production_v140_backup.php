<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    fwrite(STDERR, 'CLI only.' . PHP_EOL);
    exit(2);
}

$stkV140BackupDestination = rtrim((string) getenv('STK_V140_BACKUP_DIR'), '/');
if (!preg_match('#^/opt/stk-build/backups/stk-v140-predeploy-[0-9]{14}$#', $stkV140BackupDestination)) {
    fwrite(STDERR, 'STK_V140_BACKUP_DIR does not match the guarded backup path.' . PHP_EOL);
    exit(3);
}
if (file_exists($stkV140BackupDestination)) {
    fwrite(STDERR, 'Backup destination already exists.' . PHP_EOL);
    exit(4);
}
$stkV140DiscuzRoot = rtrim((string) getenv('STK_DISCUZ_ROOT'), '/\\');
if ($stkV140DiscuzRoot !== '/www/wwwroot/stk_zz_yihao_com' || !is_file($stkV140DiscuzRoot . '/source/class/class_core.php')) {
    fwrite(STDERR, 'STK_DISCUZ_ROOT is not the guarded production root.' . PHP_EOL);
    exit(5);
}

define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';
chdir($stkV140DiscuzRoot);
require $stkV140DiscuzRoot . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();

// Discuz clears non-whitelisted globals during bootstrap, so recover guarded inputs afterward.
$stkV140BackupDestination = rtrim((string) getenv('STK_V140_BACKUP_DIR'), '/');
$stkV140DiscuzRoot = rtrim((string) getenv('STK_DISCUZ_ROOT'), '/\\');
if (
    !preg_match('#^/opt/stk-build/backups/stk-v140-predeploy-[0-9]{14}$#', $stkV140BackupDestination) ||
    $stkV140DiscuzRoot !== '/www/wwwroot/stk_zz_yihao_com'
) {
    fwrite(STDERR, 'Guarded backup inputs were not preserved after Discuz bootstrap.' . PHP_EOL);
    exit(6);
}

global $_G;
$db = $_G['config']['db'][1] ?? null;
if (!is_array($db) || empty($db['dbhost']) || empty($db['dbuser']) || empty($db['dbname'])) {
    fwrite(STDERR, 'Discuz database configuration is unavailable.' . PHP_EOL);
    exit(5);
}

function stkV140Run(array $command, ?array $environment = null): void
{
    $process = proc_open($command, [0=>['pipe','r'],1=>['pipe','w'],2=>['pipe','w']], $pipes, null, $environment);
    if (!is_resource($process)) throw new RuntimeException('Could not start backup command.');
    fclose($pipes[0]);
    $stdout = stream_get_contents($pipes[1]);
    $stderr = stream_get_contents($pipes[2]);
    fclose($pipes[1]);
    fclose($pipes[2]);
    $status = proc_close($process);
    if ($status !== 0) throw new RuntimeException(trim($stderr !== '' ? $stderr : $stdout));
}

try {
    $plugins = $stkV140BackupDestination . '/plugins';
    if (!mkdir($plugins, 0700, true) && !is_dir($plugins)) throw new RuntimeException('Could not create backup directory.');
    $root = $stkV140DiscuzRoot;
    stkV140Run(['/usr/bin/cp', '-a', $root . '/source/plugin/stk_auth', $plugins . '/']);
    stkV140Run(['/usr/bin/cp', '-a', $root . '/source/plugin/stk_project', $plugins . '/']);
    foreach (['.well-known','stk-release'] as $releasePath) {
        if (file_exists($root . '/' . $releasePath)) stkV140Run(['/usr/bin/cp', '-a', $root . '/' . $releasePath, $stkV140BackupDestination . '/']);
    }

    $dumpPath = $stkV140BackupDestination . '/database.sql';
    $host = (string) $db['dbhost'];
    $port = '';
    if (preg_match('/^([^:]+):([0-9]+)$/', $host, $match)) {
        $host = $match[1];
        $port = $match[2];
    }
    $command = [
        '/www/server/mysql/bin/mysqldump','--single-transaction','--quick','--skip-lock-tables',
        '--hex-blob','--set-gtid-purged=OFF','--no-tablespaces',
        '--default-character-set=' . ((string) ($db['dbcharset'] ?? 'utf8mb4')),
        '--host=' . $host,'--user=' . (string) $db['dbuser'],'--result-file=' . $dumpPath,
    ];
    if ($port !== '') $command[] = '--port=' . $port;
    $command[] = (string) $db['dbname'];
    $environment = getenv();
    $environment['MYSQL_PWD'] = (string) ($db['dbpw'] ?? '');
    stkV140Run($command, $environment);
    chmod($dumpPath, 0600);

    $manifest = [
        'created_at'=>date(DATE_ATOM),
        'release'=>'1.4.0-beta.1',
        'backup_directory'=>$stkV140BackupDestination,
        'discuz_root'=>$root,
        'database_name'=>(string) $db['dbname'],
        'database_bytes'=>filesize($dumpPath),
        'database_sha256'=>hash_file('sha256', $dumpPath),
        'stk_auth_api_sha256'=>hash_file('sha256', $plugins . '/stk_auth/api.inc.php'),
        'stk_project_api_sha256'=>hash_file('sha256', $plugins . '/stk_project/api.inc.php'),
    ];
    file_put_contents($stkV140BackupDestination . '/MANIFEST.json', json_encode($manifest, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES) . PHP_EOL);
    chmod($stkV140BackupDestination . '/MANIFEST.json', 0600);
    if (!is_file($dumpPath) || !is_file($stkV140BackupDestination . '/MANIFEST.json')) throw new RuntimeException('Backup artifacts did not persist.');
    DB::insert('stk_admin_audit', [
        'operator_uid'=>0,
        'section'=>'deployment',
        'action'=>'backup_created',
        'payload_json'=>json_encode(['release'=>'1.4.0-beta.1','directory'=>$stkV140BackupDestination,'database_sha256'=>$manifest['database_sha256']], JSON_UNESCAPED_SLASHES),
        'ip'=>'127.0.0.1',
        'created_at'=>TIMESTAMP,
    ]);
    echo json_encode($manifest, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES) . PHP_EOL;
} catch (Throwable $error) {
    fwrite(STDERR, 'Backup failed: ' . $error->getMessage() . PHP_EOL);
    exit(10);
}
