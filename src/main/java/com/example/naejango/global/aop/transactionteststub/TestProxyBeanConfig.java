package com.example.naejango.global.aop.transactionteststub;

import com.example.naejango.domain.chat.repository.RedisSubscribeRepository;
import com.example.naejango.global.common.exception.TestException;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("TestStub:ProxyFactorBean")
public class TestProxyBeanConfig {

    @Bean
    @Primary
    public ProxyFactoryBean testSubscribeRepository(RedisSubscribeRepository redisSubscribeRepository) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(redisSubscribeRepository);
        proxyFactoryBean.addAdvisor(testAdvisor());
        return proxyFactoryBean;
    }

    private DefaultPointcutAdvisor testAdvisor(){
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.addMethodName("deleteSubscriptionIdBySessionId");
        pointcut.addMethodName("saveSubscriptionIdBySessionId");
        pointcut.addMethodName("deleteSessionId");
        advisor.setPointcut(pointcut);
        advisor.setAdvice((MethodInterceptor) invocation -> {
            switch(invocation.getMethod().getName()){
                case "saveSubscriptionIdBySessionId":
                    if(String.valueOf(invocation.getArguments()[1]).equals("subscribe")) throw new TestException();
                    break;
                case "deleteSubscriptionIdBySessionId":
                    if(String.valueOf(invocation.getArguments()[1]).equals("unsubscribe")) throw new TestException();
                    break;
                case "deleteSessionId":
                    if(String.valueOf(invocation.getArguments()[0]).equals("disconnect")) throw new TestException();
                    break;
            }
            return invocation.proceed();
        });
        return advisor;
    }

}
