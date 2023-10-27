package com.example.naejango.global.aop.nplusonedetector;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LoggingForm {
    public LoggingForm() {
        this.repositoryInvocationFlag = false;
        this.problemOccurFlag = false;
        this.relatedMethods = new ArrayList<>();
    }
    private String apiUrl;
    private String apiMethod;
    private boolean repositoryInvocationFlag;
    private boolean problemOccurFlag;
    private final List<String> relatedMethods;
    private int queryCounts = 0;
    private Long queryTime = 0L;

    public void setRepositoryInvocationFlag(boolean repositoryInvocationFlag) {
        this.repositoryInvocationFlag = repositoryInvocationFlag;
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
                (problemOccurFlag?(", RelatedMethods: " + relatedMethods):("")) +
                ", QueryCounts: '" + queryCounts +
                "', QueryTime: '" + queryTime + "ms'";
    }
}