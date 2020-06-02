package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.Menu;
import cn.edu.cdtu.drive.pojo.User;
import cn.edu.cdtu.drive.service.RoleService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.RedisUtil;
import cn.edu.cdtu.drive.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/17 15:45
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RoleService roleService;

    private final static int LOGIN_EXPIRE_SECONDS = 60 * 60;

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @ApiOperation("用户登录")
    @PostMapping(value = {"login", "admin/login"})
    public Result login(HttpServletRequest request, HttpServletResponse response,  @RequestBody @Valid User user) {
        String ip = request.getRemoteAddr();
        final Result result = Result.result();
        Map<String, Object>map = userService.checkUserLogin(user,
                request.getRequestURI().startsWith("/admin") ? 1 : 0);
        final int i = (Integer) map.get("status");
        if(i == 401) {
            result.setStatus(HttpStatus.UNAUTHORIZED);
        } else if(i == 403) {
            result.setStatus(HttpStatus.FORBIDDEN);
        } else if(i == 200) {
            user = (User) map.get("user");
            user.setPassword(null);
        }

        // 保存登录信息
        Login login = null;
        if(Objects.nonNull(user)) {
            if(Objects.equals(result.getStatus(), HttpStatus.OK)) {
                login = userService.saveLoginInfo(user, ip, 1);
            }else{
                login = userService.saveLoginInfo(user, ip, 0);
            }
        }

        if(Objects.isNull(login)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        map.clear();
        map.put("id", user.getId());
        // final String token = JWTUtil.generate(map);
        final String token = DigestUtils.md5DigestAsHex((user.getId() + user.getPassword()).getBytes());
        final boolean b = redisUtil.set(token, login, LOGIN_EXPIRE_SECONDS);

        boolean flag = false;
        // 检查是否已存在相应的cookie； 如果已存在，更新对应的值
        if(Objects.nonNull(request.getCookies())) {
            for (Cookie c : request.getCookies()) {
                if(c.getName().equals("token")) {
                    c.setValue(token);
                    response.addCookie(c);
                    flag = true;
                    break;
                }
            }
        }
        // 如果已存在，就不设置了
        if(!flag) {
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");
            cookie.setMaxAge(LOGIN_EXPIRE_SECONDS);
            response.addCookie(cookie);
        }
        result.put("user", user);
        return result;
    }

    @ApiOperation("后台根据管理员的角色类型查询菜单")
    @GetMapping("admin/menu")
    public Result selectMenu(HttpServletRequest request, HttpServletResponse response){
        final Result result = Result.result();
        final User user = userService.getUserFromToken(request);
        if(Objects.isNull(user) || Objects.isNull(user.getRoleId())) {
            response.setStatus(401);
            return result;
        } else {
            final List<Menu> menus = roleService.selectMenuByRole(user.getRoleId());
            result.put("menus", menus);
        }
        return result;
    }

    @ApiOperation("路由鉴权")
    @GetMapping(value = {"login/check", "admin/login/check"})
    public Result check(HttpServletRequest request) {
        var result = Result.result();
        if(request.getRequestURI().startsWith("/admin")) {
            logger.info("管理员权限验证");
            var path = request.getParameter("path");
            if(Objects.equals("/", path)) {
                return result;
            }
            var b = roleService.checkAdminMenu(request, path);
            if(!b) {
                logger.info("当前管理员对页面" + path + "不具备访问权限");
                result.setStatus(HttpStatus.UNAUTHORIZED);
            }
        }
        return result;
    }

    @ApiOperation("登出")
    @GetMapping("logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        final String token = CookieUtil.getCookie(request, "token");
        redisUtil.del(token);
        Arrays.stream(request.getCookies()).forEach(cookie -> {
            if(Objects.equals(cookie.getName(), "token")) {
                cookie.setValue(null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        });
    }
}
