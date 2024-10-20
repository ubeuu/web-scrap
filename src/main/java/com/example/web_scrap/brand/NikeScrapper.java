package com.example.web_scrap.brand;

import com.example.web_scrap.brand.dto.ScrapRequest;
import com.example.web_scrap.brand.dto.ScrapResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class NikeScrapper {
    @Autowired
    private WebDriver driver;

    @GetMapping("/item/infos")
    public ScrapResponse extractItemInfosFromUrl(@RequestBody ScrapRequest request) throws IOException {
        log.info("-- url 확인: {}", request.url());
        String url = request.url();
        if (url.contains("launch")) {
            return getLaunchSiteData(url);
        } else {
            return null;
        }
    }

    private ScrapResponse getLaunchSiteData(String url) {
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.get(url);
        // 페이지 로드 대기 (이미지 태그, 5초)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("img")));

        String html = driver.getPageSource();
        Document document = Jsoup.parse(html);

        // 이미지 추출
        Elements imgElements = document.select("img.image-img.should-transition");

        log.info("-- 이미지 요소: {}", imgElements.text());
        List<String> images = imgElements.stream()
                .map(e -> e.attr("src"))
                .toList();

        // 상품 메인명+서브명, 가격 추출
        Element itemInfo = document.selectFirst("div.product-info");
        String itemMainName = null;
        String itemSubName = null;
        Integer price = null;
        if (itemInfo != null) {
            itemMainName = itemInfo.selectFirst("h1").text();
            itemSubName = itemInfo.selectFirst("h2").text();
            price = Integer.parseInt(itemInfo.selectFirst("div[data-qa=price], div.headline-5").text()
                    .replace(" 원", "")
                    .replace(",", ""));
            log.info("-- 상품 정보: {}, {}, {}", itemMainName, itemSubName, price);
        }

        // 상품 코드 추출
        Matcher matcher = getItemInfo2(document);
        String itemCode = null;
        if (matcher.find()) {
            itemCode = matcher.group();
            log.info("-- 상품코드: {}", itemCode);
        }
        return new ScrapResponse(itemMainName + " " + itemSubName, itemCode, price, images);
    }

    private Matcher getItemInfo2(Document document) {
        // 본문의 상품코드 패턴을 정규표현식으로 표현 ([문자 3개]:[문자, 공백 6개이상]-[문자 3개이상])
        Pattern pattern = Pattern.compile("[A-Z0-9]{3}:[A-Z0-9 ]{6,}-[A-Z0-9]{3,}");

        Element itemInfo1 = document.selectFirst("div.description-text.text-color-grey");
        log.info("-- itemInfo1: {}", itemInfo1);

        if (itemInfo1 == null) {
            Elements itemInfo2 = document.select("div.available-date-component");
            log.info("-- itemInfo2: {}", itemInfo2.text());
            return pattern.matcher(itemInfo2.text());
        } else {
            return pattern.matcher(itemInfo1.selectFirst("p").html());
        }
    }
}
