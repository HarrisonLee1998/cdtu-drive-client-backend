package cn.edu.cdtu.drive.controller;

import cn.edu.cdtu.drive.annotation.ApiOperation;
import cn.edu.cdtu.drive.pojo.Group;
import cn.edu.cdtu.drive.service.GroupService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@RestController
public class GroupController {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    /**
     * 新建共享
     * 删除共享
     * 共享人员管理
     * 搜索共享
     */

    @ApiOperation("新建共享组")
    @PostMapping("group")
    public Result newGroup(HttpServletRequest request, @RequestBody Map<String,Object>map) {
        var result = Result.result();
        var group = constructGroup(map);
        if(Objects.isNull(group)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        var login = userService.getLoginFromToken(request);
        groupService.newGroup(group, login.getUId());
        return result;
    }

    @ApiOperation("修改共享组")
    @PatchMapping("group")
    public Result updateGroup(HttpServletRequest request, @RequestBody Map<String,Object>map) {
        var result = Result.result();
        var group = constructGroup(map);
        if(Objects.isNull(group)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
            return result;
        }
        var login = userService.getLoginFromToken(request);
        groupService.updateGroup(group, login.getUId());
        return result;
    }


    @ApiOperation("获取该用户所加入的共享组")
    @GetMapping("user/group")
    public Result selectGroupForUser(HttpServletRequest request) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        var groups = groupService.selectGroupsForUser(login.getUId());
        if(Objects.isNull(groups)) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            result.put("groups", groups);
        }
        return result;
    }


    @ApiOperation("搜索共享组")
    @GetMapping("group")
    public Result searchGroup(@RequestParam String gId) {
        var result = Result.result();
        var group = groupService.selectGroupById(gId);
        if(Objects.isNull(group)) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            result.put("group", group);
        }
        return result;
    }

    @ApiOperation("获取共享组内的用户")
    @GetMapping("group/users")
    public Result selectGroupUser(@RequestParam String gId, @RequestParam Integer status) {
        var result = Result.result();
        System.out.println(gId);
        var users = groupService.selectGroupUsers(gId, status);
        if(Objects.nonNull(users)) {
            result.put("users", users);
        } else {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @ApiOperation("获取当前用于与目标共享组的关系")
    @GetMapping("group/user")
    public Result selectGroupUserType(HttpServletRequest request, @RequestParam String gId) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        var groupUser = groupService.selectGroupUser(gId, login.getUId());
        if(Objects.nonNull(groupUser)) {
            result.put("groupUser", groupUser);
        } else {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @ApiOperation("修改共享组内的用户")
    @PatchMapping("group/user")
    public Result updateGroupUser(@RequestParam String gId, @RequestParam String uId,
                                  @RequestParam Integer flag) {
        var result = Result.result();
        System.out.println(gId);
        System.out.println(uId);
        return result;
    }

    @ApiOperation("删除共享组")
    @DeleteMapping("group")
    public Result deleteGroup(HttpServletRequest request, @RequestParam String gId) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        var aBoolean = groupService.deleteGroup(gId, login.getUId());
        if(!aBoolean) {
            result.setStatus(HttpStatus.BAD_REQUEST);
        }
        return result;
    }

    @ApiOperation("用户提交加入共享组的申请")
    @PostMapping("group/user")
    public Result joinGroup(HttpServletRequest request, @RequestBody Map<String,Object>map) {
        var result = Result.result();
        var login = userService.getLoginFromToken(request);
        var gId = (String)map.get("gId");
        var aBoolean = groupService.joinGroup(gId, login.getUId());
        if(!aBoolean) {
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }


    private Group constructGroup(Map<String,Object>map) {
        var id = (String)map.get("id");
        var title = (String)map.get("title");
        var brief = (String)map.get("brief");
        var isReadable = (Boolean)map.get("isReadable");
        var isWriteable = (Boolean)map.get("isWriteable");

        var group = new Group();
        if(Objects.isNull(title) || title.isBlank() ||
                Objects.isNull(isReadable) || Objects.isNull(isWriteable)) {
            return null;
        }
        if(Objects.nonNull(id)) {
            group.setId(id);
        }
        group.setTitle(title);
        group.setBrief(brief);
        group.setIsReadable(isReadable ? 1 : 0);
        group.setIsWriteable(isWriteable ? 1 : 0);
        return  group;
    }
}
