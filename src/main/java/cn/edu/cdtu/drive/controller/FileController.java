package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Chunk;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.service.FileService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.RedisUtil;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/17 15:46
 */
@RestController
public class FileController {
    @Value("${prop.upload-folder}")
    private String uploadFolder;

    @Autowired
    private FileService fileService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserService userService;

    /**
     * 上传分块
     * @param chunk 所上传的文件块 chunk属于formData，所以不用加@RequestBody
     * @return 返回上传结果信息
     */
    @ApiOperation("接收分块")
    @PostMapping("chunk/upload")
    public Result uploadChunk(HttpServletRequest request, Chunk chunk) {
        final Login login = userService.getLoginFromToken(request);
        Result result = Result.result();
        fileService.uploadChunk(login.getUId(), chunk);
        result.put("result", true);
        return result;
    }

    /**
     * checkChunk()方法会根据文件唯一标识，和当前块数判断是否已经上传过这个块
     * @param chunk 文件块
     * @param response http响应
     * @return 返回该块对象
     */
    @ApiOperation("检查分块是否存在")
    @PostMapping("chunk/check")
    public Result checkChunk(HttpServletRequest request,  HttpServletResponse response, Chunk chunk) {
        Result result = Result.result();

        final Login login = userService.getLoginFromToken(request);

        /**
         * 获取分块信息之前，一定要先检查权限
         */
        // 如果是上传第一块，那么需要校验权限
        final boolean permission = fileService.checkPermission(login.getUId(), chunk.getParentFileId());
        if(!permission) {
            result.setStatus(HttpStatus.UNAUTHORIZED);
            return result;
        }

        final List<Integer> list = fileService.checkChunk(login.getUId(), chunk);
        // 下面设置的字段，需要和前端匹配
        if(Objects.isNull(list)) {
            // 说明文件不存在，需要全部上传
            result.put("status", "null");
            result.put("ids", new ArrayList<>());
        } else if(list.size() == 0) {
            // 说明文件已存在
            result.put("status", "success");
        } else {
            // 说明文件存在部分分块
            result.put("status", "partial");
            result.put("ids", list);
        }
        return result;
    }

    /**
     * 处理合并分块的请求
     * @param chunk 需要合并的文件信息
     * @return 返回合并结果信息
     */
    @ApiOperation("合并分块")
    @PostMapping("chunk/merge")
    public Result merge(HttpServletRequest request, Chunk chunk) {
        final Login login = userService.getLoginFromToken(request);
        final Result result = Result.result();
        final boolean b = fileService.merge(login.getUId(), chunk);
        if(!b) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    @ApiOperation("获取当前文件夹下的文件信息")
    @GetMapping("file/folder/current")
    public Result getFolderTree(HttpServletRequest request,  @RequestParam @NotBlank String path) {
        final Result result = Result.result();
        final String token = CookieUtil.getCookie(request, "token");
        final Login login = (Login) redisUtil.get(token);
        String uId = login.getUId();
        FileUser fileUser = fileService.selectFileByPath(uId, path);
        result.put("file", fileUser);
        return result;
    }

    @ApiOperation("新增用户文件关系")
    @PostMapping("file/file/user")
    public Result addFileUser(HttpServletRequest request, Chunk chunk) {
        final Result result = Result.result();
        final Login login = userService.getLoginFromToken(request);
        if(Objects.isNull(login)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            final boolean b = fileService.saveFileUser(login.getUId(), chunk);
            if(!b) {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return result;
    }

    @ApiOperation("新建文件夹")
    @PostMapping("file/file/folder")
    public Result addFileUser(HttpServletRequest request, FileUser fileUser) {
        final Result result = Result.result();
        if(Objects.isNull(fileUser)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            final Login login = userService.getLoginFromToken(request);
            fileUser.setUId(login.getUId());
            final boolean b = fileService.newFileUser(fileUser);
            if(!b) {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return result;
    }

    @ApiOperation("获取目录树")
    @GetMapping("file/file/folder/tree")
    public Result selectFolderTree(HttpServletRequest request) {
        final Result result = Result.result();
        final Login login = userService.getLoginFromToken(request);
        final Node node = fileService.selectFolderTree(login.getUId());
        if(Objects.isNull(node)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("nodes", node);
        }
        return  result;
    }
}
