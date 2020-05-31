package cn.edu.cdtu.drive.pojo;

public class Group {
    private String id;

    private String title;

    private String avatar;

    private String brief;

    private Integer limit;

    private Integer isWritable;

    private Integer isReadable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? null : avatar.trim();
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief == null ? null : brief.trim();
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getIsWritable() {
        return isWritable;
    }

    public void setIsWritable(Integer isWritable) {
        this.isWritable = isWritable;
    }

    public Integer getIsReadable() {
        return isReadable;
    }

    public void setIsReadable(Integer isReadable) {
        this.isReadable = isReadable;
    }
}