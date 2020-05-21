package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.pojo.Menu;
import cn.edu.cdtu.drive.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author HarrisonLee
 * @date 2020/5/21 19:10
 */
@SpringBootTest
public class MenuTest {

    @Autowired
    private RoleService roleService;

    @Test
    public void test01() {
        final List<Menu> menus = roleService.selectMenuByRole(1);
        menus.forEach(System.out::println);
    }
}
