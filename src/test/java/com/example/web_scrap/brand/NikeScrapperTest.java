package com.example.web_scrap.brand;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.openqa.selenium.WebDriver.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(NikeScrapper.class)
class NikeScrapperTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    WebDriver driver;
    private String url="http://www.test.site/launch";
    //1. 함수가 제대로 동작하는지 (모의 태그 입력시 원하는 값만 추출 가능한지)
    @Test
    void test_parseItemCode() throws Exception {
        //given
        driverMocking();
        String tags=
                "<div class=\"product-info product-info-padding\">\n" +
                        "<h1 class=\"nds-text pb3-sm css-1rhjrcc e1yhcai00 appearance-body1Strong color-primary weight-regular\">에어 폼포짓 원</h1>\n" +
                        "<h2 class=\"nds-text pb3-sm css-11pxkba e1yhcai00 appearance-title3 color-primary weight-regular\">Jin</h2>\n" +
                        "</div>";
        given(driver.getPageSource()).willReturn(tags);

        //when, then
        mvc.perform(get("/nike/item/infos")
                .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("에어 폼포짓 원 Jin")));
    }

    @Test
    void test_parseItemPrice() throws Exception {
        //given
        driverMocking();
        String tags=
                "<div class=\"product-info product-info-padding\">\n" +
                        "<div class=\"headline-5 pb6-sm fs14-sm fs16-md\">\n" +
                        "299,000 원\n" +
                        "</div>";
        given(driver.getPageSource()).willReturn(tags);

        //when, then
        mvc.perform(get("/nike/item/infos")
                        .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(299000)));
    }

    private void driverMocking(){
        // driver 세팅 모킹
        Options options = mock(Options.class);
        Window window = mock(Window.class);
        given(driver.manage()).willReturn(options);
        given(options.window()).willReturn(window);
        doNothing().when(window).setSize(new Dimension(1920, 1080));

        doNothing().when(driver).get(url);

        // WebDriver Wait 모킹
        WebElement imgElement=mock(WebElement.class);
        when(driver.findElement(By.tagName("img"))).thenReturn(imgElement);
        when(imgElement.isDisplayed()).thenReturn(true);
    }
    //2. 예외 테스트(스크랩 시간 or 페이지 로드 지연시 예외 처리/일부 누락돼도 나머지 데이터 출력 확인)
}