package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.dao.RoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RoleTest {

    @Autowired
    private RoleMapper roleMapper;

    @Test
    public void test01() {
        var roles = roleMapper.selectAllWithPerm();
        roles.forEach(System.out::println);
    }

}
