package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.MenuMapper;
import cn.edu.cdtu.drive.dao.PermissionMapper;
import cn.edu.cdtu.drive.dao.RoleMapper;
import cn.edu.cdtu.drive.pojo.Menu;
import cn.edu.cdtu.drive.pojo.Permission;
import cn.edu.cdtu.drive.pojo.Role;
import cn.edu.cdtu.drive.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public Role selectRoleById(int roleId) {
        return roleMapper.selectByPrimaryKey(roleId);
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
}
