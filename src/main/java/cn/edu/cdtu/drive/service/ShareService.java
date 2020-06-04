package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Share;
import cn.edu.cdtu.drive.util.Result;
import com.github.pagehelper.PageInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ShareService {
    PageInfo<Share> selectShare(String uId, Integer pageNo, Integer pageSize, String sortBy);
    Share createShare(String uId, List<String> ids, Integer days, Boolean needPwd);
    Boolean deleteShare(String uId, String shareId);
    Result checkShare(List<String>shareTokens, String uId, String shareId, String pwd);
    Boolean checkShare(HttpServletRequest request, String shareId);
    Share selectShareById(String shareId);

    boolean cancelShare(String uId, List<String>ids);
}
