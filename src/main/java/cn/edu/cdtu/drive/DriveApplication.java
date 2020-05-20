package cn.edu.cdtu.drive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan(value = "cn.edu.cdtu.drive.dao")
@SpringBootApplication
@EnableScheduling
public class DriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveApplication.class, args);
    }

}
