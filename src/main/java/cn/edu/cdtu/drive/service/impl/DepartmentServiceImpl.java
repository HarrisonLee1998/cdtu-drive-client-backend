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
}
