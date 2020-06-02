package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Department;
import java.util.List;

public interface DepartmentMapper {
    boolean deleteByPrimaryKey(String id);

    boolean insert(Department record);

    Department selectByPrimaryKey(String id);

    List<Department> selectAll();

    boolean updateByPrimaryKey(Department record);

    boolean partialUpdate(Department department);
}