package com.example.naejango.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class NPlus1DetectorAop {

    private final ThreadLocal<LoggingForm> currentLoggingForm;

    private final Logger logger = LoggerFactory.getLogger("N+1 detector Log");

    public NPlus1DetectorAop() {
        this.currentLoggingForm = new ThreadLocal<>();
    }

    @Around("execution( * javax.sql.DataSource.getConnection())")
    public Object captureConnection(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Object connection = joinPoint.proceed();

        return new ConnectionProxyHandler(connection, getCurrentLoggingForm()).getProxy();
    }

    private LoggingForm getCurrentLoggingForm() {
        if (currentLoggingForm.get() == null) {
            currentLoggingForm.set(new LoggingForm());
        }

        return currentLoggingForm.get();
    }

    @After("within(@org.springframework.web.bind.annotation.RestController *) && !@annotation(com.example.naejango.global.aop.NoLogging)")
    public void loggingAfterApiFinish() {
        final LoggingForm loggingForm = getCurrentLoggingForm();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (isInRequestScope(attributes)) {
            HttpServletRequest request = attributes.getRequest();

            loggingForm.setApiMethod(request.getMethod());
            loggingForm.setApiUrl(request.getRequestURI());
        }

        // 거래 등록 요청만 요청 쿼리 개수가 많아서 따로 관리, 나머지 API는 요청 쿼리가 많은것 기준으로 조건 설정
        if (getCurrentLoggingForm().getApiMethod().equals("POST") && getCurrentLoggingForm().getApiUrl().equals("/api/transaction")){
            printLog(21);
        } else {
            printLog(8);
        }

        currentLoggingForm.remove();
    }

    private void printLog(int queryCounts) {
        if (getCurrentLoggingForm().getQueryCounts() > queryCounts) {
            logger.error(getCurrentLoggingForm().toLog());
        } else {
            logger.info(getCurrentLoggingForm().toLog());
        }
    }

    private boolean isInRequestScope(final ServletRequestAttributes attributes) {
        return attributes != null;
    }
}