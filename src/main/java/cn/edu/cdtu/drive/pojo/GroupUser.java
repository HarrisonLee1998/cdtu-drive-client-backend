package cn.edu.cdtu.drive.pojo;

import java.time.LocalDateTime;

public class GroupUser {
    private String gId;

    private String uId;

    private Integer guType;

    private LocalDateTime joinDate;

    private Integer status;

    public String getgId() {
        return gId;
    }

    public void setgId(String gId) {
        this.gId = gId == null ? null : gId.trim();
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId == null ? null : uId.trim();
    }

    public Integer getGuType() {
        return guType;
    }

    public void setGuType(Integer guType) {
        this.guType = guType;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}