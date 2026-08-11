<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

function stk_project_diagnostic_https(string $url): array
{
    $context = stream_context_create(['http'=>[
        'method'=>'HEAD',
        'timeout'=>4,
        'ignore_errors'=>true,
        'follow_location'=>0,
        'user_agent'=>'STK-Deployment-Diagnostics/1.4',
    ], 'ssl'=>[
        'verify_peer'=>true,
        'verify_peer_name'=>true,
        'capture_peer_cert'=>true,
    ]]);
    $headers = @get_headers($url, true, $context);
    if (!is_array($headers) || !isset($headers[0])) return ['ok'=>false,'status'=>'连接失败','content_type'=>''];
    preg_match('/\s(\d{3})\s/', (string) $headers[0], $match);
    $status = (int) ($match[1] ?? 0);
    $contentType = $headers['Content-Type'] ?? '';
    if (is_array($contentType)) $contentType = end($contentType);
    return ['ok'=>$status >= 200 && $status < 400,'status'=>(string) $status,'content_type'=>(string) $contentType];
}

function stk_project_diagnostic_tls(string $host): array
{
    $context = stream_context_create(['ssl'=>[
        'verify_peer'=>true,
        'verify_peer_name'=>true,
        'peer_name'=>$host,
        'capture_peer_cert'=>true,
        'SNI_enabled'=>true,
    ]]);
    $socket = @stream_socket_client('ssl://'.$host.':443', $errno, $error, 4, STREAM_CLIENT_CONNECT, $context);
    if (!$socket) return ['ok'=>false,'detail'=>'TLS 连接失败'];
    $params = stream_context_get_params($socket);
    fclose($socket);
    $certificate = $params['options']['ssl']['peer_certificate'] ?? null;
    $parsed = $certificate ? @openssl_x509_parse($certificate) : false;
    if (!is_array($parsed)) return ['ok'=>false,'detail'=>'无法读取证书'];
    $expires = (int) ($parsed['validTo_time_t'] ?? 0);
    return [
        'ok'=>$expires > TIMESTAMP,
        'detail'=>$expires > 0 ? '有效期至 '.date('Y-m-d H:i:s', $expires) : '证书有效期未知',
    ];
}

$appHost = stk_project_contract_config('app_links.host', 'stk.zz-yihao.com');
$checks = [
    ['入口与 App Links', $appHost, 'https://'.$appHost.'/.well-known/assetlinks.json', 'application/json'],
    ['Android API 健康', 'stk-api.zz-yihao.com', 'https://stk-api.zz-yihao.com/health/ready', 'application/json'],
    ['Discuz 后台', 'stk-admin.zz-yihao.com', 'https://stk-admin.zz-yihao.com/admin.php', 'text/html'],
    ['静态资源', 'stk-static.zz-yihao.com', 'https://stk-static.zz-yihao.com/', ''],
    ['APK 下载', 'stk-download.zz-yihao.com', 'https://stk-download.zz-yihao.com/', ''],
];

showtableheader('域名与部署检测（只读）');
showtablerow('', [], ['能力','域名 / DNS','HTTPS','TLS','内容类型','结论']);
foreach ($checks as [$label, $host, $url, $expectedType]) {
    $addresses = @gethostbynamel($host) ?: [];
    $https = $addresses ? stk_project_diagnostic_https($url) : ['ok'=>false,'status'=>'未检测','content_type'=>''];
    $tls = $addresses ? stk_project_diagnostic_tls($host) : ['ok'=>false,'detail'=>'DNS 未解析'];
    $typeOk = $expectedType === '' || stripos($https['content_type'], $expectedType) !== false;
    $ok = !empty($addresses) && $https['ok'] && $tls['ok'] && $typeOk;
    showtablerow('', [], [
        dhtmlspecialchars($label),
        dhtmlspecialchars($host.' / '.($addresses ? implode(', ', $addresses) : '未解析')),
        dhtmlspecialchars($https['status']),
        dhtmlspecialchars($tls['detail']),
        dhtmlspecialchars($https['content_type'] ?: '未返回'),
        $ok ? '通过' : '待处理',
    ]);
}
$live = stk_project_diagnostic_https('https://stk-api.zz-yihao.com/health/live');
showtablerow('', [], ['API 存活探针','stk-api.zz-yihao.com',$live['status'],'由上方证书检查覆盖',dhtmlspecialchars($live['content_type'] ?: '未返回'),$live['ok'] ? '通过' : '待处理']);
showtablefooter();

showtableheader('本机运行依赖');
showtablerow('', [], ['项目','当前值','结论']);
showtablerow('', [], ['PHP', PHP_VERSION, version_compare(PHP_VERSION, '7.4.0', '>=') ? '通过' : '版本过低']);
$attachmentPath = rtrim(DISCUZ_ROOT, '/\\').'/data/attachment';
showtablerow('', [], ['上传目录', dhtmlspecialchars($attachmentPath), is_writable($attachmentPath) ? '可写' : '不可写']);
showtablerow('', [], ['数据库就绪', (int) DB::result_first('SELECT COUNT(*) FROM %t', ['stk_project_config']), stk_project_config('schema_version', '') === '14001' ? 'V1.4.0' : '需迁移']);
showtablefooter();
