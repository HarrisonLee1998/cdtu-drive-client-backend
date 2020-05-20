package cn.edu.cdtu.drive.aop;


import cn.edu.cdtu.drive.annotation.ApiOperation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author HarrisonLee
 * @date 2020/4/5 1:27
 */
@Aspect
@Component
@Order(1)
public class WebLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    @Pointcut("execution(public * cn.edu.cdtu.drive.controller.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
    }

    @AfterReturning(value = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        var startTime = LocalDateTime.now();
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //记录请求信息
        var result = joinPoint.proceed();
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;
        var method = methodSignature.getMethod();
        if (method.isAnnotationPresent(ApiOperation.class)) {
            var webLog = new WebLog();
            HttpServletRequest request = attributes.getRequest();
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            webLog.setDescription(apiOperation.value());
            webLog.setIp(request.getRemoteAddr());
            webLog.setMethod(request.getMethod());
            webLog.setParameter(getParameter(method, joinPoint.getArgs()));
            webLog.setUri(URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8));
            webLog.setSpendTime(Duration.between(LocalDateTime.now(), startTime).toMillis());
            webLog.setEndTime(formatDate(LocalDateTime.now()));
            webLog.setStartTime(formatDate(startTime));
            webLog.setResult(result);
            LOGGER.info("{}", webLog.toString());
        }
        return result;
    }

    private String formatDate(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(localDateTime);
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        var argList = new ArrayList<>();
        var parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            var requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            var requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                var map = new HashMap<>();
                var key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
}
