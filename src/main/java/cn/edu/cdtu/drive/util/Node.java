package cn.edu.cdtu.drive.util;

import lombok.*;

import java.util.List;

/**
 * @author HarrisonLee
 * @date 2020/5/20 22:16
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Node {
    private String id;
    private String label;
    private List<Node>children;

}
