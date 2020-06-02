package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.MenuMapper;
import cn.edu.cdtu.drive.dao.PermissionMapper;
import cn.edu.cdtu.drive.dao.RoleMapper;
import cn.edu.cdtu.drive.pojo.Menu;
import cn.edu.cdtu.drive.pojo.Permission;
import cn.edu.cdtu.drive.pojo.Role;
import cn.edu.cdtu.drive.service.RoleService;
import cn.edu.cdtu.drive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HarrisonLee
 * @date 2020/5/21 18:34
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserService userService;

    private static List<Role>roles;

    private static List<Menu>menus;

    private static List<Permission> permissions;

    @Override
    public Role selectRoleById(int roleId) {
        fillRole();
        for (Role role : roles) {
            if(Objects.equals(roleId, role.getId())) {
                return role;
            }
        }
        return null;
    }

    @Override
    public List<Role> selectAll() {
        fillRole();
        // 缓存
        return roles;
    }

    public List<Permission>selectAllPerm() {
        fillPerm();
        return permissions;
    }

    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,timeout=36000,
            rollbackFor=Exception.class)
    @Override
    public boolean insertOrUpdate(Role role) {
        // 初步筛选
        if(Objects.isNull(role) || Objects.isNull(role.getTitle()) || role.getTitle().isBlank() ||
                Objects.isNull(role.getPermissions())||role.getPermissions().size() == 0) {
            return false;
        }
        fillRole();
        // 判断title是否已存在
        if(roles.stream().map(Role::getTitle).collect(Collectors.toList()).contains(role.getTitle())) {
            // 更新
            var b = update(role);
            roles = null;
            return b;
        } else {
            role.setCreateDate(LocalDateTime.now());
            role.setLastUpdateDate(LocalDateTime.now());
        }

        fillPerm();
        // 判断permissions是否合法
        role.setPermissions(role.getPermissions().stream().filter(permission ->
                permissions.stream().anyMatch(p -> Objects.equals(p.getId(), permission.getId())))
                .collect(Collectors.toList()));
        if(role.getPermissions().size() == 0) {
            return false;
        }
        role.setCreateDate(LocalDateTime.now());
        var b = roleMapper.insert(role);
        var list = role.getPermissions().stream().map(Permission::getId).collect(Collectors.toList());
        b = roleMapper.saveRolePerm(role.getId(), list);
        roles = null;
        return b;
    }

    @Override
    public boolean deleteById(Integer id) {
        roles = null;
        return roleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Menu> selectMenuByRole(int roleId) {
        // 先根据角色查询权限 role-permission-menu
        // 再根据权限查询菜单
        final List<Permission> list = permissionMapper.selectPermissionByRole(roleId);
        List<Integer>ids = list.stream().map(Permission::getId).collect(Collectors.toList());
        final List<Menu> menus = menuMapper.selectByPermission(ids);
        return menus;
    }

    @Override
    public Boolean checkAdminPermission(HttpServletRequest request, String title) {
        var user = userService.getUserFromToken(request);
        if(Objects.isNull(user.getRoleId())) {
            return false;
        }
        fillRole();
        Role role = null;
        for (Role r : roles) {
            if(Objects.equals(r.getId(),user.getRoleId())) {
                role = r;
                break;
            }
        }
        if(Objects.nonNull(role)) {
            for (Permission permission : role.getPermissions()) {
                if(Objects.equals(permission.getTitle(), title)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean checkAdminMenu(HttpServletRequest request, String path) {
        var user = userService.getUserFromToken(request);
        if(Objects.isNull(user.getRoleId())) {
            return false;
        }
        Role role = null;
        fillRole();
        for (Role r : roles) {
            if(Objects.equals(r.getId(), user.getRoleId())) {
                role = r;
                break;
            }
        }
        fillMenu();
        List<String>mp = new ArrayList<>();
        if(Objects.nonNull(role)) {
            for (Permission permission : role.getPermissions()) {
                menus.forEach(menu -> {
                    if(Objects.equals(permission.getMenuId(), menu.getId())) {
                        mp.add(menu.getLink());
                    }
                });
            }
        }
        return mp.contains(path);
    }

    private boolean update(Role role) {
        var set1 = role.getPermissions().stream().map(Permission::getId).collect(Collectors.toSet());
        var set2 = Objects.requireNonNull(selectRoleByTitle(role.getTitle())).getPermissions().stream().map(Permission::getId)
                .collect(Collectors.toSet());
         // 求差集
        // 交集
        var set3 = new HashSet<>(set1);
        var set4 = new HashSet<>(set2);
        set3.removeAll(set2);
        set4.removeAll(set1);

        role = selectRoleByTitle(role.getTitle());
        role.setLastUpdateDate(LocalDateTime.now());
        roleMapper.updateByPrimaryKey(role);

        if(set3.size() > 0) {
            // 新增
            roleMapper.saveRolePerm(role.getId(), new ArrayList<>(set3));
        }
        if(set4.size() > 0) {
            // 删除
            roleMapper.deleteRolePerm(role.getId(), new ArrayList<>(set4));
        }
        return true;
    }
    private Role selectRoleByTitle(String title) {
        fillRole();
        for (Role role : roles) {
            if(Objects.equals(title, role.getTitle())) {
                return role;
            }
        }
        return null;
    }

    private void fillRole() {
        if(Objects.isNull(roles)) {
            roles = roleMapper.selectAllWithPerm();
        }
    }

    private void fillMenu() {
        if(Objects.isNull(menus)) {
            menus = menuMapper.selectAll();
        }
    }

    private void fillPerm() {
        if(Objects.isNull(permissions)) {
            permissions = permissionMapper.selectAll();
        }
    }
}
