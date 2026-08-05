<?php
if (!defined('IN_DISCUZ') || !defined('IN_ADMINCP')) { exit('Access Denied'); }

require_once dirname(__DIR__) . '/stk_auth/admin/AdminSupport.php';
require_once dirname(__DIR__) . '/stk_auth/service/ApiResponse.php';
require_once dirname(__DIR__) . '/stk_auth/service/MobileCrypto.php';

$page = (string)($_GET['page'] ?? 'overview');
$pluginId = (int)$pluginid;
$pages = [
    'overview' => ['ADM-PROJ-001', ['project_operator']],
    'projects' => ['ADM-PROJ-002', ['project_operator']],
    'categories' => ['ADM-PROJ-003', ['project_operator']],
    'home' => ['ADM-PROJ-004', ['project_operator']],
    'detail' => ['ADM-PROJ-005', ['project_operator']],
];
if (!isset($pages[$page])) $page = 'overview';
StkAdminSupport::requireRoles($pages[$page][1]);

function stk_project_input(string $key, $default = '') {
    $post = $_GET + $_POST;
    return $post[$key] ?? $default;
}

function stk_project_settings_input(): array {
    $value = $_GET['settings'] ?? $_POST['settings'] ?? [];
    return is_array($value) ? $value : [];
}

function stk_project_rows(string $sql, array $arguments, callable $mapper): array {
    try { return array_map($mapper, DB::fetch_all($sql, $arguments)); }
    catch (Throwable $error) { return []; }
}

function stk_project_render_config(string $page, int $pluginId, string $title, array $fields, array $rules): void {
    if (StkAdminSupport::submitted('stk_submit')) {
        try {
            $before = [];
            foreach (array_keys($rules) as $key) $before[$key] = StkAdminSupport::setting('stk_project', $key);
            $saved = StkAdminSupport::saveSettings('stk_project', stk_project_settings_input(), $rules);
            StkAdminSupport::audit('stk_project','save_'.$page,'plugin_config',$page,$before,$saved);
            cpmsg('plugins_setting_succeed', StkAdminSupport::pageUrl('stk_project',$pluginId,$page), 'succeed');
        } catch (Throwable $error) {
            echo '<div class="infobox"><h4 class="infotitle3">保存失败</h4><p>'.StkAdminSupport::esc($error->getMessage()).'</p></div>';
        }
    }
    StkAdminSupport::formStart('stk_project',$pluginId,$page);
    showtableheader($title);
    foreach ($fields as $key => $definition) {
        StkAdminSupport::field($key,$definition[0],StkAdminSupport::setting('stk_project',$key,$definition[1]),$definition[2] ?? 'text',$definition[3] ?? '');
    }
    showtablefooter();
    StkAdminSupport::formEnd();
}

try { StkAdminSupport::audit('stk_project','view_'.$page,'admin_page',$pages[$page][0]); } catch (Throwable $ignored) {}

