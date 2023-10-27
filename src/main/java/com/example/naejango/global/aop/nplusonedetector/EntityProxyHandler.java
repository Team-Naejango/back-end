package com.example.naejango.global.aop.nplusonedetector;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class EntityProxyHandler implements MethodInterceptor {
    private final Object target;
    private final String methodName;
    private final LoggingForm loggingForm;


    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object hibernateProxy = field.get(target);
            if(hibernateProxy == null) continue;
            String instanceName = hibernateProxy.getClass().getName();
            if(instanceName.contains("HibernateProxy")){
                String[] tmp = instanceName.split("\\$");
                String entityName = tmp[0].substring(tmp[0].lastIndexOf(".") + 2);
                String invocationMethodName = invocation.getMethod().getName();
                if(invocationMethodName.startsWith("get") && invocationMethodName.endsWith(entityName)){
                    loggingForm.setRepositoryInvocationFlag(true);
                    loggingForm.addCalledMethod(methodName);
                }
            }
        }
        return invocation.proceed();
    }

    public Object getProxy(){
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(this);
        return proxyFactory.getProxy();
    }
}
