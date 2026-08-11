<?php

define('IN_DISCUZ', true);

require_once dirname(__DIR__) . '/source/plugin/stk_auth/lib/routes.php';
require_once dirname(__DIR__) . '/source/plugin/stk_project/lib/routes.php';

function assert_same($expected, $actual, string $label): void
{
    if ($expected !== $actual) {
        fwrite(
            STDERR,
            'FAIL: ' . $label . '; expected=' . var_export($expected, true)
            . '; actual=' . var_export($actual, true) . PHP_EOL
        );
        exit(1);
    }
}

function assert_auth_route(
    string $action,
    string $resource,
    string $method,
    string $expectedAction,
    string $expectedMethod,
    string $documentId = ''
): void {
    $route = stk_auth_normalize_route($action, $resource, $method);
    $label = $method . ' ' . $resource;
    assert_same($expectedAction, $route['action'], $label . ' action');
    assert_same(strtoupper($method), $route['method'], $label . ' request method');
    assert_same($expectedMethod, $route['expected_method'], $label . ' expected method');
    assert_same($documentId, $route['document_id'], $label . ' document id');
}

assert_auth_route('', 'api/v1/security/captcha', 'GET', 'captcha_create', 'GET');
assert_auth_route('', 'api/v1/security/captcha/verify', 'POST', 'captcha_verify', 'POST');
assert_auth_route('', 'api/v1/legal/user-agreement', 'GET', 'legal_document', 'GET', 'user_agreement');
assert_auth_route('', 'api/v1/legal/privacy_policy', 'GET', 'legal_document', 'GET', 'privacy_policy');

$wrongMethod = stk_auth_normalize_route('', 'api/v1/security/captcha', 'POST');
assert_same('POST', $wrongMethod['method'], 'wrong auth method is preserved');
assert_same('GET', $wrongMethod['expected_method'], 'wrong auth method remains detectable');

$legacy = stk_auth_normalize_route('password_login', '', 'POST');
assert_same('password_login', $legacy['action'], 'legacy action is preserved');
assert_same('', $legacy['expected_method'], 'legacy action has no REST method restriction');

function assert_project_route(
    string $resource,
    string $method,
    string $expectedResource,
    string $expectedMethod,
    string $expectedAction = ''
): void {
    $route = stk_project_normalize_route($resource, $method);
    $label = $method . ' ' . $resource;
    assert_same($expectedResource, $route['resource'], $label . ' resource');
    assert_same($expectedMethod, $route['method'], $label . ' method');
    assert_same($expectedAction, $route['action'], $label . ' action');
}

assert_project_route('api/v1/projects', 'POST', 'create', 'POST');
assert_project_route('api/v1/projects/42', 'PUT', 'update/42', 'POST');
assert_project_route('api/v1/projects/42', 'DELETE', 'mine/42', 'POST', 'delete');
assert_project_route('api/v1/projects/42/offline', 'POST', 'mine/42', 'POST', 'unpublish');
assert_project_route('api/v1/projects/42/resubmit', 'POST', 'resubmit/42', 'POST');
assert_project_route('api/v1/projects/42', 'GET', 'projects/42', 'GET');
assert_project_route('api/v1/projects/42/view', 'POST', 'projects/42/view', 'POST');
assert_project_route('api/v1/project/categories', 'GET', 'categories', 'GET');
assert_project_route('api/v1/releases/current', 'GET', 'release/current', 'GET');
assert_project_route('api/v1/media/project-images', 'POST', 'upload', 'POST');
assert_project_route('api/v1/media/project-images/77', 'DELETE', 'media/project-images/77', 'DELETE');

echo "PASS: route_contract_test\n";
