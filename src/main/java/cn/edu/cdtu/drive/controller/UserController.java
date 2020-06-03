package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.DepartmentService;
import cn.edu.cdtu.drive.service.RoleService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/20 9:12
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("获取单个用户")
    @GetMapping("user")
    public Result getUser(HttpServletRequest request, @RequestParam String id) {
        final Result result = Result.result();
        final Login login = userService.getLoginFromToken(request);
        if(Objects.equals(login.getUId(), id)) {
            final User user = userService.getUserById(id);
            result.put("user", user);
        } else {
            result.setStatus(HttpStatus.UNAUTHORIZED);
        }
        return result;
    }

    @ApiOperation("批量新增用户")
    @PostMapping("admin/users")
    public Result addUser(HttpServletRequest request, HttpServletResponse response, MultipartFile file) {
        var b = roleService.checkAdminPermission(request, "用户管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var result = Result.result();
        b = userService.insertByBatch(file);
        if(!b) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    @ApiOperation("管理员分页获取用户列表")
    @GetMapping("admin/users/{pageNo:^[1-9]\\d*$}/{pageSize:^-?[0-9]+$}/{deptId}/{limit}/{type}")
    public Result selectUserByDept(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable Integer pageNo,
                                   @PathVariable Integer pageSize,
                                   @PathVariable String deptId,
                                   @PathVariable Integer limit,
                                   @PathVariable Integer type) {
        var b = roleService.checkAdminPermission(request, "用户管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var result = Result.result();
        var pageInfo = userService.selectUserByPage(pageNo, pageSize, deptId,limit, type);
        var departments = departmentService.selectAll();
        var roles = roleService.selectAll();

        if(Objects.isNull(pageInfo) || Objects.isNull(roles) || Objects.isNull(departments)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("pageInfo", pageInfo);
            result.put("depts", departments);
            result.put("roles", roles);
        }
        return result;
    }


    @ApiOperation("处理用户限制")
    @PatchMapping("admin/users/limit")
    public Result handleLimit(HttpServletRequest request, HttpServletResponse response,
                              @RequestBody Map<String, Object>map){
        var result = Result.result();
        var b = roleService.checkAdminPermission(request, "用户管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        List<String>ids = (List<String>) map.get("ids");
        Integer limit = (Integer) map.get("limit");
        b = userService.setLimit(ids, limit);
        if(!b){
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @ApiOperation("修改用户部分信息")
    @PatchMapping(value = {"admin/user", "user"})
    public Result partialUpdate(HttpServletRequest request, HttpServletResponse response,
                                @RequestBody User user) {
        var result = Result.result();
        var b = false;
        if(request.getRequestURI().startsWith("/admin")) {
            b = roleService.checkAdminPermission(request, "用户管理");
            if(!b) {
                response.setStatus(401);
                return null;
            }
        }
        b = userService.partialUpdate(user);
        if(!b) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
}
