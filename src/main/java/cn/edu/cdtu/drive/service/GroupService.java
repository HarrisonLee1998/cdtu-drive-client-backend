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
    List<User>selectGroupUsers(String gId, Integer status);
    boolean updateGroupUser(String gId, String uId, Integer flag);

    GroupUser selectGroupUser(String gId, String uId);

    Boolean deleteGroup(String gId, String uId);

    Boolean joinGroup(String gId, String uId);
}
