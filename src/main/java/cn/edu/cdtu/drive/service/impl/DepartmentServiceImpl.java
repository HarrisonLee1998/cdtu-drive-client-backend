package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.DepartmentMapper;
import cn.edu.cdtu.drive.pojo.Department;
import cn.edu.cdtu.drive.service.DepartmentService;
import cn.edu.cdtu.drive.util.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author HarrisonLee
 * @date 2020/5/21 13:43
 */

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    private List<Department>departments;

    public List<Department>selectAll() {
        fillDepts();
        // 缓存
        return departments;
    }

    @Override
    public Node selectDepartmentTree() {
        final List<Department> list = departmentMapper.selectAll();
        Node rootNode = new Node();

        rootNode.setId("0");
        rootNode.setLabel("成都工业学院");

        Department rootDept = new Department();
        rootDept.setId("0");

        buildTree(rootDept, rootNode, list);
        return rootNode;
    }

    @Override
    public Boolean addDepartment(Map<String, Object> map) {
        var department = convertMapToDept(map);
        System.out.println(department);
        if(Objects.isNull(department) ||
                Objects.isNull(department.getTitle()) ||
                Objects.isNull(department.getPDid()) ||
                Objects.isNull(department.getType())) {
            return  false;
        }
        this.departments = null; // 缓存失效
        return departmentMapper.insert(department);
    }

    @Override
    public Boolean partialUpdate(Map<String, Object> map) {
        var department = convertMapToDept(map);
        if(Objects.isNull(department) ||
                (Objects.isNull(department.getTitle())&& Objects.isNull(department.getPDid()))) {
            return  false;
        }
        this.departments = null;
        return departmentMapper.partialUpdate(department);
    }

    @Override
    public Boolean delete(String id) {
        this.departments = null;
        return departmentMapper.deleteByPrimaryKey(id);
    }

    /**
     * 广度优先遍历
     * @param rootDept
     * @param rootNode
     * @param list
     */
    private void buildTree(Department rootDept, Node rootNode, List<Department> list) {
        Deque<Department> list1 = new ArrayDeque<>();
        list1.addLast(rootDept);

        Deque<Node>list2 = new ArrayDeque<>();
        list2.addFirst(rootNode);

        while(list1.size() > 0) {
            int size1 = list1.size(); // 当前这一层的数量
            for(int i = 0; i < size1; ++i) {
                Department dept = list1.pollFirst();
                Node node = list2.pollFirst();
                node.setChildren(new ArrayList<>());
                list.forEach(d -> {
                    if(Objects.equals(d.getPDid(), dept.getId())) {
                        list1.addLast(d);
                        Node n = new Node();
                        n.setId(d.getId());
                        n.setLabel(d.getTitle());
                        list2.addLast(n);
                        node.getChildren().add(n);
                    }
                });
            }
            // 待遍历完后，list1的size是下一层的数量
        }
    }

    private void fillDepts() {
        if(Objects.isNull(departments)) {
            departments = departmentMapper.selectAll();
        }
    }

    private Department convertMapToDept(Map<String, Object>map) {
        var id = map.get("id");
        var title = map.get("title");
        var pid = map.get("pid");
        var type = map.get("type");
        if(Objects.isNull(id)) {
            return null;
        }
        var department = new Department();
        department.setId((String) id);
        if(Objects.nonNull(title)) {
            department.setTitle((String) title);
        }
        if(Objects.nonNull(pid)) {
            department.setPDid((String) pid);
        }
        if(Objects.nonNull(type)) {
            department.setType((Integer) type);
        }
        return department;
    }
}
