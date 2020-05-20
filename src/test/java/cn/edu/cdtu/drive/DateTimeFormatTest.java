package cn.edu.cdtu.drive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author HarrisonLee
 * @date 2020/5/18 1:40
 */
@SpringBootTest
public class DateTimeFormatTest {

    @Value("${prop.upload-folder}")
    private String uploadFolder;

    @Test
    public void test01() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        System.out.println(formatter.format(LocalDateTime.now()));
    }

    @Test
    public void test02() {
        final LocalDateTime now = LocalDateTime.now();
        final int year = now.getYear();
        final int month = now.getMonthValue();
        final int day = now.getDayOfMonth();
        Path absolute = Paths.get(uploadFolder, Integer.toString(year), Integer.toString(month), Integer.toString(day));
        System.out.println(absolute.toString());
    }
}
