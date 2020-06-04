package cn.edu.cdtu.drive.dao;

import cn.edu.cdtu.drive.pojo.Share;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShareMapper {
    boolean deleteByPrimaryKey(String id);

    boolean insert(Share record);

    Share selectByPrimaryKey(String id);

    List<Share> selectAll();

    boolean updateByPrimaryKey(Share record);

    // -----------------------
    List<Share>selectAllByUser(String uId);

    Boolean addViewTimes(String shareId);
    Boolean addSaveTimes(String shareId);
    Boolean addDownloadTimes(String shareId);

    Boolean deleteForUser(@Param("uId")String uId, @Param("ids")List<String>ids);

}