package com.example.web_scrap;

import org.springframework.web.bind.annotation.GetMapping;

public class WebController {
    @GetMapping("/healthcheck")
    public String healthCheck(){
        return "OK";
    }
}
