package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Group;
import java.util.List;

public interface GroupMapper {
    int deleteByPrimaryKey(String id);

    int insert(Group record);

    Group selectByPrimaryKey(String id);

    List<Group> selectAll();

    int updateByPrimaryKey(Group record);

    List<Group> selectAllForUser(String uId);
}