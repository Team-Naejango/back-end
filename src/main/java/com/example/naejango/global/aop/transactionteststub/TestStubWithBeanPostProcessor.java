package com.example.naejango.global.aop.transactionteststub;

import com.example.naejango.global.common.exception.TestException;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationUtils;

@Configuration
@RequiredArgsConstructor
@Profile("TestStub:BeanPostProcessor")
public class TestStubWithBeanPostProcessor {

    @Bean
    public DefaultPointcutAdvisor testAdvisor(){
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(com.example.naejango.global.aop.transactionteststub.TransactionTest)");
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice((MethodInterceptor) invocation -> {
            TransactionTest annotation = AnnotationUtils.findAnnotation(invocation.getMethod(), TransactionTest.class);
            assert annotation != null;
            int pos = annotation.pos();
            String value = annotation.value();
            if(String.valueOf(invocation.getArguments()[pos]).equals(value)) throw new TestException();
            return invocation.proceed();
        });
        return advisor;
    }

    @Bean
    @Primary
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }
}
