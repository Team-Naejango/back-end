package com.example.naejango.global.aop.nplusonedetector;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class NPlus1DetectorAop {
    private final ThreadLocal<LoggingForm> loggingForm;
    private final Logger logger = LoggerFactory.getLogger("N+1 detector Log");

    public NPlus1DetectorAop() {
        this.loggingForm = new ThreadLocal<>();
    }

    // 커넥션 프록시를 반환하는 AOP
    @Around("execution( * javax.sql.DataSource.getConnection())")
    public Object captureConnection(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Object connection = joinPoint.proceed();
        return new ConnectionProxyHandler(connection, getLoggingForm()).getProxy();
    }

    @Pointcut("execution(* com.example..*Repository.*(..))")
    public void condition1(){}
    @Pointcut("execution(* com.example..*RepositoryImpl.*(..))")
    public void condition2(){}
    @Pointcut("execution(* com.example..*RepositoryCustom.*(..))")
    public void condition3(){}

    @Around("condition1()||condition2()||condition3()")
    public Object entityProxy (final ProceedingJoinPoint joinPoint) throws Throwable {
        LoggingForm loggingForm = getLoggingForm();
        loggingForm.setRepositoryInvocationFlag(true);
        loggingForm.addCalledMethod(joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }

    private LoggingForm getLoggingForm() {
        if (loggingForm.get() == null) {
            loggingForm.set(new LoggingForm());
        }
        return loggingForm.get();
    }

    @After("within(@org.springframework.web.bind.annotation.RestController *) && !@annotation(com.example.naejango.global.aop.nplusonedetector.NoLogging)")
    public void loggingAfterApiFinish() {
        final LoggingForm loggingForm = getLoggingForm();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (isInRequestScope(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            loggingForm.setApiMethod(request.getMethod());
            loggingForm.setApiUrl(request.getRequestURI());
            printLog(loggingForm);
        }

        this.loggingForm.remove();
    }

    private void printLog(final LoggingForm loggingForm) {
        if(loggingForm.isProblemOccurFlag()) {
            logger.error(loggingForm.toLog());
        } else {
            logger.info(loggingForm.toLog());
        }
    }

    private boolean isInRequestScope(final ServletRequestAttributes attributes) {
        return attributes != null;
    }
}