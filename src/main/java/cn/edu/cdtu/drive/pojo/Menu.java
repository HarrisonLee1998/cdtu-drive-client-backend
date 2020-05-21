package cn.edu.cdtu.drive.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(value = { "handler" })
public class Menu implements Serializable {
    private Integer id;
    private String link;
    private String title;
    private String icon;
    private Integer pmId;
    private List<Menu> subMenu;
}