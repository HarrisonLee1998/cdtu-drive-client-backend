package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileItemMapper;
import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.GroupMapper;
import cn.edu.cdtu.drive.dao.UserMapper;
import cn.edu.cdtu.drive.pojo.Chunk;
import cn.edu.cdtu.drive.pojo.FileItem;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.service.DepartmentService;
import cn.edu.cdtu.drive.service.FileService;
import cn.edu.cdtu.drive.util.FileUtils;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.Result;
import cn.edu.cdtu.drive.util.UUIDHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private GroupMapper groupMapper;

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
        var fileUser = fileUserMapper.selectByPrimaryKey(fId);
        if(Objects.nonNull(fileUser.getGId())) {
            return true;
        }
        // fileUser = fileUserMapper.checkPermission(uId, fId);
        if(Objects.equals(uId, fileUser.getUId())) {
            return true;
        }
        // 增加其他逻辑，比如在组空间中上传
        // 继续检查当前文件夹是否属于某个分组，然后当前用户是否具有上传权限
        return false;
    }

    @Override
    public FileUser selectFileByPath(String uId, String gId, String path) {
        var fileUser = fileUserMapper.selectFileByPath(uId, gId, path);
        if(Objects.isNull(fileUser)) {
            return null;
        }
        fileUser.setList(fileUser.getList().stream().filter(f -> f.getIsDelete() == 0).collect(Collectors.toList()));
        return fileUser;
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
                        if(Objects.nonNull(parentFile.getGId())){
                            f.setGId(parentFile.getGId());
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
//            if(Objects.equals("/", parentFile.getFName())) {
//                // 如果父目录是根目录
//                fileUser.setFPath("/"  + fileUser.getFName());
//            } else {
//                // 如果父目录不是根目录
//                fileUser.setFPath(parentFile.getFPath()+ "/" + fileUser.getFName());
//            }
        } else {
            // 上传的是文件夹
            fileUser.setFPid(folders.get(folders.size() - 1).getId());
            // fileUser.setFPath(folders.get(folders.size() - 1).getFPath()+ "/" + fileUser.getFName());
        }

        if(Objects.nonNull(parentFile.getGId())){
            fileUser.setGId(parentFile.getGId());
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
            // 如果是组空间上传，那么需要设置组的id
            if(Objects.nonNull(parentFile.getGId())) {
                fileUser.setGId(parentFile.getGId());
                fileUser.setId(DigestUtils.md5DigestAsHex((parentFile.getGId() + fileUser.getFPath()).getBytes()));
            } else {
                fileUser.setId(DigestUtils.md5DigestAsHex((fileUser.getUId() + fileUser.getFPath()).getBytes()));
            }
            fileUser.setLastUpdateDate(LocalDateTime.now());
            fileUser.setIsFolder(1);
            fileUser.setIsDelete(0);
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
    public Node selectFolderTree(String uId, String gId) {
        List<FileUser>fileUsers = fileUserMapper.selectAllFolder(uId, gId);
        fileUsers = fileUsers.stream().filter(f -> f.getIsDelete() == 0).collect(Collectors.toList());
        FileUser rootFolder = fileUserMapper.selectFileByName(uId, gId, "/");
        if(Objects.isNull(rootFolder)) {
            return null;
        } else {
            Node rootNode = new Node();
            rootNode.setId(rootFolder.getId());
            rootNode.setLabel(rootFolder.getFName());
            rootNode.setChildren(new ArrayList<>());
            buildTree(rootFolder, rootNode, fileUsers);
            return rootNode;
        }
    }

    @Override
    public Boolean rename(String uId, String id, String name) {
        // 先查询出fileUser
        FileUser fileUser = fileUserMapper.selectByPrimaryKey(id);
        if(Objects.isNull(fileUser) || fileUser.getIsDelete() == 1) {
            return false;
        } else {
            // 鉴权
            if(Objects.nonNull(fileUser.getGId())) {
                // 组空间, 后面补充
            } else {
                // 用户空间
                if(!Objects.equals(uId, fileUser.getUId())) {
                    return false;
                }
            }
        }
        if(fileUser.getIsFolder()  == 1) {
            int i = fileUser.getFPath().lastIndexOf(fileUser.getFName());
            String path = fileUser.getFPath().substring(0, i) + name;
            fileUser.setFPath(path);
            updateSubFolderFPath(fileUser);
        }
        fileUser.setFName(name);
        fileUserMapper.updateByPrimaryKey(fileUser);
        return true;
    }

    @Override
    public Result move(String uId, String src, String desc) {
        var result = Result.result();
        if(Objects.isNull(src) || Objects.isNull(desc) || Objects.equals(src, desc)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        var srcFile = fileUserMapper.selectByPrimaryKey(src);
        var targetFile = fileUserMapper.selectByPrimaryKey(desc);

        if(Objects.isNull(srcFile) || Objects.isNull(desc) || targetFile.getIsFolder() != 1) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        if(srcFile.getIsDelete() == 1 || targetFile.getIsDelete() == 1) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        // 权限校验
        if(Objects.isNull(srcFile.getGId()) && Objects.isNull(targetFile.getGId())) {
            // 自有空间操作
            if(!Objects.equals(srcFile.getUId(), uId) || !Objects.equals(targetFile.getUId(), uId)) {
                result.setStatus(HttpStatus.BAD_REQUEST);
                return result;
            }
        } else if(Objects.nonNull(srcFile.getGId()) && Objects.nonNull(targetFile.getGId())) {
            // 组空间操作
        } else {
            // 其他情况
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        // 校验目标目录下第一级子目录是否有源目录同名的目录
        if(srcFile.getIsFolder() == 1){
            var list = fileUserMapper.selectFilesByPId(desc);
            for (FileUser f : list) {
                if(Objects.equals(f.getFName(), srcFile.getFName())) {
                    result.setStatus(HttpStatus.BAD_REQUEST);
                    result.put("msg", "目标目录存在与源目录同名的子目录");
                    return result;
                }
            }
        }
        // 校验目标目录是否为源目录的子目录,并同时更改源目录的fPath
        List<FileUser> list = new ArrayList<>();
        List<FileUser> subList = new ArrayList<>();
        List<FileUser> total = new ArrayList<>();
        list.add(srcFile);
        total.add(srcFile);
        srcFile.setFPid(targetFile.getId());
        if(Objects.equals(targetFile.getFName(), "/")) {
            // 如果目标目录是根目录
            srcFile.setFPath("/" + srcFile.getFName());
        } else {
            // 如果目标目录不是根目录
            srcFile.setFPath( targetFile.getFPath() + "/" + srcFile.getFName());
        }
        while(list.size() > 0) {
            for (FileUser f : list) {
                var temp = fileUserMapper.selectFilesByPId(f.getId());
                for (FileUser tf : temp) {
                    if(Objects.equals(tf.getId(), desc)) {
                        result.setStatus(HttpStatus.BAD_REQUEST);
                        result.put("msg", "目标目录不能是源目录或其子目录");
                        return result;
                    }
                    tf.setFPath(f.getFPath() + "/" + tf.getFName());
                }
                subList.addAll(temp);
                total.addAll(temp);
            }
            list.clear();
            list.addAll(subList);
            subList.clear();
        }
        for (FileUser fileUser : total) {
            fileUserMapper.updateByPrimaryKey(fileUser);
        }
        return result;
    }

    @Override
    public Result copy(String uId, String src, String desc) {
        var result = Result.result();
        if (Objects.isNull(src) || Objects.isNull(desc) || Objects.equals(src, desc)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }

        var srcFile = fileUserMapper.selectByPrimaryKey(src);
        var targetFile = fileUserMapper.selectByPrimaryKey(desc);

        if(Objects.isNull(srcFile) || Objects.isNull(targetFile) || targetFile.getIsFolder() != 1) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        if(srcFile.getIsDelete() == 1 || targetFile.getIsDelete() == 1) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }

        // 校验权限
        if(Objects.nonNull(srcFile.getGId()) && Objects.nonNull(targetFile.getGId())) {
            // 组空间操作
        } else if(Objects.isNull(srcFile.getGId()) && Objects.isNull(targetFile.getGId())) {
            // 自有空间操作
            if(!Objects.equals(srcFile.getUId(), uId) || !Objects.equals(targetFile.getUId(), uId)) {
                result.setStatus(HttpStatus.BAD_REQUEST);
                return result;
            }
        }
        // 校验目标目录下是否存在与源目录同名的文件夹
        if(srcFile.getIsFolder() == 1) {
            var list = fileUserMapper.selectFilesByPId(targetFile.getId());
            for (FileUser f : list) {
                if(Objects.equals(f.getFName(), srcFile.getFName())) {
                    result.setStatus(HttpStatus.BAD_REQUEST);
                    result.put("msg", "目标目录存在与源目录同名的子目录");
                    return result;
                }
            }
        }

        List<FileUser>list = new ArrayList<>();
        List<FileUser>subList = new ArrayList<>();
        List<FileUser>total = new ArrayList<>();

        list.add(srcFile);
        total.add(srcFile);
        srcFile.setFPid(targetFile.getId());
        if(Objects.equals(targetFile.getFName(), "/")) {
            // 如果目标目录是根目录
            srcFile.setFPath("/" + srcFile.getFName());
        } else {
            // 如果目标目录不是根目录
            srcFile.setFPath( targetFile.getFPath() + "/" + srcFile.getFName());
        }

        while(list.size() > 0) {
            for (FileUser f : list) {
                var temp = fileUserMapper.selectFilesByPId(f.getId());
                f.setId(DigestUtils.md5DigestAsHex((uId + f.getId()).getBytes()));
                for (FileUser tf : temp) {
                    if(Objects.equals(tf.getId(), desc)) {
                        result.setStatus(HttpStatus.BAD_REQUEST);
                        result.put("msg", "目标目录不能是源目录或其子目录");
                        return result;
                    }
                    tf.setFPath(f.getFPath() + "/" + tf.getFName());
                    tf.setFPid(f.getId());
                }
                subList.addAll(temp);
                total.addAll(temp);
            }
            list.clear();
            list.addAll(subList);
            subList.clear();
        }
        total.forEach(fileUser -> {
            fileUserMapper.insert(fileUser);
        });
        return result;
    }

    @Override
    public List<FileUser> selectFileForRecycleBin(String uId, String gId) {
        var list = fileUserMapper.selectAllRecycledFile(uId, gId);
        var map = new HashMap<String, FileUser>();
        list.stream().forEach(fileUser -> {
            map.put(fileUser.getId(), fileUser);
        });
        return list.stream().filter(fileUser -> map.get(fileUser.getFPid()) == null).collect(Collectors.toList());
    }

    @Override
    public Result handleRecycle(String uId, List<String> ids, Integer flag) {
        var result = Result.result();
        // 判断id是否存在
        for (String id : ids) {
            var fileUser = fileUserMapper.selectByPrimaryKey(id);
            if(Objects.isNull(fileUser)) {
                result.setStatus(HttpStatus.BAD_REQUEST);
                return result;
            }
            if(Objects.nonNull(fileUser.getGId())) {
                // 组空间
            } else {
                // 用户空间
                if(!Objects.equals(fileUser.getUId(), uId)) {
                    result.setStatus(HttpStatus.BAD_REQUEST);
                    return result;
                }
            }
            // 遍历子树
            List<FileUser>list = new ArrayList<>();
            List<FileUser>subList = new ArrayList<>();
            list.add(fileUser);
            while(list.size() > 0) {
                for (FileUser f : list) {
                    fileUserMapper.handleRecycle(f.getId(), flag);
                    var temp = fileUserMapper.selectFilesByPId(f.getId());
                    subList.addAll(temp);
                }
                list.clear();
                list.addAll(subList);
                subList.clear();
            }
        }
        return result;
    }

    @Override
    public Boolean delete(String uId, List<String> ids) {
        // 鉴权及数据校验
        for (String id : ids) {
            var fileUser = fileUserMapper.selectByPrimaryKey(id);
            if(Objects.isNull(fileUser)) {
                return false;
            } else if(Objects.nonNull(fileUser.getGId())) {
                // 组空间

            } else {
                // 用户空间
                if(!Objects.equals(fileUser.getUId(), uId)) {
                    return false;
                }
            }
        }
        if(ids.size() > 0) {
            return fileUserMapper.deleteByBatch(ids);
        }
        return true;
    }

    @Override
    public FileUser selectFileByPathForShare(String shareId, String path) {
        return fileUserMapper.selectFileByPathForShare(shareId, path);
    }

    @Override
    public byte[] getAvatar(String uId, String gId) {
        Path path = Paths.get(uploadFolder, "avatar/default.jpg");
        if(Objects.nonNull(uId)) {
            var user = userMapper.selectByPrimaryKey(uId);
            if(Objects.isNull(user)) {
                return null;
            } else if(Objects.nonNull(user.getAvatar())){
                path = Paths.get(user.getAvatar());
            }
        } else if(Objects.nonNull(gId)) {
            var group = groupMapper.selectByPrimaryKey(gId);
            if(Objects.isNull(group)) {
                return null;
            } else if(Objects.nonNull(group.getAvatar())){
                path = Paths.get(group.getAvatar());
            }
        } else {
            return null;
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean saveAvatar(String uId, String gId, MultipartFile file) throws IOException {
        if(Objects.nonNull(uId) && Objects.nonNull(file)) {
            var user = userMapper.selectByPrimaryKey(uId);
            if(Objects.nonNull(user)) {
                var path = Paths.get(uploadFolder, "avatar/user", UUIDHelper.rand(32));
                Files.write(path, file.getBytes());
                user.setAvatar(path.toString());
                userMapper.partialUpdate(user);
                return true;
            }
        } else if(Objects.nonNull(gId)&& Objects.nonNull(file)) {
            var group = groupMapper.selectByPrimaryKey(gId);
            if(Objects.nonNull(group)) {
                var path = Paths.get(uploadFolder, "avatar/group", UUIDHelper.rand(32));
                group.setAvatar(path.toString());
                Files.write(path, file.getBytes());
                groupMapper.updateByPrimaryKey(group);
                return true;
            }
        }
        return false;
    }


    @Override
    public List<Map<String, Object>> selectSizeByType() {
        return fileItemMapper.selectSizeByType();
    }

    @Override
    public List<Map<String, Object>> selectSizeByDept() {
        var maps = new ArrayList<Map<String, Object>>();
        var node = departmentService.selectDepartmentTree();
        var departments = departmentService.selectAll();
        node.getChildren().forEach(n -> {
            var map = new HashMap<String, Object>();
            map.put("title", n.getLabel());
            // 获取该学院下的所有部门
            var list = new ArrayList<String>();
            var subList = new ArrayList<String>();
            var total = new ArrayList<String>();

            list.add(n.getId());
            while(list.size() > 0) {
                for (String s : list) {
                    departments.forEach(department -> {
                        if(Objects.equals(s, department.getPDid())) {
                            subList.add(department.getId());
                        }
                    });
                }
                total.addAll(subList);
                list.clear();
                list.addAll(subList);
                subList.clear();
            }
            var size = 0L;
            if(total.size() > 0) {
                size = fileItemMapper.selectSizeByDept(total);
            }
            map.put("size", size);
            maps.add(map);
        });
        return maps;
    }

    @Override
    public PageInfo<FileUser> selectByType(String uId, String type, Integer pageNo, Integer pageSize) {
        List<String>types = Arrays.asList("document", "image", "video", "audio");
        if(Objects.nonNull(type) && !types.contains(type)) {
            return null;
        }
        PageHelper.startPage(pageNo, pageSize);
        var list = fileUserMapper.selectByType(uId, type);
        return new PageInfo<>(list);
    }

    @Override
    public byte[] getFileById(String id) throws IOException {
        var fileItem = fileItemMapper.selectByPrimaryKey(id);
        if(Objects.isNull(fileItem)) {
            return null;
        } else {
            var path = Paths.get(fileItem.getPath(), fileItem.getId());
            return Files.readAllBytes(path);
        }
    }

    @Override
    public void download(String uId, String id, HttpServletResponse response) {
        var fileUser = fileUserMapper.selectByPrimaryKey(id);
        var fileItem = fileItemMapper.selectByPrimaryKey(fileUser.getFId());
        if(Objects.nonNull(fileItem)) {
            var path = Paths.get(fileItem.getPath(), fileItem.getId());
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(path);
                response.setContentType("application/octet-stream");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="
                        + fileUser.getFName());
                response.getOutputStream().write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            response.setStatus(401);
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
        String builder = Paths.get(TODAY_COMMON_PATH, chunk.getIdentifier()).toString();
        fileItem.setPath(builder);
        return fileItem;
    }

    // 更新该文件夹下的所有文件
    private void updateSubFolderFPath(FileUser fileUser) {
        ArrayList<FileUser> list = new ArrayList<>();
        var subFolders = new ArrayList<FileUser>();
        list.add(fileUser);
        // 层序遍历，查询所有子folder

        while(list.size() > 0) {
            for (FileUser f : list) {
                var folders = fileUserMapper.selectFilesByPId(f.getId())
                        .stream()
                        .filter(temp -> temp.getIsFolder() == 1)
                        .collect(Collectors.toList());
                String path = f.getFPath();
                folders.forEach(ff->{
                    ff.setFPath(path + "/" + ff.getFName());
                });
                subFolders.addAll(folders);
            }
            subFolders.forEach(f -> {
                fileUserMapper.updateByPrimaryKey(f);
            });
            list.clear();
            list.addAll(subFolders);
            subFolders.clear();
        }
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
