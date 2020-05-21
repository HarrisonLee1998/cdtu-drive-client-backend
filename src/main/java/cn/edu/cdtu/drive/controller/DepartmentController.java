package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Department;
import cn.edu.cdtu.drive.service.DepartmentService;
import cn.edu.cdtu.drive.util.Node;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/21 21:26
 */
@RestController
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("查询所有部门信息")
    @GetMapping("admin/dept")
    public Result selectAllDept(HttpServletRequest request) {
        final List<Department> departments = departmentService.selectAll();
        final Result result = Result.result();
        if(Objects.nonNull(departments)) {
            result.put("depts", departments);
        }
        return result;
    }

    @ApiOperation("查询部门树")
    @GetMapping("admin/dept/tree")
    public Result selectDeptTree(HttpServletRequest request) {
        final Node node= departmentService.selectDepartmentTree();
        final Result result = Result.result();
        if(Objects.nonNull(node)) {
            result.put("node", node);
        }
        return result;
    }
}
