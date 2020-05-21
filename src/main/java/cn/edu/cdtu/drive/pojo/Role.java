package cn.edu.cdtu.drive.pojo;

import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Role {
    private Integer id;

    private String title;

    private LocalDateTime createDate;

    private LocalDateTime lastUpdateDate;
}