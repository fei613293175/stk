<?php
define('IN_DISCUZ', true);
$api=file_get_contents(__DIR__.'/../source/plugin/stk_project/api.inc.php');
$routes=file_get_contents(__DIR__.'/../source/plugin/stk_project/lib/routes.php');
$install=file_get_contents(__DIR__.'/../source/plugin/stk_project/install.php');
$config=file_get_contents(__DIR__.'/../source/plugin/stk_project/lib/config.php');
$admin=file_get_contents(__DIR__.'/../source/plugin/stk_project/admin/projects.inc.php');
$review=file_get_contents(__DIR__.'/../source/plugin/stk_project/admin/review.inc.php');
$accountApi=file_get_contents(__DIR__.'/../source/plugin/stk_auth/api.inc.php');
$accountInstall=file_get_contents(__DIR__.'/../source/plugin/stk_auth/install.php');
$upgrade=file_get_contents(__DIR__.'/../source/plugin/stk_project/upgrade.php');
$upload=file_get_contents(__DIR__.'/../source/plugin/stk_project/lib/upload.php');
$manifest=file_get_contents(__DIR__.'/../source/plugin/stk_project/discuz_plugin_stk_project.json');
foreach(['categories','projects','next_cursor','contacts','images','update|resubmit'] as $needle){ if(strpos($api,$needle)===false){fwrite(STDERR,"FAIL: missing {$needle}\n");exit(1);} }
foreach(['project/categories','me/projects','releases/current','health/live','health/ready','media/project-images','uploads/project-images','me/profile','me/member','me/wallets','props/display','support'] as $needle){ if(strpos($routes.$api,$needle)===false){fwrite(STDERR,"FAIL: missing canonical project contract {$needle}\n");exit(1);} }
foreach(['row_version','4091','daily_limit','pending_limit','contact_name','release/current'] as $needle){ if(strpos($api.$install.$upgrade,$needle)===false){fwrite(STDERR,"FAIL: missing publish/release contract {$needle}\n");exit(1);} }
if(strpos($upgrade, 'BINARY u.storage_key=BINARY i.storage_key')===false){fwrite(STDERR,"FAIL: legacy upload backfill must be collation-safe\n");exit(1);}
foreach(['只有已发布项目可以下架','allowed_external_hosts',"'common_member'","'stk_member_status'"] as $needle){ if(strpos($api,$needle)===false){fwrite(STDERR,"FAIL: missing project state/display contract {$needle}\n");exit(1);} }
foreach(['owner_uid','asset_id','temporary','bound','expires_at','stk_project_cleanup_uploads','realpath'] as $needle){ if(strpos($upload.$api.$install.$upgrade,$needle)===false){fwrite(STDERR,"FAIL: missing upload ownership contract {$needle}\n");exit(1);} }
foreach(['publish_settings','image_settings','audit_logs','user_projects','release_settings','maintenance_settings','deployment_diagnostics','security_audit'] as $needle){ if(strpos($manifest,$needle)===false){fwrite(STDERR,"FAIL: missing project admin module {$needle}\n");exit(1);} }
foreach(['INSERT IGNORE','viewerHash','recorded','REQUEST_METHOD'] as $needle){ if(strpos($api,$needle)===false){fwrite(STDERR,"FAIL: missing view contract {$needle}\n");exit(1);} }
foreach(['HTTP_AUTHORIZATION','dev-access-','stk_auth_token'] as $needle){ if(strpos($config,$needle)===false){fwrite(STDERR,"FAIL: missing auth contract {$needle}\n");exit(1);} }
foreach(['submitcheck','projectsubmit','published','contacts_json'] as $needle){ if(strpos($admin,$needle)===false){fwrite(STDERR,"FAIL: missing admin create contract {$needle}\n");exit(1);} }
foreach(['reviewsubmit','approve','reject'] as $needle){ if(strpos($review,$needle)===false){fwrite(STDERR,"FAIL: missing review contract {$needle}\n");exit(1);} }
if(strpos($review,'只有待审核项目可以执行审核')===false){fwrite(STDERR,"FAIL: review must be restricted to pending projects\n");exit(1);}
foreach(['account_overview','stk_account_membership','stk_account_balance','stk_account_prop'] as $needle){ if(strpos($accountApi.$accountInstall,$needle)===false){fwrite(STDERR,"FAIL: missing account contract {$needle}\n");exit(1);} }
if(strpos($install,'pre_stk_project_audit_log')===false){fwrite(STDERR,"FAIL: missing canonical audit table\n");exit(1);}
foreach(['pre_stk_project_category','pre_stk_project','pre_stk_project_image','pre_stk_project_view_daily'] as $needle){ if(strpos($install,$needle)===false){fwrite(STDERR,"FAIL: missing {$needle}\n");exit(1);} }
$response=file_get_contents(__DIR__.'/../source/plugin/stk_project/lib/response.php');
foreach(['pre_stk_app_config','pre_stk_project_request_guard','response_json','server_time','owner_actions','status_counts','static_base_url','publisher_avatar_url','stk_project_write_guard','HTTP_X_STK_REQUEST_ID'] as $needle){ if(strpos($install.$upgrade.$upload.$api.$config.$response,$needle)===false){fwrite(STDERR,"FAIL: missing project configuration/security behavior {$needle}\n");exit(1);} }
if (strpos($api, "\$status !== 'published' && stk_project_config('user_can_delete', '1') === '1'") === false) { fwrite(STDERR, "FAIL: published owner actions must not expose delete\n"); exit(1); }
if (strpos($api, "if (\$action === 'delete' && \$from === 'published')") === false) { fwrite(STDERR, "FAIL: published projects must reject direct delete requests\n"); exit(1); }
echo "PASS: project_contract_test\n";
