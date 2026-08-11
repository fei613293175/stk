<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

function stk_auth_normalize_route(string $action, string $resource, string $method): array
{
    $resource = trim($resource, '/');
    $method = strtoupper($method);
    $canonicalActions = [
        'health/live' => 'health_live',
        'health/ready' => 'health_ready',
        'api/v1/bootstrap' => 'bootstrap',
        'api/v1/security/captcha' => 'captcha_create',
        'api/v1/security/captcha/verify' => 'captcha_verify',
        'api/v1/auth/sms/send' => 'sms_send',
        'api/v1/auth/login/password' => 'password_login',
        'api/v1/auth/login/sms' => 'sms_login',
        'api/v1/auth/register' => 'register',
        'api/v1/auth/token/refresh' => 'refresh',
        'api/v1/auth/logout' => 'logout',
        'api/v1/me/summary' => 'me_summary',
    ];
    $expectedMethods = [
        'health/live' => 'GET',
        'health/ready' => 'GET',
        'api/v1/bootstrap' => 'GET',
        'api/v1/security/captcha' => 'GET',
        'api/v1/security/captcha/verify' => 'POST',
        'api/v1/auth/sms/send' => 'POST',
        'api/v1/auth/login/password' => 'POST',
        'api/v1/auth/login/sms' => 'POST',
        'api/v1/auth/register' => 'POST',
        'api/v1/auth/token/refresh' => 'POST',
        'api/v1/auth/logout' => 'POST',
        'api/v1/me/summary' => 'GET',
    ];
    $documentId = '';
    if (isset($canonicalActions[$resource])) {
        $action = $canonicalActions[$resource];
    } elseif (preg_match('#^api/v1/legal/(user[_-]agreement|privacy[_-]policy)$#', $resource, $match)) {
        $action = 'legal_document';
        $documentId = str_replace('-', '_', $match[1]);
        $expectedMethods[$resource] = 'GET';
    }
    return [
        'action' => $action,
        'resource' => $resource,
        'method' => $method,
        'expected_method' => $expectedMethods[$resource] ?? '',
        'document_id' => $documentId,
    ];
}

