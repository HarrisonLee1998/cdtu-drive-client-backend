package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Menu;
import cn.edu.cdtu.drive.pojo.Role;

import java.util.List;

/**
 * @author HarrisonLee
 * @date 2020/5/21 18:34
 */
public interface RoleService {
    Role selectRoleById(int roleId);

    List<Menu>selectMenuByRole(int roleId);
}
