package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author HarrisonLee
 * @date 2020/5/17 14:17
 */
public interface UserService {
    Map<String, Object> checkUserLogin(User user);
    Login saveLoginInfo(User user, String ip, int state, int isAdmin);

    Login getLoginFromToken(HttpServletRequest request);
    User getUserFromToken(HttpServletRequest request);

    User getUserById(String id);
}
