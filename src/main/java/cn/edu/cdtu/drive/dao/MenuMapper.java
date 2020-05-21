package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Menu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MenuMapper {
    int deleteByPrimaryKey(String link);

    int insert(Menu record);

    Menu selectByPrimaryKey(String link);

    List<Menu> selectAll();

    int updateByPrimaryKey(Menu record);

    List<Menu>selectByPermission(@Param("ids") List<Integer>ids);

    List<Menu>selectByParentId(Integer pmId);
}