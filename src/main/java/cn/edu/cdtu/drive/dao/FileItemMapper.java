package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.FileItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FileItemMapper {
    int deleteByPrimaryKey(String id);

    int insert(FileItem record);

    FileItem selectByPrimaryKey(String id);

    List<FileItem> selectAll();

    int updateByPrimaryKey(FileItem record);

    boolean updateFileStatus(String id);

    List<Map<String,Object>>selectSizeByType();

    Long selectSizeByDept(@Param("ids") List<String>ids);
}