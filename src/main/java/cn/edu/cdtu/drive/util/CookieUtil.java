package cn.edu.cdtu.drive.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author HarrisonLee
 * @date 2020/5/17 17:06
 */
public class CookieUtil {
    public static String getCookie(HttpServletRequest request, String key) {
        if(Objects.nonNull(request.getCookies())) {
            for (Cookie cookie : request.getCookies()) {
                if(Objects.equals(cookie.getName(), key)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void printCookies(HttpServletRequest request) {
        if(Objects.nonNull(request.getCookies())) {
            Arrays.stream(request.getCookies()).forEach(cookie -> {
                System.out.println(cookie.getName() + " : " +cookie.getValue());
            });
        }
    }
}
