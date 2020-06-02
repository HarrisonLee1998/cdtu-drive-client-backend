package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper {
    boolean deleteByPrimaryKey(Integer id);

    boolean insert(Role record);

    Role selectByPrimaryKey(Integer id);

    List<Role> selectAll();

    int updateByPrimaryKey(Role record);

    List<Role> selectAllWithPerm();

    boolean saveRolePerm(@Param("roleId") Integer roleId,@Param("permIds")  List<Integer>permIds);
    boolean deleteRolePerm(@Param("roleId") Integer roleId, @Param("permIds") List<Integer>permIds);
}