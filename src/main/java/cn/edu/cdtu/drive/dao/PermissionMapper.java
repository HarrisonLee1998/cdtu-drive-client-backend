package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Permission;
import java.util.List;

public interface PermissionMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Permission record);

    Permission selectByPrimaryKey(Integer id);

    List<Permission> selectAll();

    int updateByPrimaryKey(Permission record);

    List<Permission> selectPermissionByRole(Integer roleId);
}