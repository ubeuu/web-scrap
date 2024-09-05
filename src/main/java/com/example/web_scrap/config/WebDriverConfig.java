package com.example.web_scrap.config;

import com.example.web_scrap.util.DriverManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Configuration
public class WebDriverConfig {

    @Bean
    public WebDriver webDriver() throws IOException {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless"); //헤드리스 모드로 실행
        chromeOptions.addArguments("window-size=1920x1080");
        chromeOptions.addArguments("--lang=ko"); //브라우저 언어를 한국어로 설정
        chromeOptions.addArguments("--no-sandbox"); //샌드박스 모드 비활성화
        chromeOptions.addArguments("--disable-dev-shm-usage"); // /dev/shm 사용 비활성화
        chromeOptions.addArguments("--disable-gpu"); //GPU 가속 비활성화
        try {
            return new ChromeDriver(chromeOptions);
        } catch (WebDriverException e) {
            log.info("-- 크롬과 드라이버 버전 미일치 예외 발생 -> 새로 드라이버 설치하고 실행");
            DriverManager.setDriver();
            return new ChromeDriver(chromeOptions);
        } catch (Exception e) {
            log.info("-- 기타 예외 발생");
            throw e;
        }
    }

    @PostConstruct
    public void init() throws IOException {
        String[] command = {
                getOsPath(),
                "--version"
        };
        Process process = new ProcessBuilder(command).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String version = reader.readLine();
        log.info("-- Chrome Version: " + version);
    }

    private String getOsPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        log.info("-- 현재 OS: " + osName);
        if (osName.contains("mac")) {
            return "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
        } else if (osName.contains("win")) {
            return "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
        } else if (osName.contains("nix") || osName.contains("nux")) {
            return "/usr/bin/google-chrome";
        } else {
            return null;
        }
    }
}