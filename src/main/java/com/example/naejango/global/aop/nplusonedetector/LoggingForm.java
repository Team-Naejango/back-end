package com.example.naejango.global.aop.nplusonedetector;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LoggingForm {
    public LoggingForm() {
        this.hibernateProxyAccessFlag = false;
        this.problemOccurFlag = false;
        this.relatedMethods = new ArrayList<>();
    }

    private String apiUrl;
    private String apiMethod;
    private boolean hibernateProxyAccessFlag;
    private boolean problemOccurFlag;
    private final List<String> relatedMethods;
    private int queryCounts = 0;
    private Long queryTime = 0L;

    public void setHibernateProxyAccessFlag(boolean hibernateProxyAccessFlag) {
        this.hibernateProxyAccessFlag = hibernateProxyAccessFlag;
    }

    public void setProblemOccurFlag(boolean problemOccurFlag) {
        this.problemOccurFlag = problemOccurFlag;
    }

    public void addCalledMethod(String methodName){
        relatedMethods.add(methodName);
    }

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

    public String toLog() {
        return "Result: " + (problemOccurFlag?"N+1 OCCURRED -> ":"OK -> ") + " API: [" + apiMethod + "]" + apiUrl +
                ", RelatedMethods: " + relatedMethods +
                ", QueryCounts: '" + queryCounts +
                "', QueryTime: '" + queryTime + "ms'";
    }
}