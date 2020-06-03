package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.GroupMapper;
import cn.edu.cdtu.drive.dao.GroupUserMapper;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.Group;
import cn.edu.cdtu.drive.pojo.GroupUser;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.GroupService;
import cn.edu.cdtu.drive.util.UUIDHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,timeout=36000,rollbackFor=Exception.class)
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupUserMapper groupUserMapper;

    @Autowired
    private FileUserMapper fileUserMapper;

    @Override
    public Group newGroup(Group group, String uId) {
        group.setId(UUIDHelper.rand(12));
        var groupUser = new GroupUser();
        groupUser.setgId(group.getId());
        groupUser.setuId(uId);
        groupUser.setStatus(1);
        groupUser.setGuType(0);
        groupUser.setJoinDate(LocalDateTime.now());
        groupMapper.insert(group);
        groupUserMapper.insert(groupUser);

        var fileUser = new FileUser();
        fileUser.setId(DigestUtils.md5DigestAsHex((group.getId() + "/").getBytes()));
        fileUser.setGId(DigestUtils.md5DigestAsHex((group.getId() + "/").getBytes()));
        fileUser.setFName("/");
        fileUser.setFPath("/");
        fileUser.setUId(uId);
        fileUser.setIsFolder(1);
        fileUser.setGId(group.getId());

        fileUserMapper.insert(fileUser);
        return group;
    }

    @Override
    public boolean updateGroup(Group group, String uId) {
        return false;
    }

    @Override
    public List<Group> selectGroupsForUser(String uId) {
        return groupMapper.selectAllForUser(uId);
    }

    @Override
    public Group selectGroupById(String id) {
        return groupMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<User> selectGroupUser(String gId) {
        return null;
    }

    @Override
    public boolean updateGroupUser(String gId, String uId, Integer flag) {
        return false;
    }

    @Override
    public GroupUser selectGroupUser(String gId, String uId) {
        return groupUserMapper.selectGroupUser(gId, uId);
    }
}
