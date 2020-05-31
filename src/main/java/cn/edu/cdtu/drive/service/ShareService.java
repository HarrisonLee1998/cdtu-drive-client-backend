package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Share;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ShareService {
    PageInfo<Share> selectShare(String uId, Integer pageNo, Integer pageSize, String sortBy);
    Share createShare(String uId, List<String> ids, Integer days, Boolean needPwd);
    Boolean deleteShare(String uId, String shareId);
    Boolean checkShare(String uId, String shareId, String pwd);
}
