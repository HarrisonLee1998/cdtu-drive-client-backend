package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Chunk;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.service.FileService;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.RedisUtil;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/17 15:46
 */
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private ShareService shareService;

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

    /**
     * 自有空间与共享空间合并处理
     * @param request
     * @param path
     * @return
     */
    @ApiOperation("获取当前文件夹下的文件信息")
    @GetMapping("file/folder")
    public Result getFolderTree(HttpServletRequest request,  @RequestParam @NotBlank String path) {
        final Result result = Result.result();
        final String token = CookieUtil.getCookie(request, "token");
        final Login login = (Login) redisUtil.get(token);
        String uId = login.getUId();
        String gId = request.getParameter("gId");
        FileUser fileUser =fileService.selectFileByPath(uId, gId, path);
        result.put("file", fileUser);
        return result;
    }

    @ApiOperation("新增用户文件关系")
    @PostMapping("file/user")
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
    @PostMapping("file/folder")
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

    /**
     * 自有空间与共享空间合并处理
     * @param request
     * @return
     */
    @ApiOperation("获取目录树")
    @GetMapping("file/folder/tree")
    public Result selectFolderTree(HttpServletRequest request) {
        final Result result = Result.result();
        final Login login = userService.getLoginFromToken(request);
        String gId = request.getParameter("gId");
        final Node node = fileService.selectFolderTree(login.getUId(), gId);
        if(Objects.isNull(node)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("nodes", node);
        }
        return  result;
    }

    @ApiOperation("重命名文件")
    @PatchMapping("file/rename")
    public Result rename(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        Result result = Result.result();
        Login login = userService.getLoginFromToken(request);
        String id = (String) map.get("id");
        String name = (String) map.get("name");
        map.forEach((key, value) -> {
            System.out.println(key + " : " + value);
        });
        if(Objects.nonNull(login) && Objects.nonNull(id) && Objects.nonNull(name)) {
            var b = fileService.rename(login.getUId(), id, name);
            if(!b) {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @ApiOperation("复制文件")
    @PatchMapping("file/copy")
    public Result copy(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        var src = (String)map.get("src");
        var desc = (String)map.get("desc");
        var login = userService.getLoginFromToken(request);
        return fileService.copy(login.getUId(), src, desc);
    }

    @ApiOperation("移动文件")
    @PatchMapping("file/move")
    public Result move(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        var src = (String)map.get("src");
        var desc = (String)map.get("desc");
        var login = userService.getLoginFromToken(request);
        return fileService.move(login.getUId(), src, desc);
    }

    /**
     * 自有空间与共享空间合并处理
     * @param request
     * @return
     */
    @ApiOperation("读取回收站列表")
    @GetMapping("file/recycle")
    public Result selectAllFilesForRecycle(HttpServletRequest request) {
        var login = userService.getLoginFromToken(request);
        var gId = request.getParameter("gId");
        var list = fileService.selectFileForRecycleBin(login.getUId(), gId);
        return Result.result().put("list", list);
    }

    @ApiOperation("回收站操作")
    @PatchMapping("file/recycle")
    public Result handleRecycle(HttpServletRequest request, @RequestBody Map<String, Object>map) {
        var login = userService.getLoginFromToken(request);
        var ids = (List<String>) map.get("ids");
        var flag = (Integer) map.get("flag");
        if(Objects.isNull(ids) || Objects.isNull(flag) || flag < 0 || flag > 1) {
            return Result.result().setStatus(HttpStatus.BAD_REQUEST);
        } else {
            return fileService.handleRecycle(login.getUId(), ids, flag);
        }
    }

    @ApiOperation("删除文件-用户关系")
    @PostMapping("file/deletion")
    public Result delete(HttpServletRequest request, @RequestBody Map<String, Object>map) {
        var login = userService.getLoginFromToken(request);
        var ids = (List<String>) map.get("ids");
        if(Objects.isNull(ids)) {
            return Result.result().setStatus(HttpStatus.BAD_REQUEST);
        } else {
            var result = Result.result();
            var b = fileService.delete(login.getUId(), ids);
            if(!b) {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return result;
        }
    }

    @ApiOperation("获取分享的目录")
    @GetMapping("share/file/folder")
    public Result selectFileByPathForShare(HttpServletRequest request,
                                           @RequestParam String shareId, @RequestParam String path) {
        var result = Result.result();
        var b = shareService.checkShare(request, shareId);
        if(!b) {
            result.setStatus(HttpStatus.UNAUTHORIZED);
            return result;
        } else {
            var fileUser = fileService.selectFileByPathForShare(shareId, path);
            var share = shareService.selectShareById(shareId);
            if(Objects.isNull(fileUser) || Objects.isNull(share)) {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                result.put("share", share);
                result.put("list", fileUser.getList());
            }
        }
        return result;
    }


    @ApiOperation("获取用户或者组的头像")
    @GetMapping(value = {"user/avatar", "group/avatar"})
    public void getAvatar(HttpServletRequest request,HttpServletResponse response,
                            @RequestParam String id) throws IOException {
        byte[] avatar = null;
        if(request.getRequestURI().startsWith("/user")) {
            avatar = fileService.getAvatar(id, null);
        } else {
            avatar = fileService.getAvatar(null, id);
        }
        if(Objects.isNull(avatar)) {
            response.setStatus(401);
        } else {
            response.getOutputStream().write(avatar);
        }
    }

    @ApiOperation("保存用户或者组的头像")
    @PostMapping(value = {"user/avatar", "group/avatar"})
    public void saveAvatar(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam("file") MultipartFile file) throws IOException {
        var id = request.getParameter("id");
        if(request.getRequestURI().startsWith("/user")) {
            fileService.saveAvatar(id, null, file);
        } else {
            fileService.saveAvatar(null, id, file);
        }
    }

    @ApiOperation("统计各学院的文件上传情况")
    @GetMapping(value = "admin/file/stats/dept")
    public Result selectSizeByDept() {
        var result = Result.result();
        var maps = fileService.selectSizeByDept();
        if(Objects.isNull(maps)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("maps", maps);
        }
        return result;
    }

    @ApiOperation("统计各学院的文件上传情况")
    @GetMapping(value = "admin/file/stats/type")
    public Result selectSizeByType() {
        var result = Result.result();
        var maps = fileService.selectSizeByType();
        if(Objects.isNull(maps)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("maps", maps);
        }
        return result;
    }

    @ApiOperation("分类型获取文件")
    @GetMapping("file/{pageNo:^[1-9]\\d*$}/{pageSize:^-?[0-9]+$}/{type}")
    public Result selectFileByType(HttpServletRequest request,
                                   @PathVariable Integer pageNo,
                                   @PathVariable Integer pageSize,
                                   @PathVariable String type) {
        var result = Result.result();

        var login = userService.getLoginFromToken(request);
        if(Objects.isNull(login)) {
            return result;
        }
        var pageInfo = fileService.selectByType(login.getUId(), type, pageNo, pageSize);
        if(Objects.isNull(pageInfo)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("pageInfo", pageInfo);
        }
        return result;
    }

    @ApiOperation("根据ID获取文件内容")
    @GetMapping("file/content")
    public void getFileContentById(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam String id) {
        try {
            var bytes = fileService.getFileById(id);
            if(Objects.isNull(bytes)) {
                response.setStatus(500);
            } else {
                System.out.println(bytes.length);
                response.getOutputStream().write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(401);
        }
    }
}
