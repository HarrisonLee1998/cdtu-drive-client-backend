package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.FileItem;

import java.util.List;

public interface FileItemMapper {
    int deleteByPrimaryKey(String id);

    int insert(FileItem record);

    FileItem selectByPrimaryKey(String id);

    List<FileItem> selectAll();

    int updateByPrimaryKey(FileItem record);

    boolean updateFileStatus(String id);
}