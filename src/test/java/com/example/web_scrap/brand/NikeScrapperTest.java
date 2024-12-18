package com.example.web_scrap.brand;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
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
    private String url = "http://www.test.site/launch";

    @Test
    void test_parseItemName() throws Exception {
        //given
        driverMocking();
        String tags = "<div class=\"product-info product-info-padding\">" +
                "<h1 class=\"nds-text pb3-sm css-1rhjrcc e1yhcai00 appearance-body1Strong color-primary weight-regular\">에어 폼포짓 원</h1>" +
                "<h2 class=\"nds-text pb3-sm css-11pxkba e1yhcai00 appearance-title3 color-primary weight-regular\">Jin</h2>" +
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
        String tags = "<div class=\"product-info product-info-padding\">" +
                "<div class=\"headline-5 pb6-sm fs14-sm fs16-md\">" +
                "299,000 원 </div>";
        given(driver.getPageSource()).willReturn(tags);

        //when, then
        mvc.perform(get("/nike/item/infos")
                        .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(299000)));
    }

    @Test
    void test_parseItemCode_condition1() throws Exception {
        //given
        driverMocking();
        String tags = "<div class=\"description-text text-color-grey\"> <p>상품 설명.<br> SKU: HF6367-001</p>";
        given(driver.getPageSource()).willReturn(tags);

        //when, then
        mvc.perform(get("/nike/item/infos")
                        .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("SKU: HF6367-001")));
    }

    @Test
    void test_parseItemCode_condition2() throws Exception {
        //given
        driverMocking();
        String tags = "<div class=\"available-date-component\"> 11. 7. 오전 10:00출시 </div>" +
                "<div class=\"available-date-component\"> <p>상품 설명. <br> SKU: HF0551-300</p> </div>";
        given(driver.getPageSource()).willReturn(tags);

        //when, then
        mvc.perform(get("/nike/item/infos")
                        .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("SKU: HF0551-300")));
    }

    @Test
    void test_parseItemImage() throws Exception {
        //given
        driverMocking();
        String tags = "<picture>" +
                "<img alt=\"에어 폼포짓 원 'Jin'(HF6367-001) 출시일\" class=\"image-img should-transition\" " +
                "data-testid=\"image-img\" src=\"https://static.nike.com/a/image.jpg\" " +
                "style=\"opacity: 1;\" loading=\"lazy\">" +
                "</picture>";
        given(driver.getPageSource()).willReturn(tags);

        //when, then
        mvc.perform(get("/nike/item/infos")
                        .param("url", url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]", is("https://static.nike.com/a/image.jpg")));
    }

    private void driverMocking() {
        // driver 세팅 모킹
        Options options = mock(Options.class);
        Window window = mock(Window.class);
        given(driver.manage()).willReturn(options);
        given(options.window()).willReturn(window);
        doNothing().when(window).setSize(new Dimension(1920, 1080));

        doNothing().when(driver).get(url);

        // WebDriver Wait 모킹
        WebElement imgElement = mock(WebElement.class);
        when(driver.findElement(By.tagName("img"))).thenReturn(imgElement);
        when(imgElement.isDisplayed()).thenReturn(true);
    }

    @Test
    void url_connect_fail() throws Exception {
        //given
        Options options = mock(Options.class);
        Window window = mock(Window.class);
        given(driver.manage()).willReturn(options);
        given(options.window()).willReturn(window);
        doNothing().when(window).setSize(new Dimension(1920, 1080));

        doThrow(new WebDriverException()).when(driver).get(url);

        //when, then
        mvc.perform(get("/nike/item/infos")
                        .param("url", url))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("ERR002")))
                .andExpect(jsonPath("$.message", is("연결 실패")));
    }
}