package cn.edu.cdtu.drive.pojo;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Login {
    private String uId;
    private String ip;
    private LocalDateTime date;
    private Integer state;
    private LocalDateTime lastActionDateTime;
    private Integer isAdmin;
}