package cn.edu.cdtu.drive;

import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.UserMapper;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.FileService;
import cn.edu.cdtu.drive.util.Node;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author HarrisonLee
 * @date 2020/5/19 12:51
 */
@SpringBootTest
public class FileUserTest {

    @Autowired
    FileUserMapper fileUserMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    FileService fileService;

    @Test
    public void initSpace(){
        final List<User> users = userMapper.selectAll();
        users.forEach(user -> {
            FileUser fileUser = new FileUser();
            fileUser.setId(DigestUtils.md5DigestAsHex((user.getId() + Instant.now()).getBytes()));
            fileUser.setFName("/");
            fileUser.setIsDelete(0);
            fileUser.setLastUpdateDate(LocalDateTime.now());
            fileUser.setUId(user.getId());
            fileUser.setIsFolder(1);
            fileUserMapper.insert(fileUser);
        });
    }

    @Test
    public void getFileByPath() {
        String path = "/";
        String uId = "16010201002";
        final FileUser fileUser = fileUserMapper.selectFileByPath(uId, null, path);
        fileUser.getList().forEach(System.out::println);
    }

    @Test
    public void updateDate() {
        String id = "fddcfcf90fd1cc2f56138c06048fdc73";
        fileUserMapper.updateDate(id, LocalDateTime.now());
    }

    @Test
    public void getFolderTree() {
        String id = "16010201001";
        final Node node = fileService.selectFolderTree(id, null);
        System.out.println("---------------------------------------");
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
