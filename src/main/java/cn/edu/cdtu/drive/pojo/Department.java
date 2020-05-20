package cn.edu.cdtu.drive.pojo;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Department {
    private String id;

    private String title;

    private String type;

    private String pDid;
}