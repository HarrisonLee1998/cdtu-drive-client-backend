package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Chunk;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.util.Node;

import java.util.List;

/**
 * @author HarrisonLee
 * @date 2020/5/17 20:34
 */
public interface FileService {
    boolean uploadChunk(String uId, Chunk chunk);
    List<Integer>checkChunk(String uId, Chunk chunk);
    boolean merge(String uId, Chunk chunk);
    boolean checkPermission(String uId, String fId);
    FileUser selectFileByPath(String uId, String path);
    boolean saveFileUser(String uId,Chunk chunk);
    boolean newFileUser(FileUser fileUser);
    Node selectFolderTree(String uId);

    Boolean rename(String uId, String id, String name);
}
