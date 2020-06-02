package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Role;
import cn.edu.cdtu.drive.service.RoleService;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ApiOperation("获取所有角色和权限")
    @GetMapping("admin/role/perm")
    public Result selectAllRoleAndPerm(HttpServletRequest request, HttpServletResponse response) {
        var result = Result.result();
        var b = false;
        b = roleService.checkAdminPermission(request, "角色管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var roles = roleService.selectAll();
        var permissions = roleService.selectAllPerm();
        if(Objects.isNull(roles) || Objects.isNull(permissions)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("roles", roles);
            result.put("perms", permissions);
        }
        return result;
    }

    @ApiOperation("新增或更新角色")
    @PostMapping("admin/role")
    public Result insertOrUpdate(HttpServletRequest request, HttpServletResponse response,
                                 @RequestBody Role role) {
        var b = false;
        b = roleService.checkAdminPermission(request, "角色管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var result = Result.result();
        roleService.insertOrUpdate(role);
        return result;
    }

    @ApiOperation("删除角色")
    @DeleteMapping("admin/role")
    public Result delete(HttpServletRequest request, HttpServletResponse response, @RequestParam Integer id) {
        var result = Result.result();
        var b = false;
        b = roleService.checkAdminPermission(request, "角色管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        b = roleService.deleteById(id);
        if(!b) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }
}
