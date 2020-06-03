package cn.edu.cdtu.drive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(value = { "handler" })
public class GroupUser {
    private String gId;

    private String uId;

    private Integer guType;

    private LocalDateTime joinDate;

    private Integer status;
}