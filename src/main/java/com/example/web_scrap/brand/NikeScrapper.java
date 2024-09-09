package com.example.web_scrap.brand;

import com.example.web_scrap.brand.dto.ScrapRequest;
import com.example.web_scrap.brand.dto.ScrapResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        driver.get(url);
        // 페이지 로드 대기 (이미지 태그, 10초)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("img")));

        String html = driver.getPageSource();
        Document document = Jsoup.parse(html);

        // 이미지 추출
        String className = "image-component.u-full-width.pdp-image";
        Elements imgElements = document.select("img." + className);

        log.info("-- 이미지 요소: {}", imgElements);
        List<String> images = imgElements.stream()
                .map(e -> e.attr("src"))
                .toList();

        // 상품 정보 추출
        Element itemInfo = document.selectFirst("div.product-info.ncss-col-sm-12.full");
        assert itemInfo != null;
        String itemName = itemInfo.selectFirst("h1").text();
        String itemSubName = itemInfo.selectFirst("h2").text();
        int price = Integer.parseInt(itemInfo.selectFirst("div[data-qa=price], div.headline-5").text().replace(" 원", "").replace(",", ""));
        log.info("-- 상품 정보: {}, {}, {}", itemName, itemSubName, price);


        // 상품 코드 추출
        Element itemInfo2 = document.selectFirst("div.description-text.text-color-grey");
        assert itemInfo2 != null;

        // 본문의 상품코드 패턴을 정규표현식으로 표현 ([문자 3개]:[문자, 공백 6개이상]-[문자 3개이상])
        String regex = "[A-Z0-9]{3}:[A-Z0-9 ]{6,}-[A-Z0-9]{3,}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(itemInfo2.selectFirst("p").html());

        String itemCode = null;
        if (matcher.find()) {
            itemCode = matcher.group();
            log.info("-- 상품코드: {}", itemCode);
        }
        return new ScrapResponse(itemName + " " + itemSubName, itemCode, price, images);
    }
}
