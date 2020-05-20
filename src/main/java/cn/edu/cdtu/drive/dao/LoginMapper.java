package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Login;
import java.util.List;

public interface LoginMapper {
    int insert(Login record);

    List<Login> selectAll();
}