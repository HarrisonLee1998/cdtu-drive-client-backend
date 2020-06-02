package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Menu;
import cn.edu.cdtu.drive.pojo.Role;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author HarrisonLee
 * @date 2020/5/21 18:34
 */
public interface RoleService {
    Role selectRoleById(int roleId);

    List<Role> selectAll();

    List<Menu>selectMenuByRole(int roleId);

    Boolean checkAdminPermission(HttpServletRequest request, String title);
    Boolean checkAdminMenu(HttpServletRequest request, String path);
}
