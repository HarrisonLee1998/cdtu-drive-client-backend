package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.SimpleUser;
import cn.edu.cdtu.drive.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(User record);

    User selectByPrimaryKey(String id);

    List<User> selectAll();

    int updateByPrimaryKey(User record);

    // ----------------------------

    int updateUSS(@Param("uId")String uId, @Param("size") Long size);

    boolean insertByBatch(List<User>list);

    List<User> selectByDept(@Param("ids") List<String>ids, @Param("limit") Integer limit,
                            @Param("type")Integer type);

    boolean setLimit(@Param("ids")List<String>ids, @Param("limit") Integer limit);

    boolean partialUpdate(User user);

    // 批量插入
    default void save(List<SimpleUser> list){
        List<User>users = new ArrayList<>();
        for (SimpleUser simpleUser : list) {
            users.add(simpleUser.toUser());
        }
        if(users.size() > 0) {
            this.insertByBatch(users);
        }
    }
}