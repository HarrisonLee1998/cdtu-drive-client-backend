package cn.edu.cdtu.drive.pojo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Data
public class User {
    @ExcelProperty("编号")
    @NotBlank(message = "用户ID不能为空")
    private String id;

    @ExcelProperty("姓名")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @ExcelProperty("性别")
    private String gender;

    private String avatar;

    private String brief;

    private String email;

    @ExcelProperty("类型")
    private String type;

    private Long tSs;

    private Long uSs;

    @ExcelProperty("部门编号")
    private String deptId;

    private Integer limit;

    private Integer roleId;
}