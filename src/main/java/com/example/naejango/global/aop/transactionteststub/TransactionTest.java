package com.example.naejango.global.aop.transactionteststub;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionTest {
    String value() default "";
    int pos() default 0;
}
