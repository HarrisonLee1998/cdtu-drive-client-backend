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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    private List<Role>roles;

    private List<Menu>menus;

    @Override
    public Role selectRoleById(int roleId) {
        return roleMapper.selectByPrimaryKey(roleId);
    }

    @Override
    public List<Role> selectAll() {
        fillRole();
        // 缓存
        return roles;
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
}
