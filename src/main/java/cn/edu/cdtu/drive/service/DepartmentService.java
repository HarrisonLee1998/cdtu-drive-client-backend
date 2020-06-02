package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Department;
import cn.edu.cdtu.drive.util.Node;

import java.util.List;
import java.util.Map;

/**
 * @author HarrisonLee
 * @date 2020/5/21 13:43
 */
public interface DepartmentService {
    List<Department> selectAll();
    Node selectDepartmentTree();

    Boolean addDepartment(Map<String, Object>map);
    Boolean partialUpdate(Map<String, Object>map);
    Boolean delete(String id);
}
