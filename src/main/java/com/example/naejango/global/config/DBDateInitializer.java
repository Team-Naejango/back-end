package com.example.naejango.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DBDateInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {

    }

}
