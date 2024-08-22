package com.example.web_scrap.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebCrawler {

    @Value("${chrome.driver}")
    private String driverPath;

    @Bean
    public WebDriver webDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless"); //헤드리스 모드로 실행
        chromeOptions.addArguments("window-size=1920x1080");
        chromeOptions.addArguments("--lang=ko"); //브라우저 언어를 한국어로 설정
        chromeOptions.addArguments("--no-sandbox"); //샌드박스 모드 비활성화
        chromeOptions.addArguments("--disable-dev-shm-usage"); // /dev/shm 사용 비활성화
        chromeOptions.addArguments("--disable-gpu"); //GPU 가속 비활성화
        return new ChromeDriver(chromeOptions);
    }
}
