package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
//app.yml kontrol
@ConfigurationProperties(prefix = "app.fastapi")
public class FastApiProperties {

    private String baseUrl;
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
