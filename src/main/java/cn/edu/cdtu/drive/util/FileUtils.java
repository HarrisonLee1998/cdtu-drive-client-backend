package cn.edu.cdtu.drive.util;


import cn.edu.cdtu.drive.pojo.Chunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author HarrisonLee
 * @date 2020/5/14 7:37
 */
public class FileUtils {
    public static String generatePath(String uploadFolder, Chunk chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append(uploadFolder).append("/").append(chunk.getIdentifier());
        if (!Files.isWritable(Paths.get(sb.toString()))) {
            try {
                Files.createDirectories(Paths.get(sb.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.append("/")
                .append(chunk.getIdentifier())
                .append("-")
                .append(chunk.getChunkNumber()).toString();
    }

    /**
     * 文件合并
     *
     * @param targetFile
     * @param folder
     */
    public static boolean merge(String targetFile, String folder, String filename) {
        try {
            Files.createFile(Paths.get(targetFile));
            Files.list(Paths.get(folder))
                    .filter(path -> !path.getFileName().toString().equals(filename))
                    .sorted((o1, o2) -> {
                        String p1 = o1.getFileName().toString();
                        String p2 = o2.getFileName().toString();
                        int i1 = p1.lastIndexOf("-");
                        int i2 = p2.lastIndexOf("-");
                        return Integer.valueOf(p2.substring(i2)).compareTo(Integer.valueOf(p1.substring(i1)));
                    })
                    .forEach(path -> {
                        try {
                            //以追加的形式写入文件
                            Files.write(Paths.get(targetFile), Files.readAllBytes(path), StandardOpenOption.APPEND);
                            //合并后删除该块
                            Files.delete(path);
                        } catch (IOException e) {
                            // log.error(e.getMessage(), e);
                        }
                    });
            return true;
        } catch (IOException e) {
            // log.error(e.getMessage(), e);
            System.out.println("合并失败");
            return false;
        }
    }
}
