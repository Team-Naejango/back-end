package com.example.naejango.global.aop.transactionteststub;

import com.example.naejango.global.common.exception.TestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("TestStub:AspectJ")
public class TestStubWithAspectJ {

    @Around("@annotation(com.example.naejango.global.aop.transactionteststub.TransactionTest)")
    public Object proxyTestStub(final ProceedingJoinPoint joinPoint) throws Throwable {
        TransactionTest annotation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(TransactionTest.class);
        String targetArg = String.valueOf(joinPoint.getArgs()[annotation.pos()]);
        if(targetArg.equals(annotation.value())) throw new TestException();
        return joinPoint.proceed();
    }

}
