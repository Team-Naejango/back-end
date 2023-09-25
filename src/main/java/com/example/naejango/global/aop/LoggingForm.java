package com.example.naejango.global.aop;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LoggingForm {

    private String apiUrl;
    private String apiMethod;
    private int queryCounts = 0;
    private Long queryTime = 0L;

    public void setApiUrl(final String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setApiMethod(final String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public void queryCountUp() {
        queryCounts++;
    }

    public void addQueryTime(final Long queryTime) {
        this.queryTime += queryTime;
    }
}