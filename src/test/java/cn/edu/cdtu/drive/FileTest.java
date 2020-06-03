package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

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

    @Test
    public void selectSizeByType() {
        var maps = fileService.selectSizeByType();
        for (Map<String, Object> map : maps) {
            map.forEach((key, value) -> {
                System.out.println(key + " : " + value);
            });
            System.out.println("===========================");
        }
    }
    @Test
    public void selectSizeByDept() {
        var maps = fileService.selectSizeByDept();
        for (Map<String, Object> map : maps) {
            map.forEach((key, value) -> {
                System.out.println(key + " : " + value);
            });
            System.out.println("===========================");
        }
    }


}
