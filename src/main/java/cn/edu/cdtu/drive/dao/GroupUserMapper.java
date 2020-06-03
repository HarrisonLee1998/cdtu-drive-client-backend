package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.GroupUser;
import cn.edu.cdtu.drive.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupUserMapper {
    int deleteByPrimaryKey(@Param("gId") String gId, @Param("uId") String uId);

    int insert(GroupUser record);

    GroupUser selectByPrimaryKey(@Param("gId") String gId, @Param("uId") String uId);

    List<GroupUser> selectAll();

    int updateByPrimaryKey(GroupUser record);

    GroupUser selectGroupUser(@Param("gId") String gId, @Param("uId") String uId);

    List<User> selectGroupUsers(@Param("gId") String gId, @Param("status") Integer status);
}