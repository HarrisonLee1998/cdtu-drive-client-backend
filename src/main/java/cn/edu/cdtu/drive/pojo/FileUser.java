package cn.edu.cdtu.drive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(value = { "handler" })
public class FileUser implements Serializable {
    private String id;

    private String uId;

    private String fId;

    @NotBlank
    private String fName;

    @NotBlank
    private String fPath;

    private String fType;

    private Integer isFolder;

    private String fPid;

    private LocalDateTime lastUpdateDate;

    private Integer isDelete;

    private List<FileUser>list;

    private Long fSize;

    private String gId;

    private String shareId;
}