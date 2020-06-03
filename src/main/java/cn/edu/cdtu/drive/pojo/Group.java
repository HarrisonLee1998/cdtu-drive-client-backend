package cn.edu.cdtu.drive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(value = { "handler" })
public class Group {
    private String id;

    @NotBlank
    private String title;

    private String avatar;

    private String brief;

    private Integer limit;

    private List<User> users;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer isWriteable;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer isReadable;
}