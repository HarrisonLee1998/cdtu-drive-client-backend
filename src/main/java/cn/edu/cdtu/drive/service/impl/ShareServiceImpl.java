package cn.edu.cdtu.drive.service.impl;

import cn.edu.cdtu.drive.dao.FileUserMapper;
import cn.edu.cdtu.drive.dao.ShareMapper;
import cn.edu.cdtu.drive.pojo.Share;
import cn.edu.cdtu.drive.service.ShareService;
import cn.edu.cdtu.drive.util.UUIDHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Override
    public PageInfo<Share> selectShare(String uId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize, "create_date");
        var shares = shareMapper.selectAllByUser(uId);
        return new PageInfo<>(shares);
    }



    @Override
    public Share createShare(String uId, List<String> ids, Integer days, Boolean needPwd) {
        for (String id : ids) {
            var fileUser = fileUserMapper.selectByPrimaryKey(id);
            if(Objects.isNull(fileUser) || !Objects.equals(uId, fileUser.getUId()) || fileUser.getIsDelete() == 1
                    || Objects.nonNull(fileUser.getShareId())) {
                return null;
            }
        }
        var share = new Share();
        share.setId(UUIDHelper.rand(12));
        share.setCreateDate(LocalDateTime.now());
        if(needPwd) {
            share.setPwd(UUIDHelper.rand(4).toLowerCase());
        }
        share.setExpireDate(LocalDateTime.now().plusDays(days));
        share.setDownloadTimes(0);
        share.setSaveTimes(0);
        share.setViewTimes(0);
        share.setLink(shareLinkPrefix + share.getId());
        share.setUId(uId);
        boolean result = shareMapper.insert(share);
        if(result) {
            result = shareMapper.saveShareFileUser(share.getId(), ids);
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
    public Boolean checkShare(String shareId, String pwd) {
        var share = shareMapper.selectByPrimaryKey(shareId);
        if(Objects.nonNull(share) && Objects.equals(share.getPwd(), pwd)) {
            return false;
        } else {
            return true;
        }
    }
}
