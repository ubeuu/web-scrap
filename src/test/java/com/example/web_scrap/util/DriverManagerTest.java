package com.example.web_scrap.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class DriverManagerTest {

    String driverFolder = "chrome-driver";

    @Test
    void 드라이버가_새로_설치됐는지_확인한다() throws IOException, NoSuchFieldException {
        // given
        Field field = DriverManager.class.getDeclaredField("driverFolder");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, null, driverFolder);
        // when
        DriverManager.setDriver();
        // then
        String formattedDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertThat(getPropertiesValue(), containsString(formattedDate));
    }

    private String getPropertiesValue() throws IOException {
        String driverPath = System.getProperty("user.dir") + "/" + driverFolder;
        File file = new File(driverPath, "resolution.properties");
        FileInputStream fileInputStream = new FileInputStream(file);

        Properties properties = new Properties();
        properties.load(fileInputStream);

        // resolution.properties의 특정 키 패턴을 정규표현식으로 표현 (chrome[숫자]-ttl)
        String regex = "^chrome\\d+-ttl$";
        Pattern pattern = Pattern.compile(regex);

        Optional<String> value = properties.stringPropertyNames().stream()
                .filter(e -> pattern.matcher(e).matches())
                .map(properties::getProperty)
                .findFirst();
        log.info("-- value: {}", value.get());
        return value.get();
    }
}