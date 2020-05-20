package cn.edu.cdtu.drive.pojo;


import lombok.*;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class User {
    @NotBlank(message = "用户ID不能为空")
    private String id;

    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String gender;

    private String avatar;

    private String brief;

    private String email;

    private String type;

    private Long tSs;

    private Long uSs;

    private String deptId;

    private Integer limit;
}