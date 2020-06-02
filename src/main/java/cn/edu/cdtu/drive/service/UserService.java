package cn.edu.cdtu.drive.service;

import cn.edu.cdtu.drive.pojo.Login;
import cn.edu.cdtu.drive.pojo.User;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author HarrisonLee
 * @date 2020/5/17 14:17
 */
public interface UserService {
    Map<String, Object> checkUserLogin(User user, Integer type);
    Login saveLoginInfo(User user, String ip, int state);

    Login getLoginFromToken(HttpServletRequest request);
    User getUserFromToken(HttpServletRequest request);

    User getUserById(String id);

    Boolean insertByBatch(MultipartFile file);

    PageInfo<User>selectUserByPage(Integer pageNo, Integer pageSize,
                                   String deptId, Integer limit, Integer type);

    boolean setLimit(@Param("ids") List<String> ids, @Param("limit") Integer limit);

    boolean partialUpdate(User user);
}
