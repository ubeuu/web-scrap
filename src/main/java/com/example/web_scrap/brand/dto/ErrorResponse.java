package com.example.web_scrap.brand.dto;

import com.example.web_scrap.exception.WebExceptionMessage;

public record ErrorResponse(
        String code,
        String message
) {
    public ErrorResponse(WebExceptionMessage exception){
        this(exception.name(),exception.getMessage());
    }
}
