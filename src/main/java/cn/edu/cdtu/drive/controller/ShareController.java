package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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
    @GetMapping("share")
    public Result selectShare(HttpServletRequest request) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        if(Objects.isNull(login)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }

        return null;
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
}
