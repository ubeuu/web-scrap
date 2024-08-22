package com.example.web_scrap.brand.dto;

import java.util.List;

public record ScrapResponse(
        String name,
        String code,
        int price,
        List<String> images
) {
}
