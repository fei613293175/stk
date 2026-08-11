<?php

if (!defined('IN_DISCUZ')) exit('Access Denied');

function stk_project_normalize_route(string $resource, string $method): array
{
    $resource = trim($resource, '/');
    $method = strtoupper($method);
    if (strpos($resource, 'api/v1/') === 0) $resource = substr($resource, 7);
    if ($resource === 'project/categories') $resource = 'categories';
    if ($resource === 'me/projects') $resource = 'mine';
    if ($resource === 'releases/current') $resource = 'release/current';
    if ($resource === 'media/project-images') $resource = 'upload';
    // V1.2 canonical upload paths are aliases of the existing upload service.
    if ($resource === 'uploads/project-images') $resource = 'upload';
    if (preg_match('#^uploads/project-images/(\d+)$#', $resource, $uploadMatch)) {
        $resource = 'media/project-images/' . $uploadMatch[1];
    }
    if ($resource === 'projects' && $method === 'POST') $resource = 'create';

    $action = '';
    if (preg_match('#^projects/(\d+)$#', $resource, $match)) {
        if ($method === 'PUT') {
            $resource = 'update/' . $match[1];
            $method = 'POST';
        } elseif ($method === 'DELETE') {
            $resource = 'mine/' . $match[1];
            $method = 'POST';
            $action = 'delete';
        }
    } elseif (preg_match('#^projects/(\d+)/(offline|resubmit)$#', $resource, $match)) {
        $resource = $match[2] === 'resubmit' ? 'resubmit/' . $match[1] : 'mine/' . $match[1];
        $method = 'POST';
        $action = $match[2] === 'offline' ? 'unpublish' : '';
    }
    return ['resource' => $resource, 'method' => $method, 'action' => $action];
}
