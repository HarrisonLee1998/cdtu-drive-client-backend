package cn.edu.cdtu.drive.pojo;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Department {
    private String id;

    private String title;

    private Integer type;

    private String pDid;

    private List<Department>list;
}