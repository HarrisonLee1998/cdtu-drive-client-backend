package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author HarrisonLee
 * @date 2020/5/17 23:50
 */
@SpringBootTest
public class FileTest {


    @Autowired
    private FileService fileService;

    @Test
    public void testSelectCurFolderTree() {
        String uId = "16010201002";
        String path = "/";
    }
}
