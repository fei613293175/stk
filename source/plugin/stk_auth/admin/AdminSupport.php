<?php
if (!defined('IN_DISCUZ') || !defined('IN_ADMINCP')) { exit('Access Denied'); }

final class StkAdminSupport {
    public static function requireRoles(array $allowedRoles): void {
        global $_G;
        if ((int)($_G['adminid'] ?? 0) === 1) return;
        $uid = (int)($_G['uid'] ?? 0);
        $bindings = json_decode(self::setting('stk_auth', 'admin_role_bindings', '{}'), true);
        $assigned = is_array($bindings) && isset($bindings[(string)$uid]) && is_array($bindings[(string)$uid])
            ? $bindings[(string)$uid]
            : [];
        if (!array_intersect($allowedRoles, $assigned)) {
            if (function_exists('cpmsg')) cpmsg('noaccess', '', 'error');
            exit('Access Denied');
        }
    }

    public static function setting(string $identifier, string $key, string $default = ''): string {
        global $_G;
        $settings = $_G['cache']['plugin'][$identifier] ?? [];
        return isset($settings[$key]) ? (string)$settings[$key] : $default;
    }

    public static function submitted(string $name): bool {
        return function_exists('submitcheck') ? submitcheck($name) : ($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'POST';
    }

    public static function saveSettings(string $identifier, array $submitted, array $rules): array {
        if (!class_exists('table_common_plugin') || !class_exists('table_common_pluginvar')) {
            throw new RuntimeException('Discuz plugin configuration runtime is unavailable');
        }
        $plugin = table_common_plugin::t()->fetch_by_identifier($identifier);
        if (!$plugin) throw new RuntimeException('Plugin is not installed: ' . $identifier);
        $saved = [];
        foreach ($rules as $key => $rule) {
            if (!array_key_exists($key, $submitted)) continue;
            $value = self::validate((string)$submitted[$key], $rule);
            table_common_pluginvar::t()->update_by_variable((int)$plugin['pluginid'], $key, ['value' => $value]);
            $saved[$key] = $value;
        }
        if ($saved && function_exists('updatecache')) updatecache(['plugin', 'setting']);
        return $saved;
    }

    public static function saveSecret(string $key, string $plaintext, int $adminUid): bool {
        if ($plaintext === '') return false;
        require_once dirname(__DIR__) . '/service/SecretConfig.php';
        $ciphertext = StkSecretConfig::encrypt($plaintext);
        DB::query(
            'INSERT INTO ' . DB::table('stk_secret_config') . ' (config_key,ciphertext,key_version,updated_by,updated_at) VALUES (%s,%s,%d,%d,%s) '
            . 'ON DUPLICATE KEY UPDATE ciphertext=VALUES(ciphertext),key_version=VALUES(key_version),updated_by=VALUES(updated_by),updated_at=VALUES(updated_at)',
            [$key, $ciphertext, 1, $adminUid, gmdate('Y-m-d H:i:s')]
        );
        return true;
    }

    public static function secretConfigured(string $key): bool {
        if (!class_exists('DB')) return false;
        $row = DB::fetch_first('SELECT config_key FROM ' . DB::table('stk_secret_config') . ' WHERE config_key=%s LIMIT 1', [$key]);
        return (bool)$row;
    }

    public static function audit(string $module, string $action, string $targetType, string $targetId = '', $before = null, $after = null): void {
        global $_G;
        if (!class_exists('DB')) return;
        $authKey = (string)($_G['config']['security']['authkey'] ?? '');
        $ip = (string)($_SERVER['REMOTE_ADDR'] ?? '');
        DB::insert('stk_admin_audit', [
            'admin_uid' => (int)($_G['uid'] ?? 0),
            'module' => substr($module, 0, 48),
            'action' => substr($action, 0, 64),
            'target_type' => substr($targetType, 0, 48),
            'target_id' => $targetId === '' ? null : substr($targetId, 0, 128),
            'before_digest' => $before === null ? null : self::digest($before),
            'after_digest' => $after === null ? null : self::digest($after),
            'request_id' => bin2hex(random_bytes(16)),
            'ip_hash' => hash_hmac('sha256', $ip, $authKey !== '' ? $authKey : 'stk-admin-audit'),
            'created_at' => gmdate('Y-m-d H:i:s'),
        ]);
    }

    public static function esc($value): string {
        return htmlspecialchars((string)$value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
    }

    public static function pageUrl(string $identifier, int $pluginId, string $page): string {
        return ADMINSCRIPT . '?action=plugins&operation=config&do=' . $pluginId
            . '&identifier=' . rawurlencode($identifier) . '&pmod=admincp&page=' . rawurlencode($page);
    }

    public static function formStart(string $identifier, int $pluginId, string $page): void {
        global $_G;
        echo '<form method="post" action="' . self::esc(self::pageUrl($identifier, $pluginId, $page)) . '">';
        echo '<input type="hidden" name="formhash" value="' . self::esc($_G['formhash'] ?? '') . '">';
    }

    public static function formEnd(string $submitName = 'stk_submit', string $label = '保存'): void {
        echo '<p><button class="btn" type="submit" name="' . self::esc($submitName) . '" value="1">' . self::esc($label) . '</button></p></form>';
    }

    public static function field(string $name, string $label, string $value, string $type = 'text', string $help = ''): void {
        echo '<tr><th style="width:240px">' . self::esc($label) . '</th><td>';
        if ($type === 'textarea') {
            echo '<textarea class="tarea" rows="6" cols="80" name="settings[' . self::esc($name) . ']">' . self::esc($value) . '</textarea>';
        } elseif ($type === 'password') {
            echo '<input class="txt" type="password" autocomplete="new-password" name="secrets[' . self::esc($name) . ']" value="">';
        } else {
            echo '<input class="txt" type="' . ($type === 'number' ? 'number' : 'text') . '" name="settings[' . self::esc($name) . ']" value="' . self::esc($value) . '">';
        }
        if ($help !== '') echo '<div class="tips2">' . self::esc($help) . '</div>';
        echo '</td></tr>';
    }

    public static function table(string $title, array $headers, array $rows): void {
        if (function_exists('showtableheader')) showtableheader($title);
        else echo '<table class="tb tb2"><caption><h3>' . self::esc($title) . '</h3></caption>';
        echo '<tr class="header">';
        foreach ($headers as $header) echo '<th>' . self::esc($header) . '</th>';
        echo '</tr>';
        foreach ($rows as $row) {
            echo '<tr>';
            foreach ($row as $cell) echo '<td>' . self::esc($cell) . '</td>';
            echo '</tr>';
        }
        if (!$rows) echo '<tr><td colspan="' . count($headers) . '">暂无数据</td></tr>';
        if (function_exists('showtablefooter')) showtablefooter(); else echo '</table>';
    }

    private static function validate(string $value, array $rule): string {
        $type = $rule['type'] ?? 'string';
        if ($type === 'bool') {
            if (!in_array($value, ['0', '1'], true)) throw new InvalidArgumentException('布尔配置只能为 0 或 1');
            return $value;
        }
        if ($type === 'int') {
            if (!preg_match('/^-?[0-9]+$/', $value)) throw new InvalidArgumentException('配置必须为整数');
            $number = (int)$value;
            if ($number < (int)$rule['min'] || $number > (int)$rule['max']) throw new InvalidArgumentException('配置超出允许范围');
            return (string)$number;
        }
        if ($type === 'enum') {
            if (!in_array($value, $rule['values'], true)) throw new InvalidArgumentException('配置值不在允许集合');
            return $value;
        }
        if ($type === 'json_roles') {
            $decoded = json_decode($value, true);
            if (!is_array($decoded) || json_last_error() !== JSON_ERROR_NONE) throw new InvalidArgumentException('角色绑定必须是 JSON 对象');
            $allowed = ['auth_operator','sms_auditor','user_operator','security_auditor','content_admin','project_operator'];
            foreach ($decoded as $uid => $roles) {
                if (!preg_match('/^[1-9][0-9]*$/', (string)$uid) || !is_array($roles) || array_diff($roles, $allowed)) {
                    throw new InvalidArgumentException('角色绑定包含无效 UID 或角色');
                }
            }
            return json_encode($decoded, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        }
        if ($type === 'regex' && !preg_match($rule['pattern'], $value)) throw new InvalidArgumentException('配置格式不正确');
        $length = function_exists('mb_strlen') ? mb_strlen($value, 'UTF-8') : strlen($value);
        if ($length < (int)($rule['min'] ?? 0) || $length > (int)($rule['max'] ?? 65535)) throw new InvalidArgumentException('配置长度不正确');
        return $value;
    }

    private static function digest($value): string {
        return hash('sha256', json_encode($value, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES));
    }
}
