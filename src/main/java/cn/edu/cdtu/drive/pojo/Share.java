package cn.edu.cdtu.drive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(value = { "handler" })
public class Share {
    private String id;

    private String pwd;

    private Integer viewTimes;

    private Integer saveTimes;

    private Integer downloadTimes;

    private LocalDateTime createDate;

    private LocalDateTime expireDate;

    private String uId;

    private String link;
}