if ($page === 'overview') {
    $rows = [];
    foreach (['pending'=>'待审核','published'=>'已发布','offline'=>'已下架'] as $status => $label) {
        try { $count = DB::fetch_first('SELECT COUNT(*) total FROM '.DB::table('stk_project').' WHERE status=%s AND deleted_at IS NULL',[$status]); $rows[] = [$label,(int)($count['total'] ?? 0)]; }
        catch (Throwable $error) { $rows[] = [$label,'不可用']; }
    }
    try { $views = DB::fetch_first('SELECT COALESCE(SUM(view_count),0) total FROM '.DB::table('stk_project')); $rows[] = ['累计浏览',(int)($views['total'] ?? 0)]; }
    catch (Throwable $error) { $rows[] = ['累计浏览','不可用']; }
    StkAdminSupport::table('ADM-PROJ-001 项目运营概览',['指标','数量'],$rows);
} elseif ($page === 'projects') {
    if (StkAdminSupport::submitted('project_action_submit')) {
        $action = (string)stk_project_input('project_action');
        $projectId = (int)stk_project_input('project_id');
        $before = $projectId > 0 ? DB::fetch_first('SELECT * FROM '.DB::table('stk_project').' WHERE project_id=%d LIMIT 1',[$projectId]) : null;
        try {
            if ($action === 'create_test') {
                global $_G;
                $title = trim((string)stk_project_input('title'));
                $summary = trim((string)stk_project_input('summary'));
                $contact = trim((string)stk_project_input('contact'));
                $categoryId = max(0,(int)stk_project_input('category_id'));
                if ($title === '' || $summary === '' || $contact === '') throw new InvalidArgumentException('测试项目标题、简介和联系方式不能为空');
                $now = gmdate('Y-m-d H:i:s');
                DB::insert('stk_project',[
                    'uid'=>(int)($_G['uid'] ?? 0),'category_id'=>$categoryId ?: null,'title'=>'[测试] '.mb_substr($title,0,70),
                    'summary'=>mb_substr($summary,0,2000),'contact_type'=>'phone','contact_ciphertext'=>StkMobileCrypto::encrypt($contact),
                    'contact_name'=>'测试联系人','status'=>'published','recommended'=>0,'recommendation_weight'=>0,'row_version'=>1,
                    'view_count'=>0,'published_at'=>$now,'created_at'=>$now,'updated_at'=>$now,
                ]);
                $projectId = (int)DB::insert_id();
                StkAdminSupport::audit('stk_project','create_test_project','project',(string)$projectId,null,['title'=>$title,'status'=>'published']);
            } elseif ($before) {
                $updates = ['updated_at'=>gmdate('Y-m-d H:i:s'),'row_version'=>(int)$before['row_version']+1];
                if ($action === 'recommend') { $updates += ['recommended'=>1,'recommendation_weight'=>100]; }
                elseif ($action === 'unrecommend') { $updates += ['recommended'=>0,'recommendation_weight'=>0]; }
                elseif ($action === 'publish') { $updates += ['status'=>'published','published_at'=>gmdate('Y-m-d H:i:s'),'offline_at'=>null]; }
                elseif ($action === 'offline') { $updates += ['status'=>'offline','offline_at'=>gmdate('Y-m-d H:i:s')]; }
                elseif ($action === 'delete') { $updates += ['deleted_at'=>gmdate('Y-m-d H:i:s')]; }
                else throw new InvalidArgumentException('不支持的项目操作');
                DB::update('stk_project',$updates,['project_id'=>$projectId]);
                StkAdminSupport::audit('stk_project',$action.'_project','project',(string)$projectId,$before,$updates);
            } else throw new InvalidArgumentException('项目不存在');
        } catch (Throwable $error) {
            echo '<div class="infobox"><h4 class="infotitle3">操作失败</h4><p>'.StkAdminSupport::esc($error->getMessage()).'</p></div>';
        }
    }
    StkAdminSupport::formStart('stk_project',$pluginId,$page);
    echo '<input type="hidden" name="project_action" value="create_test"><table class="tb tb2">';
    echo '<tr><th>测试项目标题</th><td><input class="txt" name="title" maxlength="70"></td></tr>';
    echo '<tr><th>项目简介</th><td><textarea class="tarea" name="summary" rows="4" cols="80"></textarea></td></tr>';
    echo '<tr><th>联系电话</th><td><input class="txt" name="contact" maxlength="32"></td></tr>';
    echo '<tr><th>分类 ID</th><td><input class="txt" type="number" min="0" name="category_id" value="0"></td></tr></table>';
    StkAdminSupport::formEnd('project_action_submit','创建明确标记的测试项目');
    StkAdminSupport::formStart('stk_project',$pluginId,$page);
    echo '<table class="tb tb2"><tr><th>项目 ID</th><td><input class="txt" type="number" min="1" name="project_id"></td></tr>';
    echo '<tr><th>操作</th><td><select name="project_action"><option value="recommend">推荐</option><option value="unrecommend">取消推荐</option><option value="publish">发布</option><option value="offline">下架</option><option value="delete">软删除</option></select></td></tr></table>';
    StkAdminSupport::formEnd('project_action_submit','执行项目操作');
    $rows = stk_project_rows('SELECT project_id,uid,category_id,title,status,recommended,view_count,published_at,updated_at FROM '.DB::table('stk_project').' WHERE deleted_at IS NULL ORDER BY project_id DESC LIMIT 100',[],fn($r)=>[$r['project_id'],$r['uid'],$r['category_id'] ?? '',$r['title'],$r['status'],$r['recommended'],$r['view_count'],$r['published_at'] ?? '',$r['updated_at']]);
    StkAdminSupport::table('ADM-PROJ-002 项目管理',['ID','UID','分类','标题','状态','推荐','浏览','发布时间','更新时间'],$rows);
} elseif ($page === 'categories') {
    if (StkAdminSupport::submitted('category_submit')) {
        try {
            $categoryId = (int)stk_project_input('category_id');
            $name = trim((string)stk_project_input('category_name'));
            $sort = (int)stk_project_input('sort_order');
            $enabled = (int)stk_project_input('enabled') === 1 ? 1 : 0;
            if ($name === '' || mb_strlen($name) > 48) throw new InvalidArgumentException('分类名称长度不正确');
            $now = gmdate('Y-m-d H:i:s');
            if ($categoryId > 0) {
                $before = DB::fetch_first('SELECT * FROM '.DB::table('stk_project_category').' WHERE category_id=%d LIMIT 1',[$categoryId]);
                if (!$before) throw new InvalidArgumentException('分类不存在');
                $updates = ['name'=>$name,'sort_order'=>$sort,'enabled'=>$enabled,'updated_at'=>$now];
                DB::update('stk_project_category',$updates,['category_id'=>$categoryId]);
                StkAdminSupport::audit('stk_project','update_category','project_category',(string)$categoryId,$before,$updates);
            } else {
                DB::insert('stk_project_category',['name'=>$name,'icon_asset'=>null,'sort_order'=>$sort,'enabled'=>$enabled,'deleted_at'=>null,'created_at'=>$now,'updated_at'=>$now]);
                $categoryId=(int)DB::insert_id();
                StkAdminSupport::audit('stk_project','create_category','project_category',(string)$categoryId,null,['name'=>$name,'sort_order'=>$sort,'enabled'=>$enabled]);
            }
        } catch (Throwable $error) { echo '<div class="infobox"><h4 class="infotitle3">保存失败</h4><p>'.StkAdminSupport::esc($error->getMessage()).'</p></div>'; }
    }
    StkAdminSupport::formStart('stk_project',$pluginId,$page);
    echo '<table class="tb tb2"><tr><th>分类 ID</th><td><input class="txt" type="number" min="0" name="category_id" value="0"> 0 表示新增</td></tr>';
    echo '<tr><th>分类名称</th><td><input class="txt" name="category_name" maxlength="48"></td></tr><tr><th>排序</th><td><input class="txt" type="number" name="sort_order" value="0"></td></tr>';
    echo '<tr><th>启用</th><td><select name="enabled"><option value="1">启用</option><option value="0">停用</option></select></td></tr></table>';
    StkAdminSupport::formEnd('category_submit','保存分类');
    $rows = stk_project_rows('SELECT category_id,name,sort_order,enabled,created_at,updated_at FROM '.DB::table('stk_project_category').' WHERE deleted_at IS NULL ORDER BY sort_order,category_id',[],fn($r)=>[$r['category_id'],$r['name'],$r['sort_order'],$r['enabled']?'启用':'停用',$r['created_at'],$r['updated_at']]);
    StkAdminSupport::table('ADM-PROJ-003 项目分类',['ID','名称','排序','状态','创建','更新'],$rows);
} elseif ($page === 'home') {
    stk_project_render_config($page,$pluginId,'ADM-PROJ-004 首页展示设置',[
        'home_page_size'=>['CFG-HOME-001 每页数量','10','number'], 'home_default_sort'=>['CFG-HOME-002 默认排序','latest'],
        'home_recommended_first'=>['CFG-HOME-003 推荐优先','1'], 'home_show_category'=>['CFG-HOME-004 显示分类','1'],
        'home_show_publisher'=>['CFG-HOME-005 显示发布者','1'], 'home_show_member_badge'=>['CFG-HOME-006 显示会员标识','1'],
        'home_show_view_count'=>['CFG-HOME-007 显示浏览量','1'], 'home_empty_copy'=>['CFG-HOME-008 空状态文案','暂时还没有可展示的项目'],
    ],[
        'home_page_size'=>['type'=>'int','min'=>5,'max'=>30], 'home_default_sort'=>['type'=>'enum','values'=>['latest','recommended']],
        'home_recommended_first'=>['type'=>'bool'], 'home_show_category'=>['type'=>'bool'], 'home_show_publisher'=>['type'=>'bool'],
        'home_show_member_badge'=>['type'=>'bool'], 'home_show_view_count'=>['type'=>'bool'], 'home_empty_copy'=>['type'=>'string','min'=>1,'max'=>80],
    ]);
} elseif ($page === 'detail') {
    stk_project_render_config($page,$pluginId,'ADM-PROJ-005 项目详情设置',[
        'detail_contact_login_required'=>['CFG-DETAIL-001 联系方式需登录','1'],
        'detail_view_dedupe_seconds'=>['CFG-DETAIL-002 浏览去重秒数','1800','number'],
        'detail_recommendations_enabled'=>['CFG-DETAIL-003 相关推荐','1'],
    ],[
        'detail_contact_login_required'=>['type'=>'enum','values'=>['1']],
        'detail_view_dedupe_seconds'=>['type'=>'int','min'=>60,'max'=>86400],
        'detail_recommendations_enabled'=>['type'=>'bool'],
    ]);
}
