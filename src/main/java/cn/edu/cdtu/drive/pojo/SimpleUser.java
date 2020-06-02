package cn.edu.cdtu.drive.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Data
public class SimpleUser {

    @ExcelProperty("编号")
    private String id;

    @ExcelProperty("姓名")
    private String username;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("部门编号")
    private String deptId;

    public User toUser() {
        var user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setDeptId(this.deptId);
        if(Objects.equals(this.gender, "男") || Objects.equals(this.gender, "女")) {
            user.setGender(Objects.equals(this.gender, "男") ? 0 : 1);
        } else {
            throw new RuntimeException("性别字段值不合法");
        }
        if(Objects.equals(this.type, "学生") || Objects.equals(this.type, "教师")) {
            user.setType(Objects.equals(this.type, "学生") ? 0 : 1);
        } else {
            throw new RuntimeException("用户类型字段值不合法");
        }

        // 其他字段
        user.setPassword(this.id);

        return user;
    }
}
