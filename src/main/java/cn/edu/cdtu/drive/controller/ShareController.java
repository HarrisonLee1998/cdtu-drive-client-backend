package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Share;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ShareController {

    @Autowired
    private UserService userService;

    @Autowired
    private ShareService shareService;



    @ApiOperation("获取分享")
    @GetMapping("share/{pageNo:^[1-9]\\d*$}/{pageSize:^-?[0-9]+$}/{sortBy}")
    public Result selectShare(HttpServletRequest request,
                              @PathVariable Integer pageNo,
                              @PathVariable Integer pageSize,
                              @PathVariable String sortBy) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        if(Objects.isNull(login)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        } else {
            var pageInfo = shareService.selectShare(login.getUId(), pageNo, pageSize, sortBy);
            result.put("pageInfo", pageInfo);
        }
        return result;
    }

    @ApiOperation("创建分享")
    @PostMapping("share")
    public Result createShare(HttpServletRequest request, @RequestBody Map<String,Object> map) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        var ids = (List<String>)map.get("ids");
        var days = (Integer)map.get("days");
        var needPwd = (Boolean)map.get("needPwd");
        if(Objects.isNull(ids)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            var share = shareService.createShare(login.getUId(), ids, days, needPwd);
            if(Objects.isNull(share)) {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                result.put("share", share);
            }
        }
        return result;
    }

    @ApiOperation("核验分享")
    @GetMapping("share/check")
    public Result check(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam String shareId, @RequestParam String pwd) {
        var login = userService.getLoginFromToken(request);
        List<String>shareTokens = new ArrayList<>();
        if(Objects.nonNull(request.getCookies())) {
            for (Cookie cookie : request.getCookies()) {
                if(cookie.getName().startsWith("SHARE_TOKEN")) {
                    shareTokens.add(cookie.getValue());
                }
            }
        }
        var result = Result.result();
        if(Objects.isNull(login)) {
            result = shareService.checkShare(shareTokens,null, shareId, pwd);
        } else {
            result = shareService.checkShare(shareTokens, login.getUId(), shareId, pwd);
        }
        var share = (Share)result.getMap().get("share");
        System.out.println(share);
        if(Objects.nonNull(share)) {
            String token = DigestUtils.md5DigestAsHex((share.getId() + share.getPwd()).getBytes());
            Cookie cookie = new Cookie("SHARE_TOKEN" + shareId, token);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return result;
    }

    @ApiOperation("取消分享")
    @PostMapping("share/cancel")
    public Result cancelShare(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        var result = Result.result();
        var ids = (List<String>)map.get("ids");
        var login = userService.getLoginFromToken(request);
        var b = shareService.cancelShare(login.getUId(), ids);
        if(!b) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }
}
