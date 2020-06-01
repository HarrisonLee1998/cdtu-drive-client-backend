package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.ShareMapper;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.Share;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.Result;
import cn.edu.cdtu.drive.util.UUIDHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ShareServiceImpl implements ShareService {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private FileUserMapper fileUserMapper;

    @Autowired
    private UserService userService;

    @Value("share.link.prefix")
    private String shareLinkPrefix;

    private Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);

    private static List<String> sortFields = Arrays.asList("create_date", "view_times", "save_times", "download_times");

    @Override
    public PageInfo<Share> selectShare(String uId, Integer pageNo, Integer pageSize, String sortBy) {
        if(!sortFields.contains(sortBy)) {
            return null;
        }
        PageHelper.startPage(pageNo, pageSize, sortBy);
        var shares = shareMapper.selectAllByUser(uId);
        shares.forEach(share -> {
            share.setLink(shareLinkPrefix + share.getId());
        });
        return new PageInfo<>(shares);
    }

    @Override
    public Share createShare(String uId, List<String> ids, Integer days, Boolean needPwd) {
        List<FileUser>list = new ArrayList<>();
        for (String id : ids) {
            var fileUser = fileUserMapper.selectByPrimaryKey(id);
            if(Objects.isNull(fileUser) || !Objects.equals(uId, fileUser.getUId()) || fileUser.getIsDelete() == 1
                    || Objects.nonNull(fileUser.getShareId())) {
                return null;
            }
            list.add(fileUser);
        }
        var share = new Share();
        share.setId(UUIDHelper.rand(12));
        share.setCreateDate(LocalDateTime.now());
        if(needPwd) {
            share.setPwd(UUIDHelper.rand(4).toLowerCase());
        }
        if(days > 0) {
            share.setExpireDate(LocalDateTime.now().plusDays(days));
        }
        share.setDownloadTimes(0);
        share.setSaveTimes(0);
        share.setViewTimes(0);
        share.setLink(shareLinkPrefix + share.getId());
        share.setUId(uId);
        if(list.size() > 1) {
            share.setTitle(list.get(0).getFName() + "等");
        } else {
            share.setTitle(list.get(0).getFName());
        }
        boolean result = shareMapper.insert(share);
        if(!result) {
            return null;
        }
        // 创建根目录
        FileUser rootFileUser = new FileUser();
        rootFileUser.setId(DigestUtils.md5DigestAsHex((share.getId() + uId).getBytes()));
        rootFileUser.setShareId(share.getId());
        rootFileUser.setFName("/");
        rootFileUser.setFPath("/");
        rootFileUser.setIsFolder(1);
        result = fileUserMapper.insert(rootFileUser) > 0;
        if(!result) {
            return null;
        }
        // 保存Share_fileUser
        list.forEach(f -> {
            f.setFPid(rootFileUser.getId());
            f.setFPath("/" + f.getFName());
            f.setShareId(share.getId());
            f.setUId(null);
        });

        List<FileUser>total = new ArrayList<>();
        List<FileUser>subList = new ArrayList<>();

        while(list.size() > 0) {
            for (FileUser fileUser : list) {
                var temp = fileUserMapper.selectFilesByPId(fileUser.getId());
                fileUser.setId(DigestUtils.md5DigestAsHex((share.getId() + fileUser.getId()).getBytes()));
                temp.forEach(f -> {
                    f.setFPath(fileUser.getFPath() + "/" + f.getFName());
                    f.setFPid(fileUser.getId());
                    f.setShareId(share.getId());
                    f.setUId(null);
                });
                subList.addAll(temp);
            }
            total.addAll(list);
            list.clear();
            list.addAll(subList);
            subList.clear();
        }
        if(total.size() > 0) {
            result = fileUserMapper.insertByBatch(total);
        }
        if(result) {
            return share;
        } else {
            return null;
        }
    }

    @Override
    public Boolean deleteShare(String uId, String shareId) {
        var share = shareMapper.selectByPrimaryKey(shareId);
        if(!Objects.equals(share.getUId(), uId)){
            return false;
        }
        return shareMapper.deleteByPrimaryKey(shareId);
    }

    @Override
    public Result checkShare(List<String> shareTokens, String uId, String shareId, String pwd) {
        var result = Result.result();
        var share = shareMapper.selectByPrimaryKey(shareId);
        if(Objects.isNull(share)) {
            result.setStatus(HttpStatus.NOT_FOUND);
            return result;
        }
        result.put("share", share);
        var token = DigestUtils.md5DigestAsHex((share.getId() + share.getPwd()).getBytes());
        if(shareTokens.contains(token)) {
            // 如果cookie中存在当前分享的记录
            return result;
        }
        result.put("SHARE_TOKEN", token);
        if(Objects.equals(uId, share.getUId())) {
            // 如果当前存在已登录用户，且该分享的创建者是该用户
            return result;
        }
        if(Objects.equals(share.getPwd(), pwd)) {
            // 如果当前的密码匹配
            return result;
        } else if(Objects.nonNull(share.getExpireDate()) &&
                Duration.between(LocalDateTime.now(), share.getExpireDate()).toMillis() < 0) {
            // 当前的密码匹配，但是该分享已过期
            return result;
        } else {
            // 其他情况，需要用户提供正确的提取码
            result.setStatus(HttpStatus.BAD_REQUEST);
            // 重置数据
            result.put("msg", "提取码不正确");
            result.put("SHARE_TOKEN", null);
            result.put("share", null);
            return result;
        }
    }

    @Override
    public Boolean checkShare(HttpServletRequest request, String shareId) {
        if(Objects.isNull(shareId) || Objects.isNull(request)) {
            // 如果分享的id或者请求为空
            logger.info("分享的id或者请求为空");
            return false;
        }
        var cookies = request.getCookies();
        if(Objects.nonNull(cookies)){
            var share = shareMapper.selectByPrimaryKey(shareId);
            // The result of this method can be a negative period if the end is before the start.
            if(Objects.isNull(share)
                    || Duration.between(LocalDateTime.now(), share.getExpireDate()).toSeconds() < 0) {
                // 如果请求的分享不存在或已过期
                logger.info("请求的分享不存在或已过期");
                return false;
            }
            var login = userService.getLoginFromToken(request);
            if(Objects.nonNull(login) && Objects.equals(login.getUId(), share.getUId())) {
                // 如果该分享的创建者为当前用户（可能不存在）
                logger.info("该分享的创建者为当前用户");
                return true;
            }
            var s = DigestUtils.md5DigestAsHex((share.getId() + share.getPwd()).getBytes());
            for (Cookie cookie : cookies) {
                // 如果存在该分享的token
                if(cookie.getName().startsWith("SHARE_TOKEN") && Objects.equals(cookie.getValue(), s)) {
                    logger.info("存在该分享的token");
                    return true;
                }
            }
        }
        logger.info("cookie为空");
        return false;
    }

    @Override
    public Share selectShareById(String shareId) {
        return shareMapper.selectByPrimaryKey(shareId);
    }
}
