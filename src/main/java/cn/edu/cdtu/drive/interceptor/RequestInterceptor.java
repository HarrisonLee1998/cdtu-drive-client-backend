package cn.edu.cdtu.drive.interceptor;

import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.JWTUtil;
import cn.edu.cdtu.drive.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/16 17:36
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

    private static ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        final String url = request.getRequestURI();
        final String method = request.getMethod();
        if(Objects.equals("OPTIONS", method)) {
            return true;
        }
        if(url.endsWith("/login")) {
            return true;
        }
        // 向redis请求，判断session ID是否存在
        final Cookie[] cookies = request.getCookies();
        if(Objects.isNull(cookies)) {
            response.setStatus(401);
            logger.info("cookie为空");
            return false;
        }

        final String token = CookieUtil.getCookie(request, "token");
        try {
            JWTUtil.parse(token);
        } catch (RuntimeException e) {
            response.setStatus(401);
            logger.info("token损坏");
            return false;
        }

        final Login login = (Login) redisUtil.get(token);
        if(Objects.isNull(login)) {
            logger.info("redis中不存在当前登录信息");
            response.setStatus(401);
            return false;
        }
        final LocalDateTime lastActionDateTime = login.getLastActionDateTime();
        final LocalDateTime localDateTime = login.getDate();

        long seconds = Duration.between(lastActionDateTime, LocalDateTime.now()).toSeconds();

        // 如果大于了20分钟，没有操作，那么就要求用户重新登录
        if(seconds > 60*20) {
            logger.info("超过20分钟没有操作");
            response.setStatus(401);
            return false;
        }
        seconds = Duration.between(localDateTime, LocalDateTime.now()).toSeconds();

        // 如果登录有效期低于了20分钟，那么重置过期时间为现在的一个小时后
        if(seconds < 60 * 20) {
            redisUtil.set(token, login, 60 * (60 - seconds));
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}