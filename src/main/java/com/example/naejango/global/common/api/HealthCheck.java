package com.example.naejango.global.common.api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {
    /** EC2 서버 접속 테스트를 위한 임시 API */
    @GetMapping("/")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}