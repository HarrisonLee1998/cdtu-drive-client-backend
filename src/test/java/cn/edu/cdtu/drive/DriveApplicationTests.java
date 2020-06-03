package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.dao.GroupUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DriveApplicationTests {

    @Autowired
    private GroupUserMapper groupUserMapper;

    @Test
    void contextLoads() {
        groupUserMapper.selectGroupUsers("Ab5wouLWdh2B", 0).forEach(System.out::println);
        groupUserMapper.selectGroupUsers("Ab5wouLWdh2B", 1).forEach(System.out::println);
    }

}
