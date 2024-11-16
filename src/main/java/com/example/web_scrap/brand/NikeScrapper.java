package com.example.web_scrap.brand;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class NikeScrapper {
    @Autowired
    private WebDriver driver;

    @GetMapping("/nike/item/infos")
    public ScrapResponse extractItemInfosFromUrl(@RequestParam String url) throws IOException {
        log.info("-- url 확인: {}", url);
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

        String name = parseName(document);
        Integer price = parsePrice(document);
        String code = parseItemCode(document);
        List<String> images = parseImage(document);

        return new ScrapResponse(name, code, price, images);
    }

    private String parseItemCode(Document document) {
        // 상품 코드 추출
        // 본문의 상품코드 패턴을 정규표현식으로 표현 ([문자 3개]:[문자, 공백 6개이상]-[문자 3개이상])
        Pattern pattern = Pattern.compile("[A-Z0-9]{3}:[A-Z0-9 ]{6,}-[A-Z0-9]{3,}");

        Element condition1 = document.selectFirst("div.description-text.text-color-grey");
        log.info("-- condition1: {}", condition1);
        Matcher matcher = null;
        if (condition1 == null) {
            Elements condition2 = document.select("div.available-date-component");
            log.info("-- condition2: {}", condition2);
            matcher = pattern.matcher(condition2.text());
        } else {
            matcher = pattern.matcher(condition1.selectFirst("p").html());
        }

        String itemCode = null;
        if (matcher.find()) {
            itemCode = matcher.group();
            log.info("-- 상품코드: {}", itemCode);
        }
        return itemCode;
    }

    private List<String> parseImage(Document document) {
        // 이미지 추출
        Elements imgElements = document.select("img.image-img.should-transition");
        log.info("-- 이미지 요소: {}", imgElements.text());

        return imgElements.stream()
                .map(e -> e.attr("src"))
                .toList();
    }

    private String parseName(Document document) {
        // 상품 메인명+서브명 추출
        Element itemInfo = document.selectFirst("div.product-info");
        String main = null;
        String sub = null;
        if (itemInfo != null) {
            main = Optional.ofNullable(itemInfo.selectFirst("h1"))
                    .map(Element::text)
                    .orElse(null);
            sub = Optional.ofNullable(itemInfo.selectFirst("h2"))
                    .map(Element::text)
                    .orElse(null);
            log.info("-- 상품 메인+서브명: {}, {}", main, sub);
        }
        return (main == null && sub == null) ? null : main + " " + sub;
    }

    private Integer parsePrice(Document document) {
        // 상품 가격 추출
        Element itemInfo = document.selectFirst("div.product-info");
        Integer price = null;
        if (itemInfo != null) {
            price = Integer.parseInt(
                    Optional.ofNullable(itemInfo.selectFirst("div[data-qa=price], div.headline-5"))
                            .map(Element::text)
                            .orElse("0")
                            .replace(" 원", "")
                            .replace(",", ""));

            log.info("-- 상품 가격: {}", price);
        }
        return price;
    }
}
