package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileItemMapper;
import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.UserMapper;
import cn.edu.cdtu.drive.pojo.Chunk;
import cn.edu.cdtu.drive.pojo.FileItem;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.service.FileService;
import cn.edu.cdtu.drive.util.FileUtils;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.UUIDHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HarrisonLee
 * @date 2020/5/17 20:34
 */
@Service
public class FileServiceImpl implements FileService {

    private static String uploadFolder = "E:\\Temp\\files";

    @Autowired
    private FileItemMapper fileItemMapper;

    @Autowired
    private FileUserMapper fileUserMapper;

    @Autowired
    private UserMapper userMapper;

    private static String TODAY_COMMON_PATH = todayCommonPath();

    /**
     * 上传分块
     * @param chunk 所要上传的分块
     * @return
     */
    @Override
    public boolean uploadChunk(String uId, Chunk chunk) {
        MultipartFile file = chunk.getFile();
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(FileUtils.generatePath(TODAY_COMMON_PATH, chunk));
            //文件写入指定路径
             Files.write(path, bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 先根据Chunk获取到文件的ID
     * 然后取查询，判断是否存在，如果存在且状态为完成上传，那么直接返回
     * 如果不存在，也直接返回
     * 如果存在且每上传完，那么去磁盘上获取分块信息
     *
     * @param uId 因为文件可能不存在，所以需要新建，同时需要判断文件夹是否已存在，所以需要用户ID
     * @param chunk
     * @return
     */
    @Override
    public List<Integer> checkChunk(String uId, Chunk chunk) {
        // 文件Id
        final String id = chunk.getIdentifier();
        FileItem fileItem = fileItemMapper.selectByPrimaryKey(id);
        if (Objects.isNull(fileItem)) {
            // 新建文件
            // 如果不存在，那么保存新记录到数据库中
            final boolean b = saveFileItem(uId, chunk);
            if(!b) {
                throw new RuntimeException("文件不存在，但文件创建失败");
            }
            return null;
        } else if (fileItem.getStatus() == 1) {
            return new ArrayList<>();
        } else {
            return getUnUploadedChunkNumber(fileItem);
        }
    }

    @Override
    public boolean merge(String uId, Chunk chunk) {
        System.out.println(chunk);
        // 合并后的文件夹（上一级）路径
        String folder = Paths.get(TODAY_COMMON_PATH , chunk.getIdentifier()).toString();

        // 合并后的文件名
        String targetFile = Paths.get(folder, chunk.getIdentifier()).toString();

        boolean b = FileUtils.merge(targetFile, folder, chunk.getIdentifier());
        if(!b) {
            return false;
        }
        // 更新数据库，设置文件状态为完成上传
        b = fileItemMapper.updateFileStatus(chunk.getIdentifier());
        if(!b) {
            return false;
        }
        // 保存文件-用户关系
        b = saveFileUser(uId, chunk);
        return b;
    }


    private List<Integer> getUnUploadedChunkNumber(FileItem fileItem) {
        File f = new File(fileItem.getPath());
        try {
            final String[] list = f.list();
            if(Objects.isNull(list) || list.length == 0) {
                return null;
            }
            List<Integer>numbers = new ArrayList<>();
            for(String s : list) {
                int i = s.lastIndexOf("-");
                final Integer l = Integer.parseInt(s.substring(i + 1));
                numbers.add(l);
            }
            return numbers;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public boolean checkPermission(String uId, String fId) {
        final FileUser fileUser = fileUserMapper.checkPermission(uId, fId);
        if (Objects.isNull(fileUser)) {
            return false;
        }
        // 增加其他逻辑，比如在组空间中上传
        // 继续检查当前文件夹是否属于某个分组，然后当前用户是否具有上传权限
        return true;
    }

    @Override
    public FileUser selectFileByPath(String uId, String path) {
        return fileUserMapper.selectFileByPath(uId, path);
    }

    /**
     * 将分块转为文件
     *
     * @param chunk
     * @return
     */
    @Override
    public boolean saveFileUser(String uId,Chunk chunk) {

        final String parentFileId = chunk.getParentFileId();
        final FileUser parentFile = fileUserMapper.selectByPrimaryKey(parentFileId);
        if(Objects.isNull(parentFile)) {
            return false;
        }

        System.out.println(chunk);
        // 处理relative path
        String relativePath = chunk.getRelativePath();
        final String[] strings = relativePath.split("/");
        List<FileUser>folders = new ArrayList<>();
        // 处理目录
        if(strings.length > 0) {
            final List<String> list = Arrays.stream(strings).filter(s -> s.length() > 0).collect(Collectors.toList());
            // 依次创建每个文件（目录）- 用户关系
            StringBuilder pFolder = new StringBuilder(parentFile.getFPath());
            if(!Objects.equals("/", parentFile.getFName())) {
                pFolder.append("/");
            }
            for(var i = 0; i < list.size() - 1; ++i) {
                FileUser folder = new FileUser();
                /**
                 * 将当前目录的绝对路径 + uid得到hash
                 * 对于文件夹的处理比文件少了一个属性设置，就是关联的文件ID
                 */
                pFolder.append(list.get(i));
                folder.setId(DigestUtils.md5DigestAsHex((uId + pFolder.toString()).getBytes()));
                folder.setIsFolder(1);
                folder.setIsDelete(0);
                folder.setFName(list.get(i));
                folder.setUId(uId); // 设置关联者的ID
                folder.setLastUpdateDate(LocalDateTime.now()); // 设置关联日期
                folder.setFPath(pFolder.toString());
                folders.add(folder);
                pFolder.append("/");
            }
            // 检验每个文件夹是否存在，存在，则覆盖现有的，如果不存在，则插入
            for(var i = 0; i < folders.size(); ++i) {
                FileUser f = folders.get(i);
                synchronized (FileServiceImpl.class){
                    final FileUser f1 = fileUserMapper.selectByPrimaryKey(f.getId());
                    if (Objects.nonNull(f1)) {
                        fileUserMapper.updateDate(f1.getId(), LocalDateTime.now());
                        folders.set(i, f1);
                    } else {
                        if (i == 0) {
                            f.setFPid(chunk.getParentFileId());
                        } else {
                            f.setFPid(folders.get(i - 1).getId());
                        }
                        final int r = fileUserMapper.insert(f);
                        if (r < 0) {
                            return false;
                        }
                    }
                }
            }
        }
        // 最后一个一定是文件
        FileUser fileUser = new FileUser();
        fileUser.setId(UUIDHelper.rand(32));
        fileUser.setUId(uId);
        fileUser.setFId(chunk.getIdentifier());
        fileUser.setFName(chunk.getFilename());
        fileUser.setIsFolder(0); // 类型为文件
        fileUser.setFType(chunk.getType());
        fileUser.setIsDelete(0);
        fileUser.setFSize(chunk.getTotalSize());

        if(folders.size() == 0) {
            // 上传的是文件
            fileUser.setFPid(chunk.getParentFileId());
            if(Objects.equals("/", parentFile.getFName())) {
                // 如果父目录是根目录
                fileUser.setFPath("/"  + fileUser.getFName());
            } else {
                // 如果父目录不是根目录
                fileUser.setFPath(parentFile.getFPath()+ "/" + fileUser.getFName());
            }
        } else {
            // 上传的是文件夹
            fileUser.setFPid(folders.get(folders.size() - 1).getId());
            fileUser.setFPath(folders.get(folders.size() - 1).getFPath()+ "/" + fileUser.getFName());
        }
        fileUser.setLastUpdateDate(LocalDateTime.now());
        final int i = fileUserMapper.insert(fileUser);
        if(i <= 0) {
            return false;
        }
        // 更新用户空间
        return userMapper.updateUSS(uId, chunk.getTotalSize()) > 0;
    }

    @Override
    public boolean newFileUser(FileUser fileUser) {
        if(Objects.isNull(fileUser.getUId()) || Objects.isNull(fileUser.getFName()) ||
                fileUser.getFName().contains("/")) {
            return false;
        } else {
            final FileUser parentFile = fileUserMapper.selectByPrimaryKey(fileUser.getFPid());
            if(Objects.isNull(parentFile) || !Objects.equals(parentFile.getUId(), fileUser.getUId())) {
                return false;
            }
            if(parentFile.getFName().equals("/")) {
                fileUser.setFPath("/" + fileUser.getFName());
            } else {
                fileUser.setFPath(parentFile.getFPath() + "/" + fileUser.getFName());
            }
            fileUser.setLastUpdateDate(LocalDateTime.now());
            fileUser.setIsFolder(1);
            fileUser.setIsDelete(0);
            fileUser.setId(DigestUtils.md5DigestAsHex((fileUser.getUId() + fileUser.getFPath()).getBytes()));
            System.out.println(fileUser);
            // 在判断目录是否已经存在
            final FileUser fileUser1 = fileUserMapper.selectByPrimaryKey(fileUser.getId());
            if(Objects.nonNull(fileUser1)) {
                return false;
            }
            return fileUserMapper.insert(fileUser) > 0;
        }
    }

    @Override
    public Node selectFolderTree(String uId) {
        final List<FileUser>fileUsers = fileUserMapper.selectAllFolder(uId);
        FileUser rootFolder = fileUserMapper.selectFileByName(uId, "/");
        if(Objects.isNull(rootFolder)) {
            return null;
        } else {
            Node rootNode = new Node();
            rootNode.setId(rootFolder.getId());
            rootNode.setLabel(rootFolder.getFName());
            rootNode.setChildren(new ArrayList<>());
            buildTree(rootFolder, rootNode, fileUsers);
            //printTree(rootFolder, 1);
            return rootNode;
        }
    }

    /**
     * 深度优先遍历
     */
    private void buildTree(FileUser rootFolder,Node rootNode,  List<FileUser>list) {
        list.stream()
                .filter(fileUser -> Objects.equals(rootFolder.getId(), fileUser.getFPid()))
                .forEach(fileUser -> {
                    Node node = new Node(fileUser.getId(), fileUser.getFName(), new ArrayList<>());
                    rootNode.getChildren().add(node);
                    buildTree(fileUser, node, list);
                });
    }

    public void printTree(FileUser fileUser, int i) {
        System.out.print(fileUser.getFName());
        System.out.println();
        for (FileUser n : fileUser.getList()) {
            for(int j = 0; j < i; ++j) {
                System.out.print("----");
            }
            printTree(n, i + 1);
        }
    }

    private boolean saveFileItem(String uId, Chunk chunk) {
        final FileItem fileItem = convertChunkToFileItem(uId, 0, chunk);
        return fileItemMapper.insert(fileItem) > 0;
    }

    private FileItem convertChunkToFileItem(String uId, int status, Chunk chunk) {
        FileItem fileItem = new FileItem();
        fileItem.setId(chunk.getIdentifier());
        fileItem.setFSize(chunk.getTotalSize());
        fileItem.setFName(chunk.getFilename());
        fileItem.setMime(chunk.getMime());
        fileItem.setFType(chunk.getType());
        fileItem.setUDate(LocalDateTime.now());
        fileItem.setStatus(status);
        fileItem.setUId(uId);
        String builder = TODAY_COMMON_PATH + "/" +
                chunk.getIdentifier();
        fileItem.setPath(builder);
        return fileItem;
    }
    /**
     * 每天0点执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private static String todayCommonPath() {
        final LocalDateTime now = LocalDateTime.now();
        final int year = now.getYear();
        final int month = now.getMonthValue();
        final int day = now.getDayOfMonth();
        Path absolute = Paths.get(uploadFolder, Integer.toString(year), Integer.toString(month), Integer.toString(day));
        TODAY_COMMON_PATH = absolute.toString();
        return absolute.toString();
    }
}
