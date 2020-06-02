package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.DepartmentMapper;
import cn.edu.cdtu.drive.dao.LoginMapper;
import cn.edu.cdtu.drive.dao.UserMapper;
import cn.edu.cdtu.drive.pojo.Department;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.SimpleUser;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.DemoDataListener;
import cn.edu.cdtu.drive.util.RedisUtil;
import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private DepartmentMapper departmentMapper;

    private List<Department>departments;


    @Override
    public Map<String, Object> checkUserLogin(User user, Integer type) {
        Map<String, Object>map = new HashMap<>();
        if(Objects.isNull(user) || Objects.isNull(user.getId()) || Objects.isNull(user.getPassword())) {
            map.put("status", 401);
        } else {
            User user1 = userMapper.selectByPrimaryKey(user.getId());
            if(Objects.isNull(user1)) {
                map.put("status", 401);
                return map;
            }
            if(Objects.isNull(user1.getRoleId()) && type == 1) {
                map.put("status", 401);
                return map;
            }
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

    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,timeout=36000,rollbackFor=Exception.class)
    @Override
    public Boolean insertByBatch(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), SimpleUser.class, new DemoDataListener(userMapper)).sheet().doRead();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PageInfo<User> selectUserByPage(Integer pageNo, Integer pageSize, String deptId,
                                           Integer limit, Integer type) {
        // 首先先获取到该部门下所有的部门id
        var ids = getSubDeptsIds(deptId);
        PageHelper.startPage(pageNo, pageSize);
        var users = userMapper.selectByDept(ids, limit, type);
        users.forEach(user -> {
            user.setPassword(null);
        });
        return new PageInfo<>(users);
    }

    @Override
    public boolean setLimit(List<String> ids, Integer limit) {
        if(Objects.isNull(ids) || Objects.isNull(limit) || ids.size() == 0 || limit < 0 || limit > 1){
            return false;
        }
        return userMapper.setLimit(ids, limit);
    }

    @Override
    public boolean partialUpdate(User user) {
        if(Objects.isNull(user)) {
            return false;
        }
        return userMapper.partialUpdate(user);
    }

    private List<String> getSubDeptsIds(String rootId) {
        List<String>list = new ArrayList<>();
        List<String>subList= new ArrayList<>();
        List<String>total = new ArrayList<>();
        list.add(rootId);
        total.add(rootId);

        fillDepts();
        while(list.size() > 0) {
            for (String s : list) {
                departments.forEach(department -> {
                    if(Objects.equals(s, department.getPDid())) {
                        subList.add(department.getId());
                    }
                });
                total.addAll(subList);
            }
            list.clear();
            list.addAll(subList);
            subList.clear();
        }
        return total.stream().distinct().collect(Collectors.toList());
    }

    private void fillDepts() {
        if(Objects.isNull(departments)) {
            departments = departmentMapper.selectAll();
        }
    }
}
