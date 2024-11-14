package com.example.web_scrap.exception;

import com.example.web_scrap.brand.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(TimeoutException.class)
    public ErrorResponse handleException(TimeoutException ex) {
        log.error("** exception: {}", ex.getMessage());
        return new ErrorResponse("ERR001", "연결 시간 초과");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(WebDriverException.class)
    public ErrorResponse handleException(WebDriverException ex) {
        log.error("** exception: {}", ex.getMessage());
        return new ErrorResponse("ERR002", "연결 실패");
    }
}
