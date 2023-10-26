package com.example.naejango.global.aop.nplusonedetector;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Around("execution(* com.example..*Repository.*(..))")
    public Object entityProxy (final ProceedingJoinPoint joinPoint) throws Throwable {
        Object returnValue = joinPoint.proceed();
        if(isSimpleTypeObject(returnValue)) return returnValue;

        LoggingForm loggingForm = getLoggingForm();
        if(!loggingForm.isProblemOccurFlag()) loggingForm.setHibernateProxyAccessFlag(false);

        String methodName = joinPoint.getSignature().getName();
        if(returnValue instanceof Optional<?>){

            Optional<?> targetOpt = (Optional<?>) returnValue;
            if(targetOpt.isEmpty()) return returnValue;
            if(isSimpleTypeObject(targetOpt.get())) return returnValue;
            Object proxy = new EntityProxyHandler(targetOpt.get(), methodName, loggingForm).getProxy();
            return Optional.ofNullable(proxy);
        }

        if(returnValue instanceof List<?>){
            List<?> targetList = (List<?>) returnValue;
            if(targetList.isEmpty()) return returnValue;
            if(targetList.stream().anyMatch(NPlus1DetectorAop::isSimpleTypeObject)) return returnValue;
            return targetList.stream()
                    .map(t -> new EntityProxyHandler(t, methodName, loggingForm).getProxy()).collect(Collectors.toList());
        }

        return new EntityProxyHandler(returnValue, methodName, loggingForm).getProxy();
    }

    private static boolean isSimpleTypeObject(Object target) {
        return target == null || target.getClass().equals(String.class)
                || target.getClass().equals(Long.class) || target.getClass().equals(Integer.class);
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
        }

        // 거래 등록 요청만 요청 쿼리 개수가 많아서 따로 관리, 나머지 API는 요청 쿼리가 많은것 기준으로 조건 설정
        if (getLoggingForm().getApiMethod().equals("POST") && getLoggingForm().getApiUrl().equals("/api/transaction")){
            printLog(21);
        } else {
            printLog(8);
        }

        this.loggingForm.remove();
    }

    private void printLog(int queryCounts) {
        if (getLoggingForm().getQueryCounts() > queryCounts) {
            logger.error(getLoggingForm().toLog());
        } else {
            logger.info(getLoggingForm().toLog());
        }
    }

    private boolean isInRequestScope(final ServletRequestAttributes attributes) {
        return attributes != null;
    }
}