package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Group;
import cn.edu.cdtu.drive.pojo.GroupUser;
import cn.edu.cdtu.drive.pojo.User;

import java.util.List;

public interface GroupService {

    Group newGroup(Group group, String uId);
    boolean updateGroup(Group group, String uId);
    List<Group> selectGroupsForUser(String uId);
    Group selectGroupById(String id);
    List<User>selectGroupUser(String gId);
    boolean updateGroupUser(String gId, String uId, Integer flag);

    GroupUser selectGroupUser(String gId, String uId);
}
