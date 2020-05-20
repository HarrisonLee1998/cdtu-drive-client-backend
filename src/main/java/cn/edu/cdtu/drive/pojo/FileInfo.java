package cn.edu.cdtu.drive.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author HarrisonLee
 * @date 2020/5/14 7:22
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FileInfo implements Serializable {
    private Long id;

    private String fileName;

    private String identifier;

    private Long totalSize;

    private String type;

    private String location;

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", filename='" + fileName + '\'' +
                ", identifier='" + identifier + '\'' +
                ", totalSize=" + totalSize +
                ", type='" + type + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
