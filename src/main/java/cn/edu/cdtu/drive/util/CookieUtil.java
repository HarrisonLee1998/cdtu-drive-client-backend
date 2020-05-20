package cn.edu.cdtu.drive.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/17 17:06
 */
public class CookieUtil {
    public static String getCookie(HttpServletRequest request, String key) {
        for (Cookie cookie : request.getCookies()) {
            if(Objects.equals(cookie.getName(), key)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
