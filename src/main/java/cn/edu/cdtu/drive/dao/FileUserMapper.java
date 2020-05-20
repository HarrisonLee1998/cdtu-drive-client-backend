package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.FileUser;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FileUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(FileUser record);

    FileUser selectByPrimaryKey(String id);

    List<FileUser> selectAll();

    int updateByPrimaryKey(FileUser record);

    //-------------------

    FileUser checkPermission(String uId, String fId);

    FileUser selectFileByPath(@Param("uId") String uId, @Param("path") String path);

    List<FileUser>selectFilesByPId(String id);

    boolean updateDate(@Param("id") String id, @Param("lastUpdateDate") LocalDateTime localDateTime);

    List<FileUser> selectAllFolder(String uId);
}