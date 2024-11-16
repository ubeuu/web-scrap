package com.example.web_scrap.exception;

import lombok.Getter;

@Getter
public enum WebExceptionMessage {
    ERR001("웹 드라이버 연결 실패"),
    ERR002("시간 초과");
    private String message;

    private WebExceptionMessage(String message) {
        this.message=message;
    }
}
