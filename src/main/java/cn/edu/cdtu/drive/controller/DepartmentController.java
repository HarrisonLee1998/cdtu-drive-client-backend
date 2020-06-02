package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Department;
import cn.edu.cdtu.drive.service.DepartmentService;
import cn.edu.cdtu.drive.service.RoleService;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/21 21:26
 */
@RestController
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RoleService roleService;

    @ApiOperation("查询所有部门信息")
    @GetMapping("admin/dept")
    public Result selectAllDept(HttpServletRequest request, HttpServletResponse response) {
        var b = false;
        b = roleService.checkAdminPermission(request, "组织管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        final List<Department> departments = departmentService.selectAll();
        final Result result = Result.result();
        if(Objects.nonNull(departments)) {
            result.put("depts", departments);
        }
        return result;
    }

    @ApiOperation("查询部门树")
    @GetMapping("admin/dept/tree")
    public Result selectDeptTree(HttpServletRequest request, HttpServletResponse response) {
        var b = false;
        b = roleService.checkAdminPermission(request, "组织管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        final Node node= departmentService.selectDepartmentTree();
        final Result result = Result.result();
        if(Objects.nonNull(node)) {
            result.put("node", node);
        }
        return result;
    }

    @ApiOperation("新增部门")
    @PostMapping("admin/dept")
    public Result addDept(HttpServletRequest request, HttpServletResponse response,
                          @RequestBody Map<String, Object>map) {
        var b = false;
        b = roleService.checkAdminPermission(request, "组织管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var result = Result.result();
        b = departmentService.addDepartment(map);
        if(!b) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }


    @ApiOperation("修改部门")
    @PatchMapping("admin/dept")
    public Result modifyDept(HttpServletRequest request, HttpServletResponse response,
                             @RequestBody Map<String, Object>map) {
        var b = false;
        b = roleService.checkAdminPermission(request, "组织管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var result = Result.result();
        b = departmentService.partialUpdate(map);
        if(!b) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        // 保存修改
        return result;
    }

    @ApiOperation("删除部门")
    @DeleteMapping("admin/dept")
    public Result delete(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam String id) {
        var b = false;
        b = roleService.checkAdminPermission(request, "组织管理");
        if(!b) {
            response.setStatus(401);
            return null;
        }
        var result = Result.result();
        b = departmentService.delete(id);
        if(!b) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

}
