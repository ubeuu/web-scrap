package com.example.web_scrap.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
public class DriverManager {

    @Value("${driver.folder}")
    private static String driverFolder;

    public static void setDriver() throws IOException {
        String projectPath = System.getProperty("user.dir");
        String driverPath = projectPath + "/" + driverFolder;
        log.info("-- 드라이버 경로: {}", driverPath);
        clearDirectory(Paths.get(driverPath));
        System.setProperty("wdm.cachePath", driverPath);
        WebDriverManager.chromedriver().setup();
    }

    private static void clearDirectory(Path directory) throws IOException {
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 파일 삭제
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // 하위 디렉토리 삭제
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
