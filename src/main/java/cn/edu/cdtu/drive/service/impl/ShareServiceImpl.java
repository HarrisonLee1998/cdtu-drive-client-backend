package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.ShareMapper;
import cn.edu.cdtu.drive.pojo.FileUser;
import cn.edu.cdtu.drive.pojo.Share;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.util.UUIDHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
        rootFileUser.setIsFolder(1);
        result = fileUserMapper.insert(rootFileUser) > 0;
        if(!result) {
            return null;
        }
        // 保存Share_fileUser
        list.forEach(f -> {
            f.setFPid(rootFileUser.getId());
            f.setFPath("/" + f.getFName());
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
    public Boolean checkShare(String uId, String shareId, String pwd) {
        if(Objects.isNull(shareId) || Objects.isNull(pwd) || shareId.length() != 12 || pwd.length() != 4) {
            return false;
        }
        var share = shareMapper.selectByPrimaryKey(shareId);
        if(Objects.isNull(share)) {
            return false;
        }
        if(Objects.equals(uId, share.getUId())) {
            return true;
        }
        if(Objects.equals(share.getPwd(), pwd)) {
            return true;
        } else if(Objects.nonNull(share.getExpireDate()) &&
                Duration.between(LocalDateTime.now(), share.getExpireDate()).toMillis() < 0) {
            return true;
        } else {
            return false;
        }
    }
}
