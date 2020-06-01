package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.ShareMapper;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.Share;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.service.UserService;
import cn.edu.cdtu.drive.util.CookieUtil;
import cn.edu.cdtu.drive.util.Result;
import cn.edu.cdtu.drive.util.UUIDHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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

        for (String token : shareTokens) {
            if(Objects.equals(token, DigestUtils.md5DigestAsHex((share.getId() + share.getPwd()).getBytes()))) {
                return result;
            }
        }
        result.put("SHARE_TOKEN", DigestUtils.md5DigestAsHex((share.getId() + share.getPwd()).getBytes()));
        if(Objects.equals(uId, share.getUId())) {
            return result;
        }
        if(Objects.equals(share.getPwd(), pwd)) {
            return result;
        } else if(Objects.nonNull(share.getExpireDate()) &&
                Duration.between(LocalDateTime.now(), share.getExpireDate()).toMillis() < 0) {
            return result;
        } else {
            result.setStatus(HttpStatus.BAD_REQUEST);
            result.put("msg", "提取码不正确");
            result.put("SHARE_TOKEN", null);
            return result;
        }
    }

    @Override
    public Boolean checkShare(HttpServletRequest request, String shareId) {
        CookieUtil.printCookies(request);
        if(Objects.isNull(shareId) || Objects.isNull(request)) {
            return false;
        }
        var cookies = request.getCookies();
        if(Objects.nonNull(cookies)){
            var share = shareMapper.selectByPrimaryKey(shareId);
            var login = userService.getLoginFromToken(request);
            if(Objects.nonNull(login) && Objects.equals(login.getUId(), share.getUId())) {
                return true;
            }
            if(Objects.isNull(share)) {
                return false;
            }
            var s = DigestUtils.md5DigestAsHex((share.getId() + share.getPwd()).getBytes());
            var list = new ArrayList<String>();
            for (Cookie cookie : cookies) {
                if(cookie.getName().startsWith("SHARE_TOKEN")) {
                    list.add(cookie.getValue());
                }
            }
            var b = list.contains(s);
            if(!b) {
                return false;
            } else {
                b = Duration.between(LocalDateTime.now(), share.getExpireDate()).toMillis() < 0;
                if(!b) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Share selectShareById(String shareId) {
        return shareMapper.selectByPrimaryKey(shareId);
    }
}
