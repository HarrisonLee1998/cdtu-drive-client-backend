package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.LoginMapper;
import cn.edu.cdtu.drive.dao.UserMapper;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/17 14:18
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public Map<String, Object> checkUserLogin(User user) {
        Map<String, Object>map = new HashMap<>();
        if(Objects.isNull(user) || Objects.isNull(user.getId()) || Objects.isNull(user.getPassword())) {
            map.put("status", 401);
        } else {
            User user1 = userMapper.selectByPrimaryKey(user.getId());
            // 如果用户受限，那么则不能登录
            if(user1.getLimit() > 0) {
                map.put("status", 403);
            } else {
                String md5 = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
                if(md5.equalsIgnoreCase(user1.getPassword())) {
                    map.put("status", 200);
                } else {
                    map.put("status", 401);
                }
            }
            map.put("user", user1);
        }
        return map;
    }

    @Override
    public Login saveLoginInfo(User user, String ip, int state) {
        Login login = new Login();
        login.setUId(user.getId());
        login.setIp(ip);
        login.setDate(LocalDateTime.now());
        login.setLastActionDateTime(LocalDateTime.now());
        login.setState(state);
        final int i = loginMapper.insert(login);
        if(i > 0) {
            return login;
        }
        return null;
    }



    public Login getLoginFromToken(HttpServletRequest request) {
        final String token = CookieUtil.getCookie(request, "token");
        final Login login = (Login) redisUtil.get(token);
        return login;
    }

    public User getUserFromToken(HttpServletRequest request) {
        Login login = getLoginFromToken(request);
        String uId = login.getUId();
        return userMapper.selectByPrimaryKey(uId);
    }

    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }
}
