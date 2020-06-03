package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Chunk;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    Result move(String uId, String src, String desc);
    Result copy(String uId, String src, String desc);
    List<FileUser> selectFileForRecycleBin(String uId);
    Result handleRecycle(String uId, List<String>ids, Integer flag);
    Boolean delete(String uId, List<String>ids);

    // Share Space
    FileUser selectFileByPathForShare(String shareId, String path);

    byte[] getAvatar(String uId, String gId);
    Boolean saveAvatar(String uId,String gId, MultipartFile file) throws IOException;

    List<Map<String, Object>> selectSizeByType();

    List<Map<String, Object>> selectSizeByDept();
}
