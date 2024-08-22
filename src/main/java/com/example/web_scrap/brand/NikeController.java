package com.example.web_scrap.brand;

import com.example.web_scrap.brand.dto.ScrapRequest;
import com.example.web_scrap.brand.dto.ScrapResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class NikeController {
    @Autowired
    private WebDriver driver;

    @GetMapping("/item/images")
    public ScrapResponse extractItemImagesFromUrl(@RequestBody ScrapRequest request) throws IOException {
        log.info("url 확인: {}", request.url());
        String url = request.url();
        if (url.contains("launch")) {
            return getLaunchSiteData(url);
        } else {
            return null;
        }
    }

    private ScrapResponse getLaunchSiteData(String url) {
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String html = driver.getPageSource();
        Document document = Jsoup.parse(html);

        //이미지 추출
        String className = "image-component.u-full-width.pdp-image";
        Elements imgElements = document.select("img." + className);
        log.info("이미지 요소 모음: {}", imgElements);
        List<String> images = imgElements.stream()
                .map(e -> e.attr("src"))
                .toList();

        //상품 정보 추출
        Element itemInfo = document.selectFirst("div.product-info.ncss-col-sm-12.full.ta-sm-c");
        assert itemInfo != null;
        String itemName = itemInfo.selectFirst("h1").text();
        String itemSubName = itemInfo.selectFirst("h2").text();
        int price = Integer.parseInt(itemInfo.selectFirst("div[data-qa=price]").text().replace(" 원", "").replace(",",""));
        log.info("info: {}, {}, {}", itemName, itemSubName, price);


        //상품 코드 추출
        Element itemInfo2 = document.selectFirst("div.description-text.text-color-grey.mb9-sm.ta-sm-c");
        assert itemInfo2 != null;
        String itemCode = itemInfo2.selectFirst("p").html()
                .split("<br>")[1].trim();
        log.info("code: {}", itemCode);

        return new ScrapResponse(itemName + " " + itemSubName, itemCode, price, images);
    }
}
