package cn.edu.cdtu.drive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(value = { "handler" })
public class FileItem {
    private String id;

    private String fName;

    private Long fSize;

    private String mime;

    private String fType;

    private Integer status;

    private String path;

    private LocalDateTime uDate;

    private String uId;
}