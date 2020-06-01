package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.DemoDataListener;
import cn.edu.cdtu.drive.util.Result;
import com.alibaba.excel.EasyExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/20 9:12
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("获取单个用户")
    @GetMapping("user")
    public Result getUser(HttpServletRequest request, @RequestParam String id) {
        final Result result = Result.result();
        final Login login = userService.getLoginFromToken(request);
        if(Objects.equals(login.getUId(), id)) {
            final User user = userService.getUserById(id);
            result.put("user", user);
        } else {
            result.setStatus(HttpStatus.UNAUTHORIZED);
        }
        return result;
    }

    @ApiOperation("批量新增用户")
    @PostMapping("admin/users")
    public Result addUser(HttpServletRequest request, MultipartFile file) {
        var result = Result.result();
        System.out.println(file.getOriginalFilename());
        try {
            EasyExcel.read(file.getInputStream(), User.class, new DemoDataListener()).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
