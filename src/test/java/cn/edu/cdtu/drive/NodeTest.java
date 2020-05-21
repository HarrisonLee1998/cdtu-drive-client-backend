package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.service.DepartmentService;
import cn.edu.cdtu.drive.util.Node;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author HarrisonLee
 * @date 2020/5/21 14:00
 */
@SpringBootTest
public class NodeTest {

    @Autowired
    private DepartmentService departmentService;

    @Test
    public void selectDepartmentTree() {
        final Node node = departmentService.selectDepartmentTree();
        printTree(node, 1);
    }

    public void printTree(Node node, int i) {
        System.out.print(node.getLabel());
        System.out.println();
        for (Node n : node.getChildren()) {
            for(int j = 0; j < i; ++j) {
                System.out.print("----");
            }
            printTree(n, i + 1);
        }
    }
}
