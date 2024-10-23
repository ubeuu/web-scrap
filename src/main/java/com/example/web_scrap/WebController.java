package com.example.web_scrap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
    @GetMapping("/healthcheck")
    public String healthCheck(){
        return "OK";
    }
}
