package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(String id);

    int insert(User record);

    User selectByPrimaryKey(String id);

    List<User> selectAll();

    int updateByPrimaryKey(User record);

    // ----------------------------

    int updateUSS(@Param("uId")String uId, @Param("size") Long size);

    // 批量插入
    default void save(List<User> list){
        System.out.println("=================");
        for (User user : list) {
            System.out.println(user);
        }
    }
